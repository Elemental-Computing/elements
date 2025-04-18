package dev.getelements.elements.rt.jackson.guice;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import dev.getelements.elements.rt.PayloadReader;
import dev.getelements.elements.rt.PayloadWriter;
import dev.getelements.elements.rt.jackson.ObjectMapperPayloadReader;
import dev.getelements.elements.rt.jackson.ObjectMapperPayloadWriter;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;

public class ObjectMapperPayloadReaderWriterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PayloadWriter.class).to(ObjectMapperPayloadWriter.class);
        bind(PayloadReader.class).to(ObjectMapperPayloadReader.class);
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper() {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping();
        objectMapper.enableDefaultTyping(NON_FINAL);
        objectMapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        return objectMapper;

    }

}
