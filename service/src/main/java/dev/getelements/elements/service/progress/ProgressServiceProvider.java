package dev.getelements.elements.service.progress;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.progress.ProgressService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * Created by davidjbrooks on 12/05/2018.
 */
public class ProgressServiceProvider implements Provider<ProgressService> {

    private User user;

    private Provider<UserProgressService> userProgressServiceProvider;

    private Provider<SuperUserProgressService> superUserProgressServiceProvider;

    @Override
    public ProgressService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserProgressServiceProvider().get();
            case USER:
                return getUserProgressServiceProvider().get();
            default:
                return Services.forbidden(ProgressService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserProgressService> getSuperUserProgressServiceProvider() {
        return superUserProgressServiceProvider;
    }

    @Inject
    public void setSuperUserProgressServiceProvider(Provider<SuperUserProgressService> userApplicationServiceProvider) {
        this.superUserProgressServiceProvider = userApplicationServiceProvider;
    }

    public Provider<UserProgressService> getUserProgressServiceProvider() {
        return userProgressServiceProvider;
    }

    @Inject
    public void setUserProgressServiceProvider(Provider<UserProgressService> userApplicationServiceProvider) {
        this.userProgressServiceProvider = userApplicationServiceProvider;
    }

}
