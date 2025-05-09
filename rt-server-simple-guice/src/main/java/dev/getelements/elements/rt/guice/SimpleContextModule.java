package dev.getelements.elements.rt.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.PersistenceStrategy;
import dev.getelements.elements.rt.SimpleContext;
import dev.getelements.elements.rt.remote.NodeLifecycle;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.LOCAL;

public class SimpleContextModule extends PrivateModule {

    private Runnable bindIndexContext = () -> {};

    private Runnable bindResourceContext = () -> {};

    private Runnable bindSchedulerContext = () -> {};

    private Runnable bindHandlerContext = () -> {};

    private Runnable bindTaskContext = () -> {};

    private Runnable bindManifestContext = () -> {};

    private Runnable bindEventContext = () -> {};

    /**
     * Specifies the default contexts and services.
     *
     * @return this instance
     */
    public SimpleContextModule withDefaultContexts() {
        bindIndexContext = () -> install(new SimpleIndexContextModule());
        bindResourceContext = () -> install(new SimpleResourceContextModule());
        bindSchedulerContext = () -> install(new SimpleSchedulerContextModule());
        bindHandlerContext = () -> install(new SimpleHandlerContextModule().withDefaultTimeout());
        bindTaskContext = () -> install(new SimpleTaskContextModule());
        bindManifestContext = () -> install(new SimpleManifestContextModule());
        bindEventContext = () -> install(new SimpleEventContextModule().withDefaultTimeout());
        return this;
    }

    /**
     * Specifies one or more modules as service level modules.
     *
     * @param modules the modules to add
     * @return this instance
     */
    public SimpleContextModule withSchedulerContextModules(final com.google.inject.Module ... modules) {
        bindSchedulerContext = () -> { for (final com.google.inject.Module m : modules) install(m); };
        return this;

    }

    @Override
    protected void configure() {

        expose(NodeLifecycle.class);
        expose(Context.class).annotatedWith(named(LOCAL));

        bindIndexContext.run();
        bindResourceContext.run();
        bindSchedulerContext.run();
        bindHandlerContext.run();
        bindTaskContext.run();
        bindManifestContext.run();
        bindEventContext.run();

        // Binding to SimpleContext to multiple expose

        bind(SimpleContext.class).asEagerSingleton();
        bind(NodeLifecycle.class).to(SimpleContext.class);
        bind(Context.class).annotatedWith(named(LOCAL)).to(SimpleContext.class);

    }

}
