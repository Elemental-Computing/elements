package dev.getelements.elements.appnode.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.*;

public class ContextServicesModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(SimpleTaskService.class)
            .asEagerSingleton();

        bind(TaskService.class)
            .to(SimpleTaskService.class);

        bind(Scheduler.class)
            .to(SimpleScheduler.class)
            .asEagerSingleton();

        bind(RetainedHandlerService.class)
            .to(SimpleRetainedHandlerService.class)
            .asEagerSingleton();

        bind(SingleUseHandlerService.class)
            .to(SimpleSingleUseHandlerService.class)
            .asEagerSingleton();

        bind(LoadMonitorService.class)
            .to(SimpleLoadMonitorService.class)
            .asEagerSingleton();

        bind(EventService.class)
            .to(SimpleEventService.class)
            .asEagerSingleton();

        expose(Scheduler.class);
        expose(RetainedHandlerService.class);
        expose(SingleUseHandlerService.class);
        expose(TaskService.class);
        expose(EventService.class);

    }

}
