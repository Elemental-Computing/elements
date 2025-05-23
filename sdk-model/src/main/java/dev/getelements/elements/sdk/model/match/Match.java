package dev.getelements.elements.sdk.model.match;

import dev.getelements.elements.sdk.model.ValidationGroups.Create;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.profile.Profile;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Created by patricktwohig on 7/18/17.
 */
@Schema(description =
        "Represents a single one-on-one match between the current player and an opponent.  Once " +
        "matched, the player will will be able to create a game against the supplied opposing player.  The " +
        "server may modify or delete matches based on a variety of circumstances.")
public class Match implements Serializable {

    public static final String ROOT_TOPIC = "match";

    @Null(groups = Create.class)
    @Schema(description = "The unique ID of the match.")
    private String id;

    @NotNull(groups = {Create.class, Insert.class})
    @Schema(description = "The scheme to use when matching with other players.")
    private String scheme;

    @Schema(description = "An optional scope for the match.  For example, if the match were part of a tournament, " +
                      "it could be scoped to the unique ID of the tournament.")
    private String scope;

    @NotNull(groups = {Insert.class, Update.class})
    @Schema(description = "The player requesting the match.  If not specified, then the current profile will be inferred.")
    private Profile player;

    @Null(groups = {Create.class, Insert.class})
    @Schema(description = "The opposing player, or null if no suitable opponent has been found.")
    private Profile opponent;

    @Null(groups = {Create.class, Insert.class})
    @Schema(description = "The time of the last modification of the match.")
    private Long lastUpdatedTimestamp;

    @Null(groups = {Create.class, Insert.class})
    @Schema(description = "The system-assigned game ID of the match.  Null until the match is successfully made.")
    private String gameId;

    @Schema(description = "Additional arbitrary metadata that is attached to the match.")
    private Map<String, Object> metadata;

    /**
     * Gets the unique server-assigned ID of this match.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique server-assigned ID of this match.
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the desired matchmaking scheme.
     *
     * @return the desired matchmaking scheme.
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Sets the desired matchmaking scheme.
     * @param scheme
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Gets the scope of the match.
     *
     * @return the scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the scope of the scheme.
     *
     * @param scope the scope
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Gets the {@link Profile} of the player requesting the match.
     *
     * @return the player's {@link Profile}
     */
    public Profile getPlayer() {
        return player;
    }

    /**
     * Sets the {@link Profile} of the player requesting the match.
     *
     * @param player the player
     */
    public void setPlayer(Profile player) {
        this.player = player;
    }

    /**
     * The {@link Profile} belonging to the opponent.
     *
     * @return the opponent
     */
    public Profile getOpponent() {
        return opponent;
    }

    /**
     * Sets the {@link Profile} belonging to the opponent.
     *
     * @param opponent the opponent
     */
    public void setOpponent(Profile opponent) {
        this.opponent = opponent;
    }

    /**
     * Gets the date at which the last modification was made to this match.
     *
     * @return the last-updated date
     */
    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    /**
     * Sets the date at which the last modification was made to this match.
     *
     * @param lastUpdatedTimestamp the last-updated date
     */
    public void setLastUpdatedTimestamp(Long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    /**
     * Gets the system assigned game ID for the match.
     *
     * @return the system-assigned game id
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Sets the system assigned game ID for the match.
     * @param gameId
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    /**
     * Gets the metdata associated with the match.
     *
     * @return the metadata associated with the match
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata associated with the match.
     * @param metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Match)) return false;
        Match match = (Match) object;
        return Objects.equals(getId(), match.getId()) &&
                Objects.equals(getScheme(), match.getScheme()) &&
                Objects.equals(getScope(), match.getScope()) &&
                Objects.equals(getPlayer(), match.getPlayer()) &&
                Objects.equals(getOpponent(), match.getOpponent()) &&
                Objects.equals(getLastUpdatedTimestamp(), match.getLastUpdatedTimestamp()) &&
                Objects.equals(getGameId(), match.getGameId()) &&
                Objects.equals(getMetadata(), match.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getId(),
            getScheme(),
            getScope(),
            getPlayer(),
            getOpponent(),
            getLastUpdatedTimestamp(),
            getGameId(),
            getMetadata());
    }

    @Override
    public String toString() {
        return "Match{" +
                "id='" + id + '\'' +
                ", scheme='" + scheme + '\'' +
                ", scope='" + scope + '\'' +
                ", player=" + player +
                ", opponent=" + opponent +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", gameId='" + gameId + '\'' +
                ", metadata=" + metadata +
                '}';
    }

}
