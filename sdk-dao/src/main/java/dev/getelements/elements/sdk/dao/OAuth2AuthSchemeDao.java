package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.auth.AuthSchemeNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.*;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;
import java.util.Optional;

@ElementServiceExport
public interface OAuth2AuthSchemeDao {

    /**
     * Lists all {@link AuthScheme} instances
     *
     * @param offset
     * @param count
     * @param tags
     * @return a {@link Pagination} of {@link AuthScheme} instances
     */
    Pagination<OAuth2AuthScheme> getAuthSchemes(int offset, int count, List<String> tags);

    /**
     * Finds an {@link AuthScheme}, returning an {@link Optional}.
     *
     * @param authSchemeNameOrId the auth scheme id
     * @return an {@link Optional<AuthScheme>}
     */
    Optional<OAuth2AuthScheme> findAuthScheme(String authSchemeNameOrId);

    /**
     * Fetches a specific {@link AuthScheme} instance based on ID.  If not found, an
     * exception is raised.
     *
     * @param authSchemeId the auth scheme ID
     * @return the {@link AuthScheme}, never null
     */
    default OAuth2AuthScheme getAuthScheme(final String authSchemeId) {
        return findAuthScheme(authSchemeId).orElseThrow(AuthSchemeNotFoundException::new);
    }

    /**
     * Updates the supplied {@link AuthScheme}
     *
     * @param authScheme the {@link UpdateAuthSchemeRequest} with the information to update the authScheme
     * @return a {@link UpdateAuthSchemeResponse} as it was created
     */
    OAuth2AuthScheme updateAuthScheme(OAuth2AuthScheme authScheme);

    /**
     * Creates an {@link AuthScheme}
     *
     * @param authScheme the {@link CreateAuthSchemeRequest} with the information to create the authScheme
     * @return a {@link CreateAuthSchemeResponse} as it was created
     */
    OAuth2AuthScheme createAuthScheme(OAuth2AuthScheme authScheme);

    /**
     * Deletes the {@link AuthScheme} with the supplied auth scheme ID.
     *
     * @param authSchemeId the auth scheme ID.
     */
    void deleteAuthScheme(String authSchemeId);

}