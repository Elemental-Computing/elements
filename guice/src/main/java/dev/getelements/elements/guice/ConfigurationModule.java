package dev.getelements.elements.guice;

import com.google.common.base.Splitter;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.sdk.model.util.RoundRobin;
import org.nnsoft.guice.rocoto.converters.FileConverter;
import org.nnsoft.guice.rocoto.converters.URIConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.function.Supplier;

import static com.google.inject.TypeLiteral.get;
import static com.google.inject.matcher.Matchers.only;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.bindProperties;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.model.Constants.CORS_ALLOWED_ORIGINS;

/**
 * Created by patricktwohig on 4/3/15.
 */
public class ConfigurationModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationModule.class);

    private final Supplier<Properties> propertiesSupplier;

    public ConfigurationModule(final Supplier<Properties> propertiesSupplier) {
        this.propertiesSupplier = propertiesSupplier;
    }

    @Override
    protected void configure() {

        install(new URIConverter());
        install(new FileConverter());
        convertToTypes(only(get(Path.class)), (s, to) -> Paths.get(s));

        final Properties properties = propertiesSupplier.get();
        if (properties == null) addError("Supplier supplied null properties.");

        bind(Properties.class).toProvider(() -> new Properties(properties));

        logger.debug("Using configuration properties {} from {}", properties, propertiesSupplier.getClass().getName());
        bindProperties(binder(), properties);

        final Multibinder<URI> corsAllowedOriginsMultibinder;
        corsAllowedOriginsMultibinder = newSetBinder(binder(), URI.class, named(CORS_ALLOWED_ORIGINS));

        final String corsAllowedOriginsProperty = properties.getProperty(CORS_ALLOWED_ORIGINS, "");

        final Iterable<String> corsAllowedOrigins = Splitter
            .on(",")
            .trimResults()
            .omitEmptyStrings()
            .split(corsAllowedOriginsProperty);

        for (final String origin : corsAllowedOrigins) {
            try {
                corsAllowedOriginsMultibinder.addBinding().toInstance(new URI(origin));
            } catch (URISyntaxException e) {
                binder().addError(e);
            }
        }

    }

}
