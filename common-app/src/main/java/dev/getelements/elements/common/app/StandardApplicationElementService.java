package dev.getelements.elements.common.app;

import dev.getelements.elements.rt.ApplicationAssetLoader;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.util.Monitor;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.getelements.elements.sdk.ElementPathLoader.newDefaultInstance;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.cluster.id.ApplicationId.forUniqueName;

public class StandardApplicationElementService implements ApplicationElementService {

    private ElementRegistry rootElementRegistry;

    private ApplicationAssetLoader applicationAssetLoader;

    private final Lock lock = new ReentrantLock();

    private final ConcurrentMap<ApplicationId, Lock> locks = new ConcurrentHashMap<>();

    private final Map<ApplicationId, ElementRegistry> registries = new HashMap<>();

    private final Map<ApplicationId, ApplicationElementRecord> records = new HashMap<>();

    @Override
    public ElementRegistry getElementRegistry(final Application application) {
        try (var mon = Monitor.enter(lock)) {
            final var applicationId = forUniqueName(application.getId());
            return doGetOrLoadElementRegistry(applicationId);
        }
    }

    private ElementRegistry doGetOrLoadElementRegistry(final ApplicationId applicationId) {
        return registries.computeIfAbsent(
                applicationId,
                id -> rootElementRegistry.newSubordinateRegistry()
        );
    }

    @Override
    public ApplicationElementRecord getOrLoadApplication(final Application application) {

        final var applicationId = forUniqueName(application.getId());

        final Path path;

        try {
            path = getApplicationAssetLoader().getAssetPath(applicationId);
        } catch (UncheckedIOException ex) {
            if (ex.getCause() instanceof FileNotFoundException) {
                final var registry = getRootElementRegistry().newSubordinateRegistry();
                return new ApplicationElementRecord(applicationId, registry, List.of());
            } else {
                throw ex;
            }
        }

        try (final var monitor = Monitor.enter(lock)) {
            return records.computeIfAbsent(applicationId, aid -> {

                final var registry = doGetOrLoadElementRegistry(applicationId);

                try {
                    final var loader = newDefaultInstance();
                    final var elements = loader.load(registry, path).toList();
                    return new ApplicationElementRecord(applicationId, registry, elements);
                } catch (Exception ex) {
                    registry.close();
                    throw ex;
                }

            });
        }

    }

    public ElementRegistry getRootElementRegistry() {
        return rootElementRegistry;
    }

    @Inject
    public void setRootElementRegistry(@Named(ROOT) ElementRegistry rootElementRegistry) {
        this.rootElementRegistry = rootElementRegistry;
    }

    public ApplicationAssetLoader getApplicationAssetLoader() {
        return applicationAssetLoader;
    }

    @Inject
    public void setApplicationAssetLoader(@Named(ApplicationAssetLoader.ELEMENT_STORAGE) ApplicationAssetLoader applicationAssetLoader) {
        this.applicationAssetLoader = applicationAssetLoader;
    }

}
