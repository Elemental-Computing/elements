package dev.getelements.elements.sdk.model.inventory;



import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Schema
public class CreateAdvancedInventoryItemRequest {

    @NotNull
    @Schema(description = "The User ID")
    private String userId;

    @NotNull
    @Schema(description = "The item to reference.")
    private String itemId;

    @Schema(description = "The quantity of the Item in inventory")
    @Min(value = 0, message = "Quantity may not be less than 0.")
    private int quantity;

    @Schema(description = "The priority slot for the item.")
    @Min(value = 0, message = "Priority must be greater than 0.")
    private int priority;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateAdvancedInventoryItemRequest that = (CreateAdvancedInventoryItemRequest) o;
        return getQuantity() == that.getQuantity() && getPriority() == that.getPriority() && Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getItemId(), that.getItemId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getItemId(), getQuantity(), getPriority());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateAdvancedInventoryItemRequest{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", itemId='").append(itemId).append('\'');
        sb.append(", quantity=").append(quantity);
        sb.append(", priority=").append(priority);
        sb.append('}');
        return sb.toString();
    }

}
