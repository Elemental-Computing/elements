package dev.getelements.elements.dao.mongo.auth;

import dev.getelements.elements.sdk.dao.AuthSchemeDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.auth.MongoAuthScheme;
import dev.getelements.elements.sdk.model.exception.auth.AuthSchemeNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.auth.AuthScheme;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.in;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.util.stream.Collectors.toList;

public class MongoAuthSchemeDao implements AuthSchemeDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapperRegistry;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<AuthScheme> getAuthSchemes(final int offset,
                                                 final int count,
                                                 final List<String> tags) {

        final var mongoQuery = getDatastore().find(MongoAuthScheme.class);

        if (tags != null && !tags.isEmpty()) {
            mongoQuery.filter(in("tags", tags));
        }

        return getMongoDBUtils().paginationFromQuery(mongoQuery, offset, count, this::transform, new FindOptions());

    }

    @Override
    public Optional<AuthScheme> findAuthScheme(final String authSchemeId) {

        final var objectId = getMongoDBUtils().parseOrThrow(authSchemeId, AuthSchemeNotFoundException::new);

        var mongoAuthScheme = getDatastore().find(MongoAuthScheme.class)
                .filter(eq("_id", objectId))
                .first();

        return Optional.ofNullable(mongoAuthScheme).map(this::transform);

    }

    @Override
    public List<AuthScheme> getAuthSchemesByAudience(final List<String> audience) {

        final var mongoQuery = getDatastore()
            .find(MongoAuthScheme.class)
            .filter(in("audience", audience));

        final var options = new FindOptions();

        try (final var iterator = mongoQuery.iterator(options)) {
            return iterator
                .toList()
                .stream()
                .map(this::transform)
                .collect(toList());
        }

    }

    @Override
    public AuthScheme createAuthScheme(final AuthScheme authScheme) {
        getValidationHelper().validateModel(authScheme, ValidationGroups.Insert.class);
        final var mongoAuthScheme = getBeanMapper().map(authScheme, MongoAuthScheme.class);
        final var result = getMongoDBUtils().perform(ds -> getDatastore().save(mongoAuthScheme));
        return transform(result);
    }

    @Override
    public AuthScheme updateAuthScheme(final AuthScheme authScheme) {

        getValidationHelper().validateModel(authScheme, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrow(
            authScheme.getId(),
            AuthSchemeNotFoundException::new
        );

        final var query = getDatastore().find(MongoAuthScheme.class);
        query.filter(eq("_id", objectId));

        final var builder = new UpdateBuilder();
        builder.with(set("tags", authScheme.getTags()));
        builder.with(set("audience", authScheme.getAudience()));
        builder.with(set("publicKey", authScheme.getPublicKey()));
        builder.with(set("algorithm", authScheme.getAlgorithm()));
        builder.with(set("userLevel", authScheme.getUserLevel()));
        builder.with(set("allowedIssuers", authScheme.getAllowedIssuers()));

        final MongoAuthScheme mongoAuthScheme = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoAuthScheme == null) {
            throw new AuthSchemeNotFoundException("Auth scheme not found: " + authScheme.getId());
        }

        return transform(mongoAuthScheme);

    }

    @Override
    public void deleteAuthScheme(final String authSchemeId) {

        final var objectId = getMongoDBUtils().parseOrThrow(authSchemeId, AuthSchemeNotFoundException::new);

        final var result = getDatastore()
            .find(MongoAuthScheme.class)
            .filter(eq("_id", objectId))
            .delete();

        if (result.getDeletedCount() == 0) {
            throw new AuthSchemeNotFoundException("Auth scheme not found: " + authSchemeId);
        }

    }

    private AuthScheme transform(MongoAuthScheme mongoAuthScheme) {
        return getBeanMapper().map(mongoAuthScheme, AuthScheme.class);
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public MapperRegistry getBeanMapper() {
        return beanMapperRegistry;
    }

    @Inject
    public void setBeanMapper(MapperRegistry beanMapperRegistry) {
        this.beanMapperRegistry = beanMapperRegistry;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

}
