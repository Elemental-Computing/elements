package dev.getelements.elements.sdk.model.friend;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description =
        "Represents a player's friend.  This includes the basic information of the friend as well as " +
        "the friendship type, profiles he or she has across games, and "
)
public class Friend {

    @Schema(description = "The unique ID of the friendship.")
    private String id;

    @Schema(description = "The user assocaited with this particular friend.")
    private User user;

    @Schema(description = "The friendship type.")
    private Friendship friendship;

    @Schema(description = "The profiles which are associated with the friend user.")
    private List<Profile> profiles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Friendship getFriendship() {
        return friendship;
    }

    public void setFriendship(Friendship friendship) {
        this.friendship = friendship;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friend)) return false;

        Friend friend = (Friend) o;

        if (getId() != null ? !getId().equals(friend.getId()) : friend.getId() != null) return false;
        if (getUser() != null ? !getUser().equals(friend.getUser()) : friend.getUser() != null) return false;
        if (getFriendship() != friend.getFriendship()) return false;
        return getProfiles() != null ? getProfiles().equals(friend.getProfiles()) : friend.getProfiles() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getUser() != null ? getUser().hashCode() : 0);
        result = 31 * result + (getFriendship() != null ? getFriendship().hashCode() : 0);
        result = 31 * result + (getProfiles() != null ? getProfiles().hashCode() : 0);
        return result;
    }

}
