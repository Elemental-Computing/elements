package dev.getelements.elements.service.savedata;

import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.SaveDataDocumentDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.exception.ConflictException;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.savedata.SaveDataNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.savedata.CreateSaveDataDocumentRequest;
import dev.getelements.elements.sdk.model.savedata.SaveDataDocument;
import dev.getelements.elements.sdk.model.savedata.UpdateSaveDataDocumentRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.savedata.SaveDataDocumentService;
import jakarta.inject.Inject;
import java.util.Objects;
import java.util.Optional;

import static java.lang.System.currentTimeMillis;

public class SuperUserSaveDataDocumentService implements SaveDataDocumentService {

    private UserDao userDao;

    private ProfileDao profileDao;

    private SaveDataDocumentDao saveDataDocumentDao;

    private ValidationHelper validationHelper;

    @Override
    public Optional<SaveDataDocument> findSaveDataDocument(String saveDataDocumentId) {
        return getSaveDataDocumentDao().findSaveDataDocument(saveDataDocumentId);
    }

    @Override
    public Pagination<SaveDataDocument> getSaveDataDocuments(final int offset, final int count, final String query) {
        return getSaveDataDocumentDao().getSaveDataDocuments(offset, count, null, null, query);
    }

    @Override
    public Pagination<SaveDataDocument> getSaveDataDocuments(final int offset, final int count,
                                                             final String userId, final String profileId) {
        return getSaveDataDocumentDao().getSaveDataDocuments(offset, count, userId, profileId);
    }

    @Override
    public SaveDataDocument getUserSaveDataDocumentBySlot(String userId, int slot) {
        return getSaveDataDocumentDao().getUserSaveDataDocumentBySlot(userId, slot);
    }

    @Override
    public SaveDataDocument getProfileSaveDataDocumentBySlot(String profileId, int slot) {
        return getSaveDataDocumentDao().getProfileSaveDataDocumentBySlot(profileId, slot);
    }

    @Override
    public SaveDataDocument createSaveDataDocument(final CreateSaveDataDocumentRequest createSaveDataDocumentRequest) {

        getValidationHelper().validateModel(createSaveDataDocumentRequest);

        final var document = new SaveDataDocument();

        final var userId = createSaveDataDocumentRequest.getUserId();
        final var profileId = createSaveDataDocumentRequest.getProfileId();

        var user = userId == null ? null : getUserDao().getUser(userId);
        var profile = profileId == null ? null : getProfileDao().getActiveProfile(profileId);

        if (user != null && profile != null) {
            if (Objects.equals(user.getId(), profile.getUser().getId()))
                throw new ConflictException("User and profile do not match.");
        } else if (profile != null) {
            document.setUser(profile.getUser());
        } else if (user != null) {
            document.setUser(user);
        } else {
            throw new InvalidDataException("Must specify either user or profile.");
        }

        document.setUser(user);
        document.setProfile(profile);
        document.setTimestamp(currentTimeMillis());
        document.setSlot(createSaveDataDocumentRequest.getSlot());
        document.setContents(createSaveDataDocumentRequest.getContents());

        return getSaveDataDocumentDao().createSaveDataDocument(document);

    }

    @Override
    public SaveDataDocument updateSaveDataDocument(
            final String saveDataDocumentId,
            final UpdateSaveDataDocumentRequest updateSaveDataDocumentRequest) {

        getValidationHelper().validateModel(updateSaveDataDocumentRequest);

        final var force = updateSaveDataDocumentRequest.getForce();
        final var document = getSaveDataDocumentDao().getSaveDataDocument(saveDataDocumentId);

        document.setTimestamp(currentTimeMillis());
        document.setContents(updateSaveDataDocumentRequest.getContents());

        if (force != null && force) {
            return getSaveDataDocumentDao().forceUpdateSaveDataDocument(document);
        } else {

            final var existingVersion = updateSaveDataDocumentRequest.getVersion();

            if (existingVersion == null)
                throw new InvalidDataException("Must specify existing version.");

            document.setVersion(existingVersion);

            try {
                return getSaveDataDocumentDao().checkedUpdate(document);
            } catch (SaveDataNotFoundException nfe) {
                throw new DuplicateException("Version mismatch.");
            }

        }

    }

    @Override
    public void deleteSaveDocument(final String saveDataDocumentId) {
        getSaveDataDocumentDao().deleteSaveDocument(saveDataDocumentId);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public SaveDataDocumentDao getSaveDataDocumentDao() {
        return saveDataDocumentDao;
    }

    @Inject
    public void setSaveDataDocumentDao(SaveDataDocumentDao saveDataDocumentDao) {
        this.saveDataDocumentDao = saveDataDocumentDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
