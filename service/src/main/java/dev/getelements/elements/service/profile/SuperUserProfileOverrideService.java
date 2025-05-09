package dev.getelements.elements.service.profile;

import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.profile.Profile;

import dev.getelements.elements.sdk.service.profile.ProfileOverrideService;
import jakarta.inject.Inject;
import java.util.Optional;

/**
 * Implementation for users with {@link User.Level#SUPERUSER} users.
 */
public class SuperUserProfileOverrideService implements ProfileOverrideService {

    private ProfileDao profileDao;

    /**
     * Returns the profile, regardless of ownership.
     *
     * @param profileId the profile ID
     * @return the profile or null, if it doesn't exist.
     */
    @Override
    public Optional<Profile> findOverrideProfile(final String profileId) {
        return getProfileDao().findActiveProfile(profileId);
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

}
