package dev.getelements.elements.sdk.model.user;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Objects;

@Schema
public class UserUpdatePasswordRequest {

    @NotNull
    @Schema(description = "The user's current password.")
    private String oldPassword;

    @NotNull
    @Schema(description = "The user's updated password.")
    private String newPassword;

    @Schema(description = "The user's profile id to assign the new session. Leave blank for no profile id.")
    private String profileId;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserUpdatePasswordRequest that = (UserUpdatePasswordRequest) o;
        return Objects.equals(getOldPassword(), that.getOldPassword()) && Objects.equals(getNewPassword(), that.getNewPassword()) && Objects.equals(getProfileId(), that.getProfileId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOldPassword(), getNewPassword(), getProfileId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserUpdatePasswordRequest{");
        sb.append("oldPassword='").append(oldPassword).append('\'');
        sb.append(", newPassword='").append(newPassword).append('\'');
        sb.append(", profileId='").append(profileId).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
