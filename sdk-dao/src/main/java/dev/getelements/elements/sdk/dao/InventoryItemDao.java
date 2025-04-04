package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Created by davidjbrooks on 11/11/18.
 */

@ElementServiceExport
public interface InventoryItemDao {

    /**
     * The priority for use by the {@link InventoryItemDao}
     */
    int SIMPLE_PRIORITY = 0;

    /**
     * Gets the specific inventory item with the id, or throws a {@link NotFoundException} if the
     * inventory item can't be found.
     *
     * @return the {@link InventoryItem} that was requested, never null
     */
    InventoryItem getInventoryItem(String inventoryItemId);

    /**
     * Gets inventory items specifying the offset and the count.
     *
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link InventoryItem} objects.
     */
    Pagination<InventoryItem> getInventoryItems(int offset, int count);

    /**
     * Gets inventory items for specified user, which are flagged publicVisible
     *
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link InventoryItem} objects.
     */
    Pagination<InventoryItem> getUserPublicInventoryItems(int offset, int count, User user);

    /**
     * Gets inventory items specifying the offset and the count, specifying a search filter.
     *
     * @param offset the offset
     * @param count  the count
     * @param user   the {@link User} that owns the items
     * @param search a query to filter the results
     * @return a {@link Pagination} of {@link InventoryItem} objects.
     */
    Pagination<InventoryItem> getInventoryItems(int offset, int count, User user, String search);

    /**
     * Gets the primary (single) inventory item for with the item name or id, or throws a {@link NotFoundException}
     * if the item or inventory item can't be found.
     *
     * @param user         the {@link User} that owns the item
     * @param itemNameOrId an item name or ID to limit the results
     * @param priority
     * @return the {@link InventoryItem} that was requested, never null
     */
    InventoryItem getInventoryItemByItemNameOrId(User user, String itemNameOrId, int priority);

    /**
     * Updates the specific inventory item with the id, or throws a {@link NotFoundException} if the
     * inventory item can't be found.  The {@link InventoryItem#getId()} is used to key the inventory item being updated.
     *
     * @param inventoryItemId
     * @param quantity
     * @return the {@link InventoryItem} as it was written into the database
     * @throws InvalidDataException if the state of the passed in InventoryItem is invalid
     */
    InventoryItem updateInventoryItem(String inventoryItemId, int quantity);

    /**
     * Creates an inventory item.  The value of {@link InventoryItem#getId()} will be ignored.
     *
     * @return the {@link InventoryItem} as it was written into the database
     * @throws InvalidDataException if the state of the passed in InventoryItem is invalid
     * @throws DuplicateException   if the passed in Item has a name that already exists
     */
    InventoryItem createInventoryItem(InventoryItem inventoryItem);

    /**
     * Adjusts the quantity of the supplied item and user.
     *
     * @param user         the {@link User} for which to adjust the item.
     * @param itemNameOrId the {@link Item#getName()} or {@link Item#getId()}
     * @param priority     the priority of the item slot
     * @param quantity     the amount to adjust the quantity by
     * @return the updated {@link InventoryItem}
     */
    InventoryItem setQuantityForItem(User user, String itemNameOrId, int priority, int quantity);

    /**
     * Adjusts the quantity of the supplied item and user.
     *
     * @param inventoryItemId the {@link Item#getName()} or {@link Item#getId()}
     * @param quantityDelta   the amount to adjust the quantity by
     * @return the updated {@link InventoryItem}
     */
    InventoryItem adjustQuantityForItem(String inventoryItemId, int quantityDelta);

    /**
     * Convenience method which allows for invoking {@link #adjustQuantityForItem(String, int)} without needing to
     * create the item first.
     *
     * @param user          the user object to adjust
     * @param itemNameOrId  the item name or identifier
     * @param priority      the priority slot
     * @param quantityDelta the quantity delta
     * @return the updated {@link InventoryItem}
     */
    InventoryItem adjustQuantityForItem(User user, String itemNameOrId, int priority, int quantityDelta);

    /**
     * Deletes an inventory item.
     *
     * @param inventoryItemId the {@link InventoryItem}'s id.
     */
    void deleteInventoryItem(String inventoryItemId);

}
