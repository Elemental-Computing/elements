package dev.getelements.elements.dao.mongo.blockchain;

import dev.getelements.elements.sdk.dao.WalletDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.MongoUserDao;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.blockchain.MongoVault;
import dev.getelements.elements.dao.mongo.model.blockchain.MongoWallet;
import dev.getelements.elements.dao.mongo.model.blockchain.MongoWalletAccount;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.blockchain.WalletNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.ValidationGroups.Read;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.wallet.Wallet;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.in;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.util.stream.Collectors.toList;


public class MongoWalletDao implements WalletDao {

    private MapperRegistry mapperRegistry;

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoVaultDao mongoVaultDao;

    private MongoDBUtils mongoDBUtils;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<Wallet> getWallets(
            final int offset, final int count,
            final String vaultId,
            final String userId,
            final BlockchainApi protocol,
            final List<BlockchainNetwork> networks) {

        final var query = getDatastore().find(MongoWallet.class);

        if (userId != null && !userId.isBlank()) {

            final var mongoUser = getMongoUserDao().findMongoUser(userId);

            if (mongoUser.isEmpty())
                return Pagination.empty();

            query.filter(eq("user", mongoUser.get()));

        }

        if (vaultId != null && !vaultId.isBlank()) {

            final var mongoVault = getMongoVaultDao().findMongoVault(vaultId);

            if (mongoVault.isEmpty())
                return Pagination.empty();

            query.filter(eq("vault", mongoVault.get()));

        }

        if (protocol != null) {
            query.filter(eq("api", protocol));
        }

        if (networks != null && !networks.isEmpty()) {
            networks.forEach(n -> query.filter(in("networks", List.of(n))));
        }

        return getMongoDBUtils().paginationFromQuery(query, offset, count, Wallet.class);

    }

    @Override
    public Optional<Wallet> findWallet(String walletId) {
        return findMongoWallet(walletId).map(mw -> getMapper().map(mw, Wallet.class));
    }

    public Optional<MongoWallet> findMongoWallet(final String walletId) {

        final var walletObjectId = getMongoDBUtils().parse(walletId);

        if (walletObjectId.isEmpty()) {
            return Optional.empty();
        }

        final var query = getDatastore()
                .find(MongoWallet.class)
                .filter(eq("_id", walletObjectId.get()));

        final var result = query.first();
        return Optional.ofNullable(result);

    }

    @Override
    public Optional<Wallet> findWalletInVault(final String walletId, final String vaultId) {
        return findMongoWalletInVault(walletId, vaultId).map(mw -> getMapper().map(mw, Wallet.class));
    }

    public Optional<MongoWallet> findMongoWalletInVault(final String walletId, final String vaultId) {

        final var walletObjectId = getMongoDBUtils().parse(walletId);

        if (walletObjectId.isEmpty()) {
            return Optional.empty();
        }

        final var query = getDatastore()
                .find(MongoWallet.class)
                .filter(eq("_id", walletObjectId.get()));

        final var mongoVault = getMongoVaultDao().findMongoVault(vaultId);

        if (mongoVault.isEmpty()) {
            return Optional.empty();
        }

        query.filter(eq("vault", mongoVault.get()));

        final var result = query.first();
        return Optional.ofNullable(result);

    }

    @Override
    public Optional<Wallet> findWalletForUser(final String walletId, final String userId) {
        return findMongoWalletForUser(walletId, userId).map(mw -> getMapper().map(mw, Wallet.class));
    }

    public Optional<MongoWallet> findMongoWalletForUser(final String walletId, final String userId) {

        final var walletObjectId = getMongoDBUtils().parse(walletId);

        if (walletObjectId.isEmpty()) {
            return Optional.empty();
        }

        final var query = getDatastore()
                .find(MongoWallet.class)
                .filter(eq("_id", walletObjectId.get()));

        final var mongoUser = getMongoUserDao().findMongoUser(userId);

        if (mongoUser.isEmpty()) {
            return Optional.empty();
        }

        query.filter(eq("user", mongoUser.get()));

        final var result = query.first();
        return Optional.ofNullable(result);

    }

    @Override
    public Wallet getSingleWalletFromVaultForNetwork(final String vaultId, final BlockchainNetwork blockchainNetwork) {

        final var mongoVault = getMongoVaultDao().getMongoVault(vaultId);

        final var query = getDatastore().find(MongoWallet.class)
            .filter(eq("vault", mongoVault))
            .filter(in("networks", List.of(blockchainNetwork)));

        final var count = query.count();

        if (count == 1) {
            return getMapper().map(query.first(), Wallet.class);
        } else if (count == 0) {
            throw new WalletNotFoundException();
        } else {
            throw new DuplicateException();
        }

    }

    @Override
    public Wallet updateWallet(final Wallet wallet) {

        getValidationHelper().validateModel(wallet, Update.class);
        getValidationHelper().validateModel(wallet.getUser(), Read.class);
        getValidationHelper().validateModel(wallet.getVault(), Read.class);

        if (wallet.getAccounts().stream().anyMatch(Objects::isNull)) {
            throw new InvalidDataException("All identities must be non-null.");
        }

        if (wallet.getNetworks().stream().anyMatch(Objects::isNull)) {
            throw new InvalidDataException("All networks must be non-null.");
        }

        wallet.getApi().validate(wallet.getNetworks());

        final var objectId = getMongoDBUtils().parseOrThrow(wallet.getId(), WalletNotFoundException::new);

        final var query = getDatastore().find(MongoWallet.class);
        query.filter(eq("_id", objectId));

        final var mongoUser = getMongoUserDao()
                .findMongoUser(wallet.getUser().getId())
                .orElseThrow(() -> new InvalidDataException("No such user: " + wallet.getUser().getId()));

        final var mongoVault = getMongoVaultDao()
                .findMongoVault(wallet.getVault().getId())
                .orElseThrow(() -> new InvalidDataException("No such vault:" + wallet.getVault().getId()));

        final var mongoAccounts = wallet.getAccounts()
                .stream()
                .map(a -> getMapper().map(a, MongoWalletAccount.class))
                .collect(toList());

        if (!Objects.equals(mongoUser.getObjectId(), mongoVault.getUser().getObjectId())) {
            throw new InvalidDataException("Vault user and wallet user must match.");
        }

        final var mongoWallet = new UpdateBuilder()
                .with(set("user", mongoUser))
                .with(set("vault", mongoVault))
                .with(set("displayName", wallet.getDisplayName().trim()))
                .with(set("api", wallet.getApi()))
                .with(set("networks", wallet.getNetworks()))
                .with(set("preferredAccount", wallet.getPreferredAccount()))
                .with(set("accounts", mongoAccounts))
                .execute(query, new ModifyOptions().returnDocument(AFTER).upsert(false));

        if (mongoWallet == null) {
            throw new WalletNotFoundException();
        }

        return getMapper().map(mongoWallet, Wallet.class);

    }

    @Override
    public Wallet createWallet(final Wallet wallet) {

        getValidationHelper().validateModel(wallet, ValidationGroups.Insert.class);
        getValidationHelper().validateModel(wallet.getUser(), Read.class);
        getValidationHelper().validateModel(wallet.getVault(), Read.class);

        if (wallet.getAccounts().stream().anyMatch(Objects::isNull)) {
            throw new InvalidDataException("All identities must be non-null values.");
        }

        if (wallet.getNetworks().stream().anyMatch(Objects::isNull)) {
            throw new InvalidDataException("All networks must be non-null values.");
        }

        wallet.getApi().validate(wallet.getNetworks());

        final var mongoWallet = getMapper().map(wallet, MongoWallet.class);

        final var mongoUser = getMongoUserDao()
                .findMongoUser(wallet.getUser().getId())
                .orElseThrow(() -> new InvalidDataException("No such user: " + wallet.getUser().getId()));

        final var mongoVault = getMongoVaultDao()
                .findMongoVault(wallet.getVault().getId())
                .orElseThrow(() -> new InvalidDataException("No such vault:" + wallet.getVault().getId()));

        if (!Objects.equals(mongoUser.getObjectId(), mongoVault.getUser().getObjectId())) {
            throw new InvalidDataException("Vault user and wallet user must match.");
        }

        mongoWallet.setUser(mongoUser);
        mongoWallet.setVault(mongoVault);

        getDatastore().insert(mongoWallet);

        return getMapper().map(mongoWallet, Wallet.class);

    }

    @Override
    public void deleteWallet(final String walletId) {

        final var objectId = getMongoDBUtils().parseOrThrow(walletId, WalletNotFoundException::new);

        final var query = getDatastore()
                .find(MongoWallet.class)
                .filter(eq("_id", objectId));

        final var result = query.delete();

        if (result.getDeletedCount() == 0)
            throw new WalletNotFoundException();

    }

    @Override
    public void deleteWalletForUser(final String walletId, final String userId) {

        final var objectId = getMongoDBUtils().parseOrThrow(walletId, WalletNotFoundException::new);

        final var query = getDatastore()
                .find(MongoWallet.class)
                .filter(eq("_id", objectId));

        final var mongoUser = getMongoUserDao()
                .findMongoUser(userId);

        if (mongoUser.isEmpty())
            throw new WalletNotFoundException();

        query.filter(eq("user", mongoUser.get()));

        final var result = query.delete();

        if (result.getDeletedCount() == 0)
            throw new WalletNotFoundException();

    }

    @Override
    public void deleteWalletForVault(final String walletId, final String vaultId) {

        final var objectId = getMongoDBUtils().parseOrThrow(walletId, WalletNotFoundException::new);

        final var query = getDatastore()
                .find(MongoWallet.class)
                .filter(eq("_id", objectId));

        final var mongoVault = getMongoVaultDao().findMongoVault(vaultId);

        if (mongoVault.isEmpty())
            throw new WalletNotFoundException();

        query.filter(eq("vault", mongoVault.get()));

        final var result = query.delete();

        if (result.getDeletedCount() == 0)
            throw new WalletNotFoundException();

    }

    public long deleteWalletsInMongoVault(final MongoVault vault) {

        final var query = getDatastore()
                .find(MongoWallet.class)
                .filter(eq("vault", vault));

        final var result = query.delete();
        return result.getDeletedCount();

    }

    public MapperRegistry getMapper() {
        return mapperRegistry;
    }

    @Inject
    public void setMapper(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoVaultDao getMongoVaultDao() {
        return mongoVaultDao;
    }

    @Inject
    public void setMongoVaultDao(MongoVaultDao mongoVaultDao) {
        this.mongoVaultDao = mongoVaultDao;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
