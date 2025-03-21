package dev.getelements.elements.sdk.model.profile;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

@Schema(description = "Represents a request to create a profile for a user.")
public class CreateProfileRequest {

    @NotNull
    @Schema(description = "The user id this profile belongs to.")
    private String userId;

    @NotNull
    @Schema(description = "The application id this profile belongs to.")
    private String applicationId;

    /** @deprecated use separate call to create image largeObject*/
    @Deprecated
    @Schema(description = "A URL to the image of the profile.  (ie the User's Avatar).")
    private String imageUrl;

    @Schema(description = "A non-unique display name for this profile.")
    private String displayName;

    @Schema(description = "A map of arbitrary metadata.")
    private Map<String, Object> metadata;

    /**
     * @deprecated
     * Providing the entire {@link User} object is no longer necessary.
     * Provide {@link #userId} instead
     */
    @Deprecated
    @Schema(hidden = true)
    private User user;

    /**
     * @deprecated
     * Providing the entire {@link Application} object is no longer necessary.
     * Provide {@link #applicationId} instead
     */
    @Deprecated
    @Schema(hidden = true)
    private Application application;

    /**
     * @deprecated
     * Providing ID is no longer necessary.
     */
    @Deprecated
    @Schema(hidden = true)
    private String id;

    /**
     * @deprecated
     * Providing lastLogin is no longer necessary.
     */
    @Deprecated
    @Schema(hidden = true)
    private String lastLogin;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Deprecated
    public void setUser(User user){
        this.user = user;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Application getApplication() {
        return application;
    }

    @Deprecated
    public void setApplication(Application application){
        this.application = application;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateProfileRequest that = (CreateProfileRequest) o;
        return Objects.equals(userId, that.userId) && Objects.equals(applicationId, that.applicationId) && Objects.equals(imageUrl, that.imageUrl) && Objects.equals(displayName, that.displayName) && Objects.equals(metadata, that.metadata) && Objects.equals(user, that.user) && Objects.equals(application, that.application) && Objects.equals(id, that.id) && Objects.equals(lastLogin, that.lastLogin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, applicationId, imageUrl, displayName, metadata, user, application, id, lastLogin);
    }

    @Override
    public String toString() {
        return "CreateProfileRequest{" +
                "userId='" + userId + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", displayName='" + displayName + '\'' +
                ", metadata=" + metadata +
                ", user=" + user +
                ", application=" + application +
                ", id='" + id + '\'' +
                ", lastLogin='" + lastLogin + '\'' +
                '}';
    }
}
