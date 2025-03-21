package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.sdk.dao.IndexDao;
import dev.getelements.elements.sdk.dao.Indexable;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexPlan;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.index.IndexPlan;
import dev.getelements.elements.sdk.model.index.IndexableType;
import dev.morphia.Datastore;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.Map;

public class MongoIndexDao implements IndexDao {

    private MapperRegistry mapperRegistry;

    private MongoDBUtils mongoDBUtils;

    private Map<IndexableType, Indexable> indexableByType;

    private Provider<Indexer> indexerProvider;

    private Datastore datastore;

    @Override
    public Pagination<IndexPlan<?>> getPlans(final int offset, final int count) {
        final var query = getDatastore().find(MongoIndexPlan.class);
        return getMongoDBUtils().paginationFromQuery(query,offset, count, p -> getMapper().map(p, IndexPlan.class));
    }

    public MapperRegistry getMapper() {
        return mapperRegistry;
    }

    @Inject
    public void setMapper(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    @Override
    public void planAll() {
        getIndexableByType().values().forEach(Indexable::plan);
    }

    @Override
    public void planType(final IndexableType indexableType) {

        final var indexable = getIndexableByType().get(indexableType);

        if (indexable == null) {
            throw new InternalException("No indexer for type:" + indexableType);
        }

        indexable.plan();

    }

    @Override
    public Indexer beginIndexing() {
        return getIndexerProvider().get();
    }

    public Map<IndexableType, Indexable> getIndexableByType() {
        return indexableByType;
    }

    @Inject
    public void setIndexableByType(Map<IndexableType, Indexable> indexableByType) {
        this.indexableByType = indexableByType;
    }

    public Provider<Indexer> getIndexerProvider() {
        return indexerProvider;
    }

    @Inject
    public void setIndexerProvider(Provider<Indexer> indexerProvider) {
        this.indexerProvider = indexerProvider;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

}
