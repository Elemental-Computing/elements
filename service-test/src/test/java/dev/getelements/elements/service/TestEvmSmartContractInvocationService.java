package dev.getelements.elements.service;

import dev.getelements.elements.dao.mongo.test.UserTestFactory;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.contract.CreateSmartContractRequest;
import dev.getelements.elements.sdk.model.blockchain.contract.SmartContractAddress;
import dev.getelements.elements.sdk.model.blockchain.wallet.CreateVaultRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.CreateWalletRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.CreateWalletRequestAccount;
import dev.getelements.elements.sdk.model.blockchain.wallet.Vault;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.blockchain.EvmSmartContractInvocationService;
import dev.getelements.elements.sdk.service.blockchain.SmartContractService;
import dev.getelements.elements.sdk.service.blockchain.VaultService;
import dev.getelements.elements.sdk.service.blockchain.WalletService;
import jakarta.inject.Named;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;
import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;
import static java.util.stream.Collectors.toMap;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class TestEvmSmartContractInvocationService {

    private static final String CONTRACT_NAME = "evm_integration_test";

    public static final Map<BlockchainNetwork, String> CONTACT_ADDRESSES = Map.of(
            BlockchainNetwork.BSC_TEST, "0xEF5FAD1711BA258ff6a4AbB1d86D165100B87956"
    );

    public static final Map<BlockchainNetwork, String> WALLET_ADDRESSES = Map.of(
            BlockchainNetwork.BSC_TEST, "0x05011bba59d302d5ff4ee1be2468bb8572aae286"
    );

    public static final Map<BlockchainNetwork, String> WALLET_PRIVATE_KEYS = Map.of(
            BlockchainNetwork.BSC_TEST, "c9aa92ff79ca085f7cc421227fe9f418d76933aadf0f81b595acd4722d63c943"
    );

    private User user;

    private Vault vault;

    private UserTestFactory userTestFactory;

    private EvmSmartContractInvocationService underTest;

    private VaultService vaultService;

    private WalletService walletService;

    private SmartContractService smartContractService;

    private Map<BlockchainNetwork, String> listingIds = new ConcurrentHashMap<>();

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getUnixFSTest(TestEvmSmartContractInvocationService.class)
        };
    }

    @BeforeClass
    public void setupUser() {
        user = getUserTestFactory().createTestUser(u -> u.setLevel(SUPERUSER));
    }

    @BeforeClass(dependsOnMethods = "setupUser")
    public void setupVault() {
        final var request = new CreateVaultRequest();
        request.setAlgorithm(RSA_512);
        request.setDisplayName("Integration Test Vault");
        request.setUserId(user.getId());
        vault = getVaultService().createVault(request);
    }

    @BeforeClass(dependsOnMethods = "setupVault")
    public void setupWallets() {
        for (var network : CONTACT_ADDRESSES.keySet()) {

            final var address = WALLET_ADDRESSES.get(network);
            final var privateKey = WALLET_PRIVATE_KEYS.get(network);
            final var account = new CreateWalletRequestAccount();
            account.setAddress(address);
            account.setPrivateKey(privateKey);

            final var request = new CreateWalletRequest();
            request.setApi(network.api());
            request.setNetworks(new ArrayList<>(List.of(network)));
            request.setAccounts(new ArrayList<>(List.of(account)));
            request.setDisplayName("Integration Test Wallet");
            request.setPreferredAccount(0);

            getWalletService().createWallet(vault.getId(), request);

        }
    }

    @BeforeClass(dependsOnMethods = "setupVault")
    public void setupContract() {

        final var addresses = CONTACT_ADDRESSES
                .entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, e -> {
                    final var address = new SmartContractAddress();
                    address.setAddress(e.getValue());
                    return address;
                }));

        final var request = new CreateSmartContractRequest();
        request.setDisplayName("Integration Test Contract");
        request.setName(CONTRACT_NAME);
        request.setVaultId(vault.getId());
        request.setAddresses(addresses);

        getSmartContractService().createSmartContract(request);

    }

    @DataProvider
    public Object[][] testNetworks() {
        return CONTACT_ADDRESSES
                .keySet()
                .stream()
                .map(n -> new Object[]{n})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "testNetworks", enabled = false)
    public void testSend(final BlockchainNetwork blockchainNetwork) {

        final var name = "Test";
        final var desc = "This is a test";

        final var response = getUnderTest()
                .resolve(CONTRACT_NAME, blockchainNetwork)
                .open()
                .send(
                        "createListing",
                        List.of("string", "string"),
                        List.of(name, desc),
                        List.of("string", "uint256")
                );

        assertNotNull(response);

        final var logs = response.getLogs();
        assertEquals(logs.size(), 1);

        final var data = logs.get(0).getData();
        assertNotNull(data);

        final List<Object> decodedData = response.getDecodedLog();
        final var responseName = decodedData.get(0).toString();
        final var responseId = decodedData.get(1).toString();

        assertNotNull(responseId);
        assertEquals(decodedData.size(), 2);
        assertEquals(name, responseName);

        listingIds.put(blockchainNetwork, responseId);

    }

    @Test(dataProvider = "testNetworks", dependsOnMethods = "testSend", enabled = false)
    public void testCall(final BlockchainNetwork blockchainNetwork) {

        final var listingId = listingIds.get(blockchainNetwork);

        final var response = getUnderTest()
                .resolve(CONTRACT_NAME, blockchainNetwork)
                .open()
                .call(
                        "listings",
                        List.of("uint256"),
                        List.of(listingId),
                        List.of("string", "string", "uint256", "uint256")
                );

        assertTrue(response instanceof List);

        final var responseList = (List<?>) response;
        assertEquals(responseList.size(), 4);
        assertTrue(responseList.get(0) instanceof String);
        assertTrue(responseList.get(1) instanceof String);
        assertTrue(responseList.get(2) instanceof Integer || ((List<?>) response).get(2) instanceof BigInteger);
        assertTrue(responseList.get(3) instanceof Integer || ((List<?>) response).get(3) instanceof BigInteger);

    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public EvmSmartContractInvocationService getUnderTest() {
        return underTest;
    }

    @Inject
    public void setUnderTest(@Named(UNSCOPED) EvmSmartContractInvocationService underTest) {
        this.underTest = underTest;
    }

    public VaultService getVaultService() {
        return vaultService;
    }

    @Inject
    public void setVaultService(@Named(UNSCOPED) VaultService vaultService) {
        this.vaultService = vaultService;
    }

    public WalletService getWalletService() {
        return walletService;
    }

    @Inject
    public void setWalletService(@Named(UNSCOPED) WalletService walletService) {
        this.walletService = walletService;
    }

    public SmartContractService getSmartContractService() {
        return smartContractService;
    }

    @Inject
    public void setSmartContractService(@Named(UNSCOPED) SmartContractService smartContractService) {
        this.smartContractService = smartContractService;
    }

}
