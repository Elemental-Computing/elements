package dev.getelements.elements.rt.jersey;

import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Singleton
@Consumes("multipart/*")
public class GenericMultipartReader implements MessageBodyReader<Collection<?>> {

    private static final Logger logger = LoggerFactory.getLogger(GenericMultipartReader.class);

    private static final TemporaryFiles temporary = new TemporaryFiles(GenericMultipartReader.class);

    private final Providers providers;

    public GenericMultipartReader(final @Context Providers providers) {
        this.providers = providers;
    }

    @Override
    public boolean isReadable(final Class<?> type,
                              final Type genericType,
                              final Annotation[] annotations,
                              final MediaType mediaType) {
        return Collection.class.isAssignableFrom(type);
    }

    @Override
    public Collection<?> readFrom(
            final Class<Collection<?>> type,
            final Type genericType,
            final Annotation[] annotations,
            final MediaType mediaType,
            final MultivaluedMap<String, String> httpHeaders,
            final InputStream entityStream)
            throws IOException, WebApplicationException {

        final var delegate = providers.getMessageBodyReader(
            MultiPart.class,
            MultiPart.class,
            annotations,
            mediaType);

        final var multipart = delegate.readFrom(
                MultiPart.class,
                null,
                new Annotation[]{},
                mediaType,
                httpHeaders,
                entityStream
        );

        return multipart.getBodyParts()
            .stream()
            .map(this::toMap)
            .collect(toList());

    }

    private Map<String, Object> toMap(final BodyPart bodyPart) {

        final var map = new LinkedHashMap<String, Object>();
        final var type = bodyPart.getMediaType();
        final var disposition = bodyPart.getContentDisposition();
        final var entity = bodyPart.getEntity();
        final Function<Class<?>, Object> reader = bodyPart::getEntityAs;

        map.put(GenericMultipartFeature.TYPE, type.toString());
        map.put(GenericMultipartFeature.READER, reader);

        if (disposition != null) {
            map.put(GenericMultipartFeature.DISPOSITION, disposition.toString());
        }

        if (entity != null) {
            map.put(GenericMultipartFeature.ENTITY, entity);
        }

        return map;

    }

}
