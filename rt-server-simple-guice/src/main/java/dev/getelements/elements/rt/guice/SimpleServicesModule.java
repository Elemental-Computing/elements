//package dev.getelements.elements.rt.guice;
//
//import com.google.getInstance.PrivateModule;
//import com.google.getInstance.TypeLiteral;
//import dev.getelements.elements.rt.*;
//import dev.getelements.elements.rt.id.ResourceId;
//
//import java.util.Deque;
//
//import static dev.getelements.elements.rt.PersistenceStrategy.getNullPersistence;
//
///**
// * Creates the simple internal
// * <p>
// * <p>
// * Created by patricktwohig on 9/22/15.
// */
//public class SimpleServicesModule extends PrivateModule {
//
//    @Override
//    protected void configure() {
//
//        bind(Scheduler.class).to(SimpleScheduler.class).asEagerSingleton();
//        bind(ResourceLockService.class).to(SimpleResourceLockService.class).asEagerSingleton();
//        bind(ResourceService.class).to(SimpleResourceService.class).asEagerSingleton();
//        bind(RetainedHandlerService.class).to(SimpleRetainedHandlerService.class).asEagerSingleton();
//        bind(SingleUseHandlerService.class).to(SimpleSingleUseHandlerService.class).asEagerSingleton();
//        bind(LoadMonitorService.class).to(SimpleLoadMonitorService.class).asEagerSingleton();
//        bind(TaskService.class).to(SimpleTaskService.class);
//        bind(PersistenceStrategy.class).toInstance(getNullPersistence());
//
//        bind(new TypeLiteral<OptimisticLockService<Deque<Path>>>() {})
//            .toProvider(() -> new ProxyLockService<>(Deque.class));
//
//        bind(new TypeLiteral<OptimisticLockService<ResourceId>>() {})
//            .to(SimpleResourceIdOptimisticLockService.class);
//
//        expose(Scheduler.class);
//        expose(ResourceService.class);
//        expose(RetainedHandlerService.class);
//        expose(SingleUseHandlerService.class);
//        expose(PersistenceStrategy.class);
//        expose(TaskService.class);
//
//    }
//
//}
