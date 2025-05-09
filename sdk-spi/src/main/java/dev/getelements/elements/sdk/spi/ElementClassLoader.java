package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.PermittedPackages;
import dev.getelements.elements.sdk.PermittedTypes;
import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementLocal;
import dev.getelements.elements.sdk.annotation.ElementPrivate;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.ElementRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static dev.getelements.elements.sdk.spi.UrlUtils.toUrl;
import static dev.getelements.elements.sdk.spi.UrlUtils.toUrls;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * A {@link ClassLoader} type which inspects classes at load time processing the visibility annotations provided by the
 * Elements' SDK. Such annotations include {@link ElementPrivate} and {@link ElementPublic}.
 */
public class ElementClassLoader extends ClassLoader {

    private static final Map<String, String> BUILTIN_RESOURCES = Map.of(
            "META-INF/services/dev.getelements.elements.sdk.ElementSupplier",
            "text://dev.getelements.elements.sdk.spi.ElementScopedElementSupplier",
            "META-INF/services/dev.getelements.elements.sdk.ElementRegistrySupplier",
            "text://dev.getelements.elements.sdk.spi.ElementScopedElementRegistrySupplier"
    );

    private static final List<Predicate<Class<?>>> BUILTIN_TYPES_WHITELIST = ServiceLoader
            .load(PermittedTypes.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toUnmodifiableList());

    private static final List<Predicate<Package>> BUILTIN_PACKAGES_WHITELIST = ServiceLoader
            .load(PermittedPackages.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toUnmodifiableList());

    private ElementRecord elementRecord;

    public ElementClassLoader(final ClassLoader parent) {
        super(requireNonNull(parent, "parent"));
    }

    public ElementRecord getElementRecord() {
        return elementRecord;
    }

    public void setElementRecord(final ElementRecord elementRecord) {
        this.elementRecord = elementRecord;
    }

    @Override
    protected URL findResource(String name) {
        final var url = BUILTIN_RESOURCES.getOrDefault(name, null);
        return url == null ? null : toUrl(url);
    }

    @Override
    protected Enumeration<URL> findResources(final String name) throws IOException {
        final var url = BUILTIN_RESOURCES.getOrDefault(name, null);
        return url == null ? toUrls() : toUrls(url);
    }

    @Override
    protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {

        Class<?> aClass;

        try {
            // All system-provided classes automatically exist in the classloader hierarchy without needing special
            // permission. This enables the element to see the core JVM classes, but essentially skips all of the
            // Element's classpath.
            aClass = getPlatformClassLoader().loadClass(name);
        } catch (ClassNotFoundException ex) {
            // If not present, then we load the class from the parent and the process any annotations. We have explicit
            // whitelists.
            aClass = processVisibilityAnnotations(getParent().loadClass(name));
        }

        if (resolve) {
            resolveClass(aClass);
        }

        return aClass;

    }

    private Class<?> processVisibilityAnnotations(final Class<?> aClass) throws ClassNotFoundException {

        final var name = aClass.getName();

        // SPI Types annotated as ElementLocal will be copied from the bytecode of the parent and then loaded into this
        // classloader. This ensures that the Element Local types are unique per Element.
        if (aClass.getAnnotation(ElementLocal.class) != null) {
            final var aLocalClass = findLoadedClass(name);
            return aLocalClass == null ? copyFromParent(aClass) : aLocalClass;
        }

        final var aClassPackage = aClass.getPackage();

        // If the class is part of the whitelist that's builtin, then we honor it. This may be something we want to
        // make configurable based on application attributes later, but we'll stitck to hardcoded values we have for
        // now. We will likely need to extend more.

        if (BUILTIN_TYPES_WHITELIST.stream().anyMatch(t -> t.test(aClass))) {
            return aClass;
        }

        if (BUILTIN_PACKAGES_WHITELIST.stream().anyMatch(p -> p.test(aClassPackage))) {
            return aClass;
        }

        if (aClass.isAnnotationPresent(ElementPrivate.class)) {
            throw new ClassNotFoundException(format(
                    "%s is @%s",
                    aClass.getSimpleName(),
                    ElementPrivate.class.getSimpleName()
            ));
        }

        if (aClassPackage.isAnnotationPresent(ElementPrivate.class)) {

            final var message = format("%s's package (%s) is @%s",
                    aClass.getSimpleName(),
                    aClassPackage.getName(),
                    ElementPrivate.class.getSimpleName()
            );

            throw new ClassNotFoundException(message);

        }

        if (aClass.isAnnotationPresent(ElementPublic.class)) {
            return aClass;
        }

        if (aClassPackage.isAnnotationPresent(ElementPublic.class)) {
            return aClass;
        }

        if (getElementRecord() == null) {
            // This happens pre-initialization of the classloader, so there is not an ElementRecord yet to be loaded
            // so this remains as-is until it is properly scoped.
            return aClass;
        }

        final var isRegisteredService = getElementRecord()
                .services()
                .stream()
                .flatMap(svc -> svc.export().exposed().stream())
                .anyMatch(exposed -> exposed.equals(aClass));

        if (isRegisteredService) {
            return aClass;
        }

        final var message = format("%s or %s's package (%s) must have @%s annotation or be exposed via @%s",
                aClass.getSimpleName(),
                aClass.getName(),
                aClassPackage,
                ElementPublic.class.getSimpleName(),
                ElementDefinition.class.getSimpleName()
        );

        throw new ClassNotFoundException(message);

    }

    private Class<?> copyFromParent(final Class<?> parentClass) {

        final var clsName = parentClass.getName();
        final var registryResourceURL = clsName.replace(".", "/") + ".class";

        try (var is = getParent().getResourceAsStream(registryResourceURL);
             var os = new ByteArrayOutputStream()) {

            assert is != null;
            is.transferTo(os);

            final var bytes = os.toByteArray();
            return defineClass(clsName, bytes, 0, bytes.length);

        } catch (IOException ex) {
            throw new SdkException(ex);
        }

    }

}
