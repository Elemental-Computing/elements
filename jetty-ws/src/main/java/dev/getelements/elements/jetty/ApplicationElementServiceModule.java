package dev.getelements.elements.jetty;

import com.google.inject.PrivateModule;
import dev.getelements.elements.common.app.ApplicationElementService;
import dev.getelements.elements.common.app.StandardApplicationElementService;

import java.util.List;

import static java.util.List.copyOf;

public class ApplicationElementServiceModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(ApplicationElementService.class);

        bind(ApplicationElementService.class)
                .to(StandardApplicationElementService.class)
                .asEagerSingleton();

    }

}
