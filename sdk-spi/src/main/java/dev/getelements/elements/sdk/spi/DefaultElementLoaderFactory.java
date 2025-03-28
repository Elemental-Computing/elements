package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.annotation.*;
import dev.getelements.elements.sdk.exception.SdkElementNotFoundException;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.*;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.ElementLoader.ELEMENT_RECORD;
import static dev.getelements.elements.sdk.ElementLoader.SERVICE_LOCATOR;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class DefaultElementLoaderFactory implements ElementLoaderFactory {

    @Override
    public ElementLoader getIsolatedLoader(
            final Attributes attributes,
            final ClassLoader baseClassLoader,
            final ClassLoaderConstructor classLoaderCtor,
            final Predicate<ElementDefinitionRecord> selector) {
        final var elementRecord = loadElementRecord(attributes, baseClassLoader, classLoaderCtor, selector);
        final var elementLoader = newIsolatedLoader(baseClassLoader, elementRecord);
        return inject(elementLoader, elementRecord, null);
    }

    private ElementRecord loadElementRecord(
            final Attributes attributes,
            final ClassLoader baseClassLoader,
            final ClassLoaderConstructor classLoaderCtor,
            final Predicate<ElementDefinitionRecord> selector) {

        // We first create a classloader for the purposes of scanning for annotations and building the element
        // definitions.
        final var isolatedElementClassLoader = new ElementClassLoader(baseClassLoader);
        final var elementClassLoader = classLoaderCtor.apply(isolatedElementClassLoader);

        // The Module Definition Records and Services
        final var elementDefinitionRecord = scanForModuleDefinition(elementClassLoader, selector);
        final var elementServices = scanForElementServices(elementClassLoader, elementDefinitionRecord);
        final var elementProducedEvents = scanForProducedEvents(elementClassLoader, elementDefinitionRecord);
        final var elementConsumedEvents = scanForConsumedEvents(elementClassLoader, elementDefinitionRecord, elementServices);
        final var elementDefaultAttributes = scanForDefaultAttributes(elementClassLoader, elementDefinitionRecord);

        // The Module Records and Services
        final var elementResolvedAttributes = new SimpleAttributes.Builder()
                .from(elementDefaultAttributes)
                .from(attributes)
                .build()
                .immutableCopy();

        final var elementRecord = new ElementRecord(
                ElementType.ISOLATED_CLASSPATH,
                elementDefinitionRecord,
                elementServices,
                elementProducedEvents,
                elementConsumedEvents,
                elementResolvedAttributes,
                elementDefaultAttributes,
                elementClassLoader
        );

        // Finally, initializes the isolated classloader with the ElementRecord
        inject(baseClassLoader, elementRecord, null);
        inject(elementClassLoader, elementRecord, null);

        return elementRecord;

    }

    @Override
    public ElementRecord getElementRecord(Attributes attributes, final Package aPackage) {
        return loadElementRecord(attributes, aPackage);
    }

    @Override
    public Stream<ElementServiceRecord> getExposedServices(final Package aPackage) {
        final var localClassLoader = getClass().getClassLoader();
        final var elementDefinitionRecord = ElementDefinitionRecord.fromPackage(aPackage);
        return scanForElementServices(localClassLoader, elementDefinitionRecord).stream();
    }

    @Override
    public ElementLoader getSharedLoader(final ElementRecord elementRecord,
                                         final ServiceLocator serviceLocator) {
        final var elementLoader = newSharedLoader(elementRecord);
        return inject(elementLoader, elementRecord, serviceLocator);
    }

    private ElementRecord loadElementRecord(final Attributes attributes, final Package aPackage) {

        final var localClassLoader = getSystemClassLoader();
        final var elementDefinitionRecord = ElementDefinitionRecord.fromPackage(aPackage);
        final var elementServices = scanForElementServices(localClassLoader, elementDefinitionRecord);
        final var elementProducedEvents = scanForProducedEvents(localClassLoader, elementDefinitionRecord);
        final var elementConsumedEvents = scanForConsumedEvents(localClassLoader, elementDefinitionRecord, elementServices);
        final var elementDefaultAttributes = scanForDefaultAttributes(localClassLoader, elementDefinitionRecord);

        // The Module Records and Services
        final var elementResolvedAttributes = new SimpleAttributes.Builder()
                .from(elementDefaultAttributes)
                .from(attributes)
                .build()
                .immutableCopy();

        return new ElementRecord(
                ElementType.SHARED_CLASSPATH,
                elementDefinitionRecord,
                elementServices,
                elementProducedEvents,
                elementConsumedEvents,
                elementResolvedAttributes,
                elementDefaultAttributes,
                localClassLoader
        );

    }

    private List<ElementDefaultAttributeRecord> scanForDefaultAttributes(
            final ClassLoader classLoader,
            final ElementDefinitionRecord elementDefinitionRecord) {

        final var cg = new ClassGraph()
                .enableClassInfo()
                .enableFieldInfo()
                .enableAnnotationInfo()
                .overrideClassLoaders(classLoader);

        elementDefinitionRecord.acceptPackages(
                cg::acceptPackages,
                cg::acceptPackagesNonRecursive
        );

        try (final var result = cg.scan()) {

            return result
                    .getClassesWithFieldAnnotation(ElementDefaultAttribute.class)
                    .stream()
                    .flatMap(classInfo -> classInfo
                            .getDeclaredFieldInfo()
                            .stream()
                            .filter(fieldInfo ->
                                    fieldInfo.hasAnnotation(ElementDefaultAttribute.class) &&
                                    fieldInfo.isStatic() &&
                                    fieldInfo.isFinal())
                            .map(FieldInfo::loadClassAndGetField)
                    )
                    .map(field -> {
                        try {
                            final var value = field.get(null).toString();
                            final var annotation = field.getAnnotation(ElementDefaultAttribute.class);
                            field.setAccessible(true);
                            return new ElementDefaultAttributeRecord(value, annotation.value());
                        } catch (IllegalAccessException ex) {
                            throw new SdkException(ex);
                        }
                    })
                    .collect(toList());

        }

    }

    private ElementDefinitionRecord scanForModuleDefinition(
            final ClassLoader classLoader,
            final Predicate<ElementDefinitionRecord> selector) {

        final var cg = new ClassGraph()
                .overrideClassLoaders(classLoader)
                .enableClassInfo()
                .enableAnnotationInfo();

        try (final var result = cg.scan()) {

            final var elementDefinitionRecords = result
                    .getPackageInfo()
                    .stream()
                    .filter(nfo -> nfo.hasAnnotation(ElementDefinition.class))
                    .map(nfo -> {
                        try {
                            final var packageInfoClass = nfo.getName() + ".package-info";
                            return classLoader.loadClass(packageInfoClass);
                        } catch (ClassNotFoundException ex) {
                            throw new SdkException("Unable to find package-info: " + nfo.getName(), ex);
                        }
                    })
                    .map(Class::getPackage)
                    .map(ElementDefinitionRecord::fromPackage)
                    .filter(selector)
                    .toList();

            if (elementDefinitionRecords.size() > 1) {
                throw new SdkException("Found more than one element definition: " + elementDefinitionRecords);
            } else if (elementDefinitionRecords.isEmpty()) {
                throw new SdkElementNotFoundException("Found no element definition in " + classLoader);
            }

            return elementDefinitionRecords.getFirst();

        }

    }

    private List<ElementServiceRecord> scanForElementServices(
            final ClassLoader classLoader,
            final ElementDefinitionRecord elementDefinitionRecord) {

        final var classGraph = new ClassGraph()
                .overrideClassLoaders(classLoader)
                .enableClassInfo()
                .enableAnnotationInfo();

        elementDefinitionRecord.acceptPackages(
                classGraph::acceptPackages,
                classGraph::acceptPackagesNonRecursive
        );

        try (final var result = classGraph.scan()) {

            final var fromPackage = ElementServiceRecord.fromPackage(elementDefinitionRecord.pkg());

            final var fromClasses = result.getClassesWithAnnotation(ElementServiceExport.class)
                    .stream()
                    .map(ClassInfo::loadClass)
                    .flatMap(ElementServiceRecord::fromClass);

            return Stream.concat(fromPackage, fromClasses).collect(toList());

        }

    }

    private List<ElementEventProducerRecord> scanForProducedEvents(
            final ClassLoader classLoader,
            final ElementDefinitionRecord elementDefinitionRecord) {

        final var classGraph = new ClassGraph()
                .overrideClassLoaders(classLoader)
                .enableClassInfo()
                .enableMethodInfo()
                .enableAnnotationInfo();

        if (elementDefinitionRecord.recursive()) {
            classGraph.acceptPackages(elementDefinitionRecord.pkgName());
        } else {
            classGraph.acceptPackagesNonRecursive(elementDefinitionRecord.pkgName());
        }

        try (var result = classGraph.scan()) {
            return result.getClassesWithAnnotation(ElementEventProducer.class)
                    .stream()
                    .map(ClassInfo::loadClass)
                    .flatMap(aClass -> Stream.of(aClass.getAnnotationsByType(ElementEventProducer.class)))
                    .map(ElementEventProducerRecord::from)
                    .toList();
        }

    }

    private List<ElementEventConsumerRecord<?>> scanForConsumedEvents(
            final ClassLoader classLoader,
            final ElementDefinitionRecord elementDefinitionRecord,
            final List<ElementServiceRecord> elementServiceRecords) {

        final var serviceInterfaces = elementServiceRecords
                .stream()
                .flatMap(ElementServiceRecord::exposedTypes)
                .toList();

        final var classGraph = new ClassGraph()
                .overrideClassLoaders(classLoader)
                .enableClassInfo()
                .enableMethodInfo()
                .enableAnnotationInfo()
                .acceptClasses(serviceInterfaces
                        .stream()
                        .map(Class::getName)
                        .toArray(String[]::new)
                );

        elementDefinitionRecord.acceptPackages(
                classGraph::acceptPackages,
                classGraph::acceptPackagesNonRecursive
        );

        try (var result = classGraph.scan()) {

            final var methods = result
                    .getClassesWithMethodAnnotation(ElementEventConsumer.class)
                    .stream()
                    .collect(toMap(ClassInfo::loadClass, classInfo -> classInfo
                            .getMethodInfo()
                            .filter(methodInfo -> methodInfo.hasAnnotation(ElementEventConsumer.class))
                            .stream()
                            .map(MethodInfo::loadClassAndGetMethod)
                            .toList()
                    ));

            final var interfaceMethods = elementServiceRecords.stream()
                    .flatMap(esr -> esr
                            .export()
                            .exposed()
                            .stream()
                            .filter(methods::containsKey)
                            .flatMap(interfaceType -> methods.get(interfaceType)
                                    .stream()
                                    .flatMap(method -> ElementEventConsumerRecord.from(esr, method)))
                    );

            final var implementationExposedMethods = elementServiceRecords.stream()
                    .filter(esr -> methods.containsKey(esr.implementation().type()))
                    .flatMap(esr -> methods.get(esr.implementation().type())
                            .stream()
                            .flatMap(method -> ElementEventConsumerRecord.from(esr, method))
                    );

            return Stream.concat(interfaceMethods, implementationExposedMethods).collect(toList());

        }

    }

    private ElementLoader newSharedLoader(final ElementRecord elementRecord) {

        final var aClass = elementRecord.definition().loader();

        if (ElementLoader.Default.class.equals(aClass)) {
            return new DefaultSharedElementLoader();
        } else {
            try {
                final var ctor = aClass.getConstructor();
                return ctor.newInstance();
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                throw new SdkException(e);
            }
        }

    }

    private ElementLoader newIsolatedLoader(
            final ClassLoader classLoader,
            final ElementRecord elementRecord) {

        final var aClass = elementRecord.definition().loader();

        if (ElementLoader.Default.class.equals(aClass)) {

            final var loader = ServiceLoader.load(ElementLoader.class, classLoader);

            return loader.findFirst().orElseThrow(() -> new SdkException(
                    "No SPI (Service Provider Implementation) for " + ElementLoader.class.getName())
            );

        } else {
            try {
                final var ctor = aClass.getConstructor();
                return ctor.newInstance();
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                throw new SdkException(e);
            }
        }

    }

    private <T> T inject(final T target,
                         final ElementRecord elementRecord,
                         final ServiceLocator serviceLocator) {

        try {

            final var info = Introspector.getBeanInfo(target.getClass());

            for (var descriptor : info.getPropertyDescriptors()) {

                if (descriptor.getName().equals(ELEMENT_RECORD)) {
                    descriptor.getWriteMethod().invoke(target, elementRecord);
                }

                if (descriptor.getName().equals(SERVICE_LOCATOR)) {
                    descriptor.getWriteMethod().invoke(target, serviceLocator);
                }

            }

        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException ex) {
            throw new SdkException(ex);
        }

        return target;

    }

}
