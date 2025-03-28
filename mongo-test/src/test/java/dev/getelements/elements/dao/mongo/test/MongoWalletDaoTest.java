package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.VaultDao;
import dev.getelements.elements.sdk.dao.WalletDao;
import dev.getelements.elements.sdk.model.exception.blockchain.WalletNotFoundException;
import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.wallet.Vault;
import dev.getelements.elements.sdk.model.blockchain.wallet.VaultKey;
import dev.getelements.elements.sdk.model.blockchain.wallet.Wallet;
import dev.getelements.elements.sdk.model.blockchain.wallet.WalletAccount;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;
import static dev.getelements.elements.sdk.util.Hex.Case.UPPER;
import static dev.getelements.elements.sdk.util.Hex.forNibble;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoWalletDaoTest {

    private static final int TEST_USER_COUNT = 10;

    private WalletDao underTest;

    private VaultDao vaultDao;

    private UserTestFactory userTestFactory;

    private User trudyUser;

    private List<User> regularUsers;

    private List<Vault> regularUsersVaults;

    private final Map<String, Wallet> wallets = new ConcurrentHashMap<>();

    @BeforeClass
    public void createTestUsers() {

        trudyUser = getUserTestFactory().createTestUser();

        regularUsers = IntStream.range(0, TEST_USER_COUNT)
                .mapToObj(i -> getUserTestFactory().createTestUser())
                .collect(toList());

        regularUsersVaults = regularUsers
                .stream()
                .map(user -> {

                    final var key = new VaultKey();
                    key.setAlgorithm(RSA_512);
                    key.setEncrypted(false);
                    key.setPublicKey(randomKey());
                    key.setPrivateKey(randomKey());

                    final var vault = new Vault();
                    vault.setKey(key);
                    vault.setUser(user);
                    vault.setDisplayName("Vault for User:" + user.getName());

                    return getVaultDao().createVault(vault);

                }).collect(toList());

    }

    @DataProvider
    public Object[][] regularUsers() {
        return regularUsers
                .stream()
                .map(u -> new Object[]{u})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] regularUsersAndBlockchainNetworks() {
        return regularUsers
                .stream()
                .flatMap(user -> Stream
                        .of(BlockchainNetwork.values())
                        .map(network -> new Object[]{user, network}))
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] regularUserVaultsAndBlockchainNetworks() {
        return regularUsersVaults
                .stream()
                .flatMap(user -> Stream
                        .of(BlockchainNetwork.values())
                        .map(network -> new Object[]{user, network}))
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] regularUsersAndBlockchainProtocols() {
        return regularUsers
                .stream()
                .flatMap(user -> Stream
                        .of(BlockchainApi.values())
                        .map(protocol -> new Object[]{user, protocol}))
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] blockchainNetworks() {
        return Stream
                .of(BlockchainNetwork.values())
                .map(protocol -> new Object[]{protocol})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] blockchainProtocols() {
        return Stream
                .of(BlockchainApi.values())
                .map(protocol -> new Object[]{protocol})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] wallets() {
        return wallets
                .values()
                .stream()
                .map(u -> new Object[]{u})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] walletsById() {
        return wallets
                .entrySet()
                .stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toArray(Object[][]::new);
    }

    public static String randomKey() {

        final var key = new char[256];
        final var random = ThreadLocalRandom.current();

        for (int i = 0; i < key.length; ++i) {
            key[i] = forNibble(random.nextInt(0xF), UPPER);
        }

        return new String(key);

    }

    @Test(dataProvider = "regularUserVaultsAndBlockchainNetworks", groups = "createWallets")
    public void testCreateWallets(final Vault vault, final BlockchainNetwork network) {

        final var wallet = new Wallet();
        wallet.setDisplayName("Wallet for User " + vault.getDisplayName());
        wallet.setVault(vault);
        wallet.setUser(vault.getUser());
        wallet.setNetworks(List.of(network));
        wallet.setApi(network.api());

        final var identity = new WalletAccount();
        identity.setEncrypted(true);
        identity.setAddress(randomKey());
        identity.setPrivateKey(randomKey());
        wallet.setAccounts(List.of(identity));

        final var created = getUnderTest().createWallet(wallet);
        assertNotNull(created.getId());
        assertEquals(created.getDisplayName(), wallet.getDisplayName());
        assertEquals(created.getNetworks(), wallet.getNetworks());
        assertEquals(created.getUser(), wallet.getUser());
        assertEquals(created.getApi(), wallet.getApi());

        wallets.put(created.getId(), created);

    }

    @Test(dataProvider = "wallets", groups = "updateWallets", dependsOnGroups = "createWallets")
    public void testUpdateWallet(final Wallet wallet) {

        final var update = new Wallet();
        update.setId(wallet.getId());
        update.setPreferredAccount(wallet.getPreferredAccount());
        update.setDisplayName(wallet.getDisplayName());
        update.setUser(wallet.getUser());
        update.setVault(wallet.getVault());
        update.setNetworks(wallet.getNetworks());
        update.setApi(wallet.getApi());

        final var identity = new WalletAccount();
        identity.setEncrypted(true);
        identity.setAddress(randomKey());
        identity.setPrivateKey(randomKey());
        update.setAccounts(List.of(identity));

        final var updated = getUnderTest().updateWallet(update);
        assertEquals(updated, update);
        assertNotEquals(updated, wallet);

        wallets.put(updated.getId(), updated);

    }

    @Test(dataProvider = "regularUsers", groups = "read", dependsOnGroups = "updateWallets")
    public void testGetWalletForUser(final User user) {

        final var wallets = new PaginationWalker()
            .toList((offset, count) -> getUnderTest()
                    .getWallets(offset, count, null, user.getId(), null, null));

        for(var wallet : wallets) {
            assertEquals(wallet.getUser(), user);
        }

    }

    @Test(dataProvider = "regularUsersAndBlockchainNetworks", groups = "read", dependsOnGroups = "updateWallets")
    public void testGetWalletForUser(final User user, final BlockchainNetwork network) {

        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getWallets(offset, count, null, user.getId(), null, List.of(network)));

        for(var wallet : wallets) {
            assertEquals(wallet.getUser(), user);
            assertEquals(wallet.getApi(), network.api());
            assertTrue(wallet.getNetworks().contains(network));
        }

    }

    @Test(dataProvider = "regularUsersAndBlockchainProtocols", groups = "read", dependsOnGroups = "updateWallets")
    public void testGetWalletForUser(final User user, final BlockchainApi protocol) {

        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getWallets(offset, count, null, user.getId(), protocol, null));

        for(var wallet : wallets) {
            assertEquals(wallet.getUser(), user);
            assertEquals(wallet.getApi(), protocol);
        }

    }

    @Test(dataProvider = "blockchainNetworks", groups = "read", dependsOnGroups = "updateWallets")
    public void testGetWalletNetwork(final BlockchainNetwork network) {

        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getWallets(offset, count, null, null, null, List.of(network))
                );

        for(var wallet : wallets) {
            assertTrue(wallet.getNetworks().contains(network));
            assertEquals(wallet.getApi(), network.api());
        }

    }

    @Test(dataProvider = "blockchainProtocols", groups = "read", dependsOnGroups = "updateWallets")
    public void testGetWalletApi(final BlockchainApi api) {

        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getWallets(offset, count, null, null, api, null));

        for(var wallet : wallets) {
            assertEquals(wallet.getApi(), api);
        }

    }

    @Test(groups = "read", dependsOnGroups = "updateWallets")
    public void testGetWalletWalletsFilters() {
        final var wallets = new PaginationWalker()
                .toList((offset, count) -> getUnderTest()
                        .getWallets(offset, count, null, trudyUser.getId(),null, null));
        assertTrue(wallets.isEmpty());
    }

    @Test(dataProvider = "walletsById", groups = "read", dependsOnGroups = "updateWallets")
    public void testGetSingleWallet(final String walletId, final Wallet wallet) {
        final var fetched = getUnderTest().getWallet(walletId);
        assertEquals(fetched, wallet);
    }

    @Test(dataProvider = "walletsById", groups = "read", dependsOnGroups = "updateWallets")
    public void testGetSingleWalletForVault(final String walletId, final Wallet wallet) {
        final var fetched = getUnderTest().getWalletInVault(walletId, wallet.getVault().getId());
        assertEquals(fetched, wallet);
    }

    @Test(groups = "read", dependsOnGroups = "updateWallets", expectedExceptions = WalletNotFoundException.class)
    public void testGetSingleWalletNotFound() {
        getUnderTest().getWallet(new ObjectId().toString());
    }

    @Test(dataProvider = "wallets", groups = "read", dependsOnGroups = "updateWallets", expectedExceptions = WalletNotFoundException.class)
    public void testGetSingleWalletForUserNotFound(final Wallet wallet) {
        getUnderTest().getWalletInVault(wallet.getId(), trudyUser.getId());
    }

    @Test(groups = "read", dependsOnGroups = "updateWallets")
    public void testFindSingleWalletNotFound() {
        final var wallet = getUnderTest().findWallet(new ObjectId().toString());
        assertFalse(wallet.isPresent());
    }

    @Test(dataProvider = "wallets", groups = "read", dependsOnGroups = "updateWallets")
    public void testFindSingleWalletForUserNotFound(final Wallet wallet) {
        final var result = getUnderTest().findWalletInVault(wallet.getId(), trudyUser.getId());
        assertFalse(result.isPresent());
    }

    @Test(dataProvider = "wallets",
            groups = {"delete", "pre delete"},
            dependsOnGroups = "updateWallets",
            expectedExceptions = WalletNotFoundException.class)
    public void deleteWalletForWrongUserFails(final Wallet wallet) {
        try {
            getUnderTest().deleteWalletForUser(wallet.getId(), trudyUser.getId());
        } finally {
            assertTrue(getUnderTest().findWallet(wallet.getId()).isPresent());
        }
    }

    @Test(dataProvider = "wallets", groups = "delete", dependsOnGroups = "pre delete")
    public void deleteWallet(final Wallet wallet) {
        getUnderTest().deleteWallet(wallet.getId());
    }

    @Test(dataProvider = "wallets",
            groups = "delete",
            dependsOnGroups = "pre delete",
            dependsOnMethods = "deleteWallet",
            expectedExceptions = WalletNotFoundException.class)
    public void doubleDeleteWalletFails(final Wallet wallet) {
        getUnderTest().deleteWallet(wallet.getId());
    }

    @Test(dataProvider = "regularUserVaultsAndBlockchainNetworks", groups = "delete", dependsOnGroups = "pre delete")
    public void deleteWalletForUser(final Vault vault, final BlockchainNetwork network) {

        final var wallet = new Wallet();
        wallet.setDisplayName("Wallet for User " + vault.getDisplayName());
        wallet.setUser(vault.getUser());
        wallet.setVault(vault);
        wallet.setNetworks(List.of(network));
        wallet.setApi(network.api());

        final var identity = new WalletAccount();
        identity.setEncrypted(false);
        identity.setAddress(randomKey());
        identity.setPrivateKey(randomKey());
        wallet.setAccounts(List.of(identity));

        final var created = getUnderTest().createWallet(wallet);
        getUnderTest().deleteWalletForUser(created.getId(), vault.getUser().getId());
        assertTrue(getUnderTest().findWallet(created.getId()).isEmpty());

    }

    @Test(dataProvider = "regularUserVaultsAndBlockchainNetworks", groups = "delete", dependsOnGroups = "pre delete")
    public void deleteWalletForVault(final Vault vault, final BlockchainNetwork network) {

        final var wallet = new Wallet();
        wallet.setDisplayName("Wallet for User " + vault.getDisplayName());
        wallet.setUser(vault.getUser());
        wallet.setVault(vault);
        wallet.setNetworks(List.of(network));
        wallet.setApi(network.api());

        final var identity = new WalletAccount();
        identity.setEncrypted(false);
        identity.setAddress(randomKey());
        identity.setPrivateKey(randomKey());
        wallet.setAccounts(List.of(identity));

        final var created = getUnderTest().createWallet(wallet);
        getUnderTest().deleteWalletForVault(created.getId(), vault.getId());
        assertTrue(getUnderTest().findWallet(created.getId()).isEmpty());

    }

    public WalletDao getUnderTest() {
        return underTest;
    }

    @Inject
    public void setUnderTest(WalletDao underTest) {
        this.underTest = underTest;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public VaultDao getVaultDao() {
        return vaultDao;
    }

    @Inject
    public void setVaultDao(VaultDao vaultDao) {
        this.vaultDao = vaultDao;
    }

}
