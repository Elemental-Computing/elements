package dev.getelements.elements.dao.mongo;

import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.sdk.dao.FriendDao;
import dev.getelements.elements.dao.mongo.model.MongoFriendship;
import dev.getelements.elements.dao.mongo.model.MongoFriendshipId;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.sdk.model.exception.FriendNotFoundException;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.friend.Friend;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.List;

import static dev.getelements.elements.sdk.model.friend.Friendship.*;
import static java.util.stream.Collectors.toList;

public class MongoFriendDao implements FriendDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private MongoDBUtils mongoDBUtils;

    private MapperRegistry mapperRegistry;

    private MongoUserDao mongoUserDao;

    private MongoProfileDao mongoProfileDao;

    @Override
    public Pagination<Friend> getFriendsForUser(final User user, final int offset, final int count) {

        final MongoUser mongoUser = getMongoUserDao().getMongoUser(user.getId());
        final Query<MongoFriendship> query = getDatastore().find(MongoFriendship.class);

        query.filter(Filters.and(
                Filters.or(
                        Filters.eq("_id.lesser", mongoUser.getObjectId()),
                        Filters.eq("_id.greater", mongoUser.getObjectId())
                ),
                Filters.or(
                        Filters.eq("lesserAccepted", true),
                        Filters.eq("greaterAccepted", true)
                )
        ));

        return getMongoDBUtils().paginationFromQuery(query, offset, count, f -> transform(mongoUser, f), new FindOptions());

    }

    @Override
    public Pagination<Friend> getFriendsForUser(final User user, final int offset, final int count, final String search) {
        //TODO Fix This
        return Pagination.empty();
    }

    @Override
    public Friend getFriendForUser(final User user, final String friendId) {

        final MongoUser mongoUser = getMongoUserDao().getMongoUser(user.getId());

        final MongoFriendshipId mongoFriendshipId;
        mongoFriendshipId = MongoFriendshipId.parseOrThrow(friendId, ex -> new FriendNotFoundException(ex));

        final Query<MongoFriendship> query = getDatastore().find(MongoFriendship.class);

        query.filter(Filters.and(Filters.eq("_id", mongoFriendshipId),
                Filters.or(Filters.eq("lesserAccepted", true),
                        Filters.eq("greaterAccepted", true))));

        final MongoFriendship mongoFriendship = query.first();
        return transform(mongoUser, mongoFriendship);

    }

    public List<MongoFriendship> getAllMongoFriendshipsForUser(final MongoUser mongoUser) {

        final Query<MongoFriendship> query = getDatastore().find(MongoFriendship.class);

        query.filter(Filters.and(
                Filters.or(
                        Filters.eq("_id.lesser", mongoUser.getObjectId()),
                        Filters.eq("_id.greater", mongoUser.getObjectId())
                        ),
                Filters.or(
                        Filters.eq("lesserAccepted", true),
                        Filters.eq("greaterAccepted", true)
                )
        ));

        try (var iterator = query.iterator()) {
            return iterator.toList();
        }

    }

    @Override
    public void deleteFriendForUser(final User user, final String friendId) {

        final MongoUser mongoUser = getMongoUserDao().getMongoUser(user.getId());
        final ObjectId mongoUserId = mongoUser.getObjectId();

        final MongoFriendshipId mongoFriendshipId;
        mongoFriendshipId = MongoFriendshipId.parseOrThrow(friendId, ex ->
            new FriendNotFoundException("Friend not found: " + friendId, ex));

        final Query<MongoFriendship> query = getDatastore().find(MongoFriendship.class);

        final String property;

        if (mongoFriendshipId.getLesser().equals(mongoUserId)) {
            property = "lesserAccepted";
        } else if (mongoFriendshipId.getGreater().equals(mongoUserId)) {
            property = "greaterAccepted";
        } else {
            throw new FriendNotFoundException("Friend not found: " + friendId);
        }

        query.filter(Filters.and(
                Filters.eq("_id", mongoFriendshipId),
                Filters.eq(property, true)
        ));

        final DeleteResult deleteResult = query.delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new FriendNotFoundException("Friend not found: " + friendId);
        } else if (deleteResult.getDeletedCount() > 1) {
            throw new InternalException("Deleted more rows than expected.");
        }

    }

    private Friend transform(final MongoUser mongoUser, final MongoFriendship mongoFriendship) {

        final Friend friend = getMapperRegistry().map(mongoFriendship, Friend.class);

        final ObjectId lesserObjectId = mongoFriendship.getObjectId().getLesser();
        final ObjectId greaterObjectId = mongoFriendship.getObjectId().getGreater();

        if (mongoFriendship.isLesserAccepted() && mongoFriendship.isGreaterAccepted()) {
            friend.setFriendship(MUTUAL);
        } else if (lesserObjectId.equals(mongoUser.getObjectId())) {
            friend.setFriendship(mongoFriendship.isLesserAccepted() ? OUTGOING : INCOMING);
        } else if (greaterObjectId.equals(mongoUser.getObjectId())) {
            friend.setFriendship(mongoFriendship.isGreaterAccepted() ? OUTGOING : INCOMING);
        } else {
            friend.setFriendship(NONE);
        }

        final MongoUser lesser = getDatastore().find(MongoUser.class).filter("_id.lesser", lesserObjectId).first();
        final MongoUser greater = getDatastore().find(MongoUser.class).filter("_id.greater", greaterObjectId).first();
        final MongoUser friendUser = lesser.equals(mongoUser) ? greater : lesser;

        friend.setUser(getMapperRegistry().map(friendUser, User.class));
        friend.setProfiles(getMongoProfileDao()
            .getActiveMongoProfilesForUser(friendUser)
            .map(p -> getMapperRegistry().map(p, Profile.class))
            .collect(toList()));

        return friend;

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(MapperRegistry dozerMapperRegistry) {
        this.mapperRegistry = dozerMapperRegistry;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
    }

}
