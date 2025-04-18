package dev.getelements.elements.rt.remote.provider;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import java.util.concurrent.ExecutorService;

import static dev.getelements.elements.rt.remote.Instance.THREAD_GROUP;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class CachedThreadPoolProvider implements Provider<ExecutorServiceFactory<ExecutorService>> {

    private Provider<ThreadGroup>threadGroupProvider;

    @Override
    public ExecutorServiceFactory<ExecutorService> get() {
        return name -> {
            final var group = getThreadGroupProvider().get();
            final var factory = new InstanceThreadFactory(group, name);
            return newCachedThreadPool(factory);
        };
    }

    public Provider<ThreadGroup> getThreadGroupProvider() {
        return threadGroupProvider;
    }

    @Inject
    public void setThreadGroupProvider(@Named(THREAD_GROUP) Provider<ThreadGroup> threadGroupProvider) {
        this.threadGroupProvider = threadGroupProvider;
    }

}
