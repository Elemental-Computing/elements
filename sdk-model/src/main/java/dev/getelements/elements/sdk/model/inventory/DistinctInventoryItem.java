package dev.getelements.elements.sdk.model.inventory;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@Schema
public class DistinctInventoryItem implements Serializable {

    @Null(groups = {ValidationGroups.Create.class, Insert.class})
    @NotNull(groups = ValidationGroups.Update.class)
    @Schema(description = "The unique ID of the inventory item itself.")
    private String id;

    @NotNull(groups = {Insert.class})
    private Item item;

    @NotNull(groups = {Insert.class})
    private User user;

    private Profile profile;

    private Map<String, Object> metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DistinctInventoryItem that = (DistinctInventoryItem) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getItem(), that.getItem()) && Objects.equals(getUser(), that.getUser()) && Objects.equals(getProfile(), that.getProfile()) && Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getItem(), getUser(), getProfile(), getMetadata());
    }

}
