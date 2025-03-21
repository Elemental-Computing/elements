package dev.getelements.elements.sdk.model.mission;

import dev.getelements.elements.sdk.model.reward.Reward;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;


/**
 * Represents a mission step.
 *
 * Created by davidjbrooks on 11/23/2018.
 */
@Schema
public class Step implements Serializable {
    public static final String ID_TAG_PREFIX = "ID";
    public static final String TAG_SEPARATOR = ".";

    @Schema(description = "The display name for the step")
    @NotNull
    private String displayName;

    @Schema(description = "The description of the step")
    @NotNull
    private String description;

    @NotNull
    @Schema(description = "The number of times the step must be completed to receive the reward(s)")
    @Min(value = 0, message = "Count may not be less than 1")
    private Integer count;

    @NotNull
    @Schema(description = "The reward(s) that will be granted upon completion")
    private List<Reward> rewards;

    @Schema(description = "The metadata for this step")
    private Map<String, Object> metadata;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count= count;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(final String name, final Object value) {

        if (getMetadata() == null) {
            setMetadata(new HashMap<>());
        }

        getMetadata().put(name, value);

    }

    static public List<String> buildRewardIssuanceTags(Progress progress, int sequence) {
        final List <String> tags = new ArrayList<>();
        tags.add(buildIdentifyingRewardIssuanceTag(progress, sequence));

        return tags;
    }

    static public String buildIdentifyingRewardIssuanceTag(Progress progress, int sequence) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ID_TAG_PREFIX);
        stringBuilder.append(TAG_SEPARATOR);
        stringBuilder.append(progress.getId());
        stringBuilder.append(TAG_SEPARATOR);
        stringBuilder.append(sequence);

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Step)) return false;
        Step step = (Step) object;
        return Objects.equals(getDisplayName(), step.getDisplayName()) &&
                Objects.equals(getDescription(), step.getDescription()) &&
                Objects.equals(getCount(), step.getCount()) &&
                Objects.equals(getRewards(), step.getRewards()) &&
                Objects.equals(getMetadata(), step.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplayName(), getDescription(), getCount(), getRewards(), getMetadata());
    }

    @Override
    public String toString() {
        return "Step{" +
                "displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", count=" + count +
                ", rewards=" + rewards +
                ", metadata=" + metadata +
                '}';
    }

}
