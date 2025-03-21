package dev.getelements.elements.sdk.model.goods;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.*;

@Schema
public class UpdateItemRequest {

    @NotNull
    @Pattern(regexp = Constants.Regexp.WHOLE_WORD_ONLY)
    private String name;

    @NotNull
    private String displayName;

    @NotNull
    private String description;

    private List<String> tags;

    private MetadataSpec metadataSpec;

    private Map<String, Object> metadata;

    private Boolean publicVisible;

    /**
     * Get the unique name of the Item
     *
     * @return The unique name for the Item
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Set the unique name for the Item
     *
     * @param name
     *     unique name for the Item
     */
    public void setName(@NotNull String name) {
        this.name = name;
    }

    /**
     * Get the tags for the Item.  The returned List should be treated as immutable.
     *
     * @return The tags for an Item, or an empty List if the Item does not have any tags.
     */
    public List<String> getTags() {
        return tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    /**
     * Sets the tags for the Item.
     *
     * @param tags
     *     The tags for the Item.
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Gets the display name for this item, suitable for use at the presentation tier.
     *
     * @return The display name for the Item
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name for this item.
     *
     * @param displayName
     *     The display name for this item
     */
    public void setDisplayName(@NotNull String displayName) {
        this.displayName = displayName;
    }


    /**
     * Gets the long description for the Item
     *
     * @return The long description for the Item
     */
    @NotNull
    public String getDescription() {
        return description;
    }

    /**
     * Sets the long description for the Item
     *
     * @param description
     *     The long description for the Item
     */
    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    /**
     * Gets the metadata spec ID.
     *
     * @return the metadata spec id
     */
    public MetadataSpec getMetadataSpec() {
        return metadataSpec;
    }

    /**
     * Sets the metadata spec ID.
     *
     * @param metadataSpec the metadata spec id
     */
    public void setMetadataSpec(MetadataSpec metadataSpec) {
        this.metadataSpec = metadataSpec;
    }

    /**
     * Gets a copy of metadata of string key-value pairs for this Item.  Changes to the returned Map are not reflected
     * on this Item.
     *
     * @return The metadata for the Item
     */
    public Map<String, Object> getMetadata() {
        return metadata == null ? new HashMap<>() : new HashMap<>(metadata);
    }

    /**
     * Sets the metadata of string key-value pairs for this Item.
     *
     * @param metadata
     *     The metadata for the Item
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : Collections.emptyMap();
    }

    public void addMetadata(final String name, final Object value) {

        if (getMetadata() == null) {
            setMetadata(new HashMap<>());
        }

        getMetadata().put(name, value);

    }

    public Boolean isPublicVisible() {
        return publicVisible;
    }

    /**
     * Sets the visibility of this Item
     *
     * @param publicVisible
     *     The boolean flag for the Item
     */
    public void setPublicVisible(Boolean publicVisible) {
        this.publicVisible = publicVisible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateItemRequest that = (UpdateItemRequest) o;
        return publicVisible == that.publicVisible && Objects.equals(name, that.name) && Objects.equals(displayName, that.displayName) && Objects.equals(description, that.description) && Objects.equals(tags, that.tags) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, displayName, description, tags, metadata, publicVisible);
    }

    @Override
    public String toString() {
        return "UpdateItemRequest{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                ", metadata=" + metadata +
                ", publicVisible=" + publicVisible +
                '}';
    }
}
