package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.DistinctInventoryItemDao;
import dev.getelements.elements.sdk.model.exception.inventory.DistinctInventoryItemNotFoundException;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.getelements.elements.sdk.model.goods.ItemCategory.DISTINCT;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoDistinctInventorItemDaoTest {

    private static final int ITEM_COUNT = 5;

    private static final int PUBLIC_ITEM_COUNT = 2;

    private static final int USER_COUNT = 5;

    private static final int PROFILE_COUNT = 5;

    @Inject
    private DistinctInventoryItemDao underTest;

    @Inject
    private ItemTestFactory itemTestFactory;

    @Inject
    private UserTestFactory userTestFactory;

    @Inject
    private ProfileTestFactory profileTestFactory;

    @Inject
    private ApplicationTestFactory applicationTestFactory;

    private List<Item> itemList;

    private List<Item> publicItemList;

    private List<User> userList;

    private User userWithPublicItems;

    private List<Profile> profileList;

    private final Map<Object, DistinctInventoryItem> intermediates = new ConcurrentHashMap<>();

    @BeforeClass
    public void setup() {

        var itemList = new ArrayList<Item>();
        var publicItemList = new ArrayList<Item>();
        var userList = new ArrayList<User>();
        var profileList = new ArrayList<Profile>();

        final var application = applicationTestFactory.createMockApplication("Distinct Items");

        for (int i = 0; i < ITEM_COUNT; ++i) {
            final var item = itemTestFactory.createTestItem(DISTINCT, false);
            itemList.add(item);
        }
        for (int i = 0; i < PUBLIC_ITEM_COUNT; ++i) {
            final var publicItem = itemTestFactory.createTestItem(DISTINCT, true);
            publicItemList.add(publicItem);
        }

        for (int i = 0; i < USER_COUNT; ++i) {

            final var user = userTestFactory.createTestUser();
            userList.add(user);

            for (int j = 0; j < PROFILE_COUNT; ++j) {
                final var profile = profileTestFactory.makeMockProfile(user, application);
                profileList.add(profile);
            }
        }

        userWithPublicItems = userTestFactory.createTestUser();

        // Immutable for thread safety
        this.itemList = unmodifiableList(itemList);
        this.userList = unmodifiableList(userList);
        this.profileList = unmodifiableList(profileList);
        this.publicItemList = unmodifiableList(publicItemList);

    }

    @DataProvider
    public Object[][] getIntermediates() {
        return intermediates
                .entrySet()
                .stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] getDistinctItemsToCreate() {

        final var output = new ArrayList<Object[]>();

        for (var item : itemList) {

            // User items with no metadata
            for (var user : userList) {
                final var toCreate = new DistinctInventoryItem();
                toCreate.setUser(user);
                toCreate.setItem(item);
                output.add(new Object[]{toCreate});
            }

            // User items with metadata
            for (var user : userList) {
                final var toCreate = new DistinctInventoryItem();
                toCreate.setUser(user);
                toCreate.setItem(item);
                toCreate.setMetadata(generateMockMetadata());
                output.add(new Object[]{toCreate});
            }

            // Profile items with no metadata
            for (var profile : profileList) {
                final var toCreate = new DistinctInventoryItem();
                toCreate.setProfile(profile);
                toCreate.setUser(profile.getUser());
                toCreate.setItem(item);
                output.add(new Object[]{toCreate});
            }

            // Profile items with metadata
            for (var profile : profileList) {
                final var toCreate = new DistinctInventoryItem();
                toCreate.setProfile(profile);
                toCreate.setUser(profile.getUser());
                toCreate.setItem(item);
                toCreate.setMetadata(generateMockMetadata());
                output.add(new Object[]{toCreate});
            }

        }

        for (var publicItem : publicItemList) {

            final var toCreate = new DistinctInventoryItem();
            toCreate.setUser(userWithPublicItems);
            toCreate.setItem(publicItem);
            output.add(new Object[]{toCreate});

            for (var profile : profileList) {
                final var distinctToCreate = new DistinctInventoryItem();
                distinctToCreate.setProfile(profile);
                distinctToCreate.setUser(profile.getUser());
                distinctToCreate.setItem(publicItem);
                output.add(new Object[]{distinctToCreate});
            }

        }

        return output.toArray(Object[][]::new);

    }

    private void saveIntermediate(final DistinctInventoryItem item) {
        intermediates.put(item.getId(), item);
    }

    private Map<String, Object> generateMockMetadata() {
        return Map.of(
                randomUUID().toString(), randomUUID().toString(),
                randomUUID().toString(), randomUUID().toString(),
                randomUUID().toString(), randomUUID().toString(),
                randomUUID().toString(), randomUUID().toString()
        );
    }

    @Test(expectedExceptions = DistinctInventoryItemNotFoundException.class)
    public void testGetNonsense() {
        underTest.getDistinctInventoryItem("asdf");
    }

    @Test(expectedExceptions = DistinctInventoryItemNotFoundException.class)
    public void testGetNotFound() {
        underTest.getDistinctInventoryItem(new ObjectId().toHexString());
    }

    @Test(dataProvider = "getDistinctItemsToCreate")
    public void testCreateDistinctUserInventoryItem(final DistinctInventoryItem toCreate) {
        final var created = underTest.createDistinctInventoryItem(toCreate);
        assertNotNull(created.getId());
        assertEquals(created.getItem(), toCreate.getItem());
        assertEquals(created.getUser(), toCreate.getUser());
        assertEquals(created.getMetadata(), toCreate.getMetadata());
        saveIntermediate(created);
    }

    @Test(dependsOnMethods = "testCreateDistinctUserInventoryItem")
    public void testGetPublicItems() {
        final var all = new PaginationWalker().toList((offset, count) ->
                underTest.getDistinctInventoryItems(offset, count, userWithPublicItems.getId(), null, true)
        );
        assertEquals(all.size(), PUBLIC_ITEM_COUNT);
        assertTrue(intermediates.values().containsAll(all));
    }

    @Test(dependsOnMethods = "testCreateDistinctUserInventoryItem")
    public void testGetAllItems() {
        final var items = new PaginationWalker().toList((offset, count) ->
                underTest.getDistinctInventoryItems(offset, count, null, null, false)
        );
        assertTrue(items.containsAll(intermediates.values()));
    }

    @Test(dataProvider = "getIntermediates", dependsOnMethods = "testCreateDistinctUserInventoryItem")
    public void testGetSingleDistinctInventoryItem(final String owner, final DistinctInventoryItem item) {
        final var fetched = underTest.getDistinctInventoryItem(item.getId());
        assertEquals(fetched, item);
    }

    @Test(dataProvider = "getIntermediates", dependsOnMethods = "testCreateDistinctUserInventoryItem")
    public void testFindDistinctInventoryItemByOwnerAndId(final String id, final DistinctInventoryItem item) {

        final var owner = item.getProfile() == null ? item.getUser().getId() : item.getProfile().getId();

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        final var fetched = underTest
                .findDistinctInventoryItemForOwner(id, owner)
                .get();

        assertEquals(fetched, item);

    }

    @Test
    public void testFindDistinctInventoryItemByOwnerAndIdNotFound() {

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        final var fetched = underTest.findDistinctInventoryItemForOwner(
                new ObjectId().toHexString(),
                new ObjectId().toHexString()
        );

        assertNotNull(fetched);
        assertTrue(fetched.isEmpty());

    }

    @Test
    public void testFindDistinctInventoryItemByOwnerAndIdNotFoundWithBadId() {
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        final var fetched = underTest.findDistinctInventoryItemForOwner("asdf","asdf");
        assertNotNull(fetched);
        assertTrue(fetched.isEmpty());
    }

    @Test(dataProvider = "getIntermediates",
            dependsOnMethods = {
                    "testGetAllItems",
                    "testGetSingleDistinctInventoryItem",
                    "testFindDistinctInventoryItemByOwnerAndId"
            })
    public void testUpdate(final String owner, final DistinctInventoryItem item) {
        final var toUpdate = new DistinctInventoryItem();
        toUpdate.setId(item.getId());
        toUpdate.setUser(item.getUser());
        toUpdate.setItem(item.getItem());
        toUpdate.setProfile(item.getProfile());

        final Map<String, Object> metadata;

        if (toUpdate.getMetadata() == null) {
            metadata = generateMockMetadata();
        } else {
            metadata = null;
        }

        toUpdate.setMetadata(metadata);

        final var updated = underTest.updateDistinctInventoryItem(toUpdate);
        assertEquals(updated.getItem(), toUpdate.getItem());
        assertEquals(updated.getUser(), toUpdate.getUser());
        assertEquals(updated.getProfile(), toUpdate.getProfile());
        assertEquals(updated.getMetadata(), metadata);
        saveIntermediate(updated);

    }

    @Test(dataProvider = "getIntermediates",
            dependsOnMethods = {"testUpdate"})
    public void testDelete(final String owner, final DistinctInventoryItem item) {
        underTest.deleteDistinctInventoryItem(item.getId());
    }

    @Test(dataProvider = "getIntermediates",
            dependsOnMethods = {"testDelete"},
            expectedExceptions = DistinctInventoryItemNotFoundException.class)
    public void testDoubleDelete(final String owner, final DistinctInventoryItem item) {
        underTest.deleteDistinctInventoryItem(item.getId());
    }

    @Test(dataProvider = "getIntermediates",
            dependsOnMethods = {"testDelete"},
            expectedExceptions = DistinctInventoryItemNotFoundException.class)
    public void testFetchPostDelete(final String owner, final DistinctInventoryItem item) {
        underTest.getDistinctInventoryItem(item.getId());
    }

    @Test()
    public void testCountDistinctMetadataField() {
        User userWithMatadataItems = userTestFactory.createTestUser();
        var itemWithMetadata1 = new DistinctInventoryItem();
        var itemWithMetadata2 = new DistinctInventoryItem();
        itemWithMetadata1.setUser(userWithMatadataItems);
        itemWithMetadata2.setUser(userWithMatadataItems);
        Map<String, Object> metadata1 = new HashMap<>();
        Map<String, Object> metadata2 = new HashMap<>();

        metadata1.put("country", "USA");
        metadata1.put("city", "City1");

        metadata2.put("country", "USA");
        metadata2.put("city", "City2");
        metadata2.put("book", "StrangBook");

        itemWithMetadata1.setMetadata(metadata1);
        itemWithMetadata2.setMetadata(metadata2);
        itemWithMetadata1.setItem(itemTestFactory.createTestItem(DISTINCT, true));
        itemWithMetadata2.setItem(itemTestFactory.createTestItem(DISTINCT, true));

        underTest.createDistinctInventoryItem(itemWithMetadata1);
        underTest.createDistinctInventoryItem(itemWithMetadata2);

        Long expectedTwoDisctinctCity = underTest.countDistinctMetadataField(null,"city");
        Long expectedOneDisctinctCountry = underTest.countDistinctMetadataField(null,"country");
        Long expectedOneDisctinctBook= underTest.countDistinctMetadataField(null,"book");

        assertEquals(expectedTwoDisctinctCity.longValue(), 2);
        assertEquals(expectedOneDisctinctCountry.longValue(), 1);
        assertEquals(expectedOneDisctinctBook.longValue(), 1);

    }

}
