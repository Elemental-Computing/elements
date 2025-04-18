package dev.getelements.elements.service.googleplayiap;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.googleplayiap.GooglePlayIapReceiptService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class GooglePlayIapReceiptServiceProvider implements Provider<GooglePlayIapReceiptService> {

    private User user;

    private Provider<UserGooglePlayIapReceiptService> userGooglePlayIapReceiptServiceProvider;

    @Override
    public GooglePlayIapReceiptService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
            case USER:
                return getUserGooglePlayIapReceiptServiceProvider().get();
            default:
                return Services.forbidden(GooglePlayIapReceiptService.class);
       }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserGooglePlayIapReceiptService> getUserGooglePlayIapReceiptServiceProvider() {
        return userGooglePlayIapReceiptServiceProvider;
    }

    @Inject
    public void setUserGooglePlayIapReceiptServiceProvider(
            Provider<UserGooglePlayIapReceiptService> userGooglePlayIapReceiptServiceProvider
    ) {
        this.userGooglePlayIapReceiptServiceProvider = userGooglePlayIapReceiptServiceProvider;
    }
}
