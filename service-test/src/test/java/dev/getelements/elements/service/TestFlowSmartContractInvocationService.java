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
import dev.getelements.elements.sdk.service.blockchain.FlowSmartContractInvocationService;
import dev.getelements.elements.sdk.service.blockchain.SmartContractService;
import dev.getelements.elements.sdk.service.blockchain.VaultService;
import dev.getelements.elements.sdk.service.blockchain.WalletService;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;
import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestFlowSmartContractInvocationService {

    private static final Logger logger = LoggerFactory.getLogger(TestFlowSmartContractInvocationService.class);

    private static final String CONTRACT_NAME = "flow_integration_test";

    private static final String CONTRACT_NAME_ALT = "flow_integration_test_alt";

    public static final Map<BlockchainNetwork, String> WALLET_ADDRESSES = Map.of(
            BlockchainNetwork.FLOW_TEST, "f8d6e0586b0a20c7"
    );

    public static final Map<BlockchainNetwork, String> WALLET_PRIVATE_KEYS = Map.of(
            BlockchainNetwork.FLOW_TEST, "97b3facdc2f260566b110ac3638e5c7bc07b96dd374886425654e5c09588eaaa"
    );

    public static final Map<BlockchainNetwork, String> CONTACT_ADDRESSES = Map.of(
            BlockchainNetwork.FLOW_TEST, "0xf8d6e0586b0a20c7"
    );

    public static final Map<BlockchainNetwork, String> CONTACT_ADDRESSES_ALT = Map.of(
            BlockchainNetwork.FLOW_TEST, "0xf8d6e0586b0a20c7:HelloWorld"
    );

    private static final String TEST_SEND_GET_MESSAGE;

    private static final String TEST_SEND_SET_MESSAGE;

    private static final String TEST_CALL_SCRIPT_SINGLE_ARG;

    private static final Set<String> TEST_CALL_SCRIPTS_NOARG;

    static {

        final Function<String, String> loader = filename -> {

            final var path = String.format("/flow/%s", filename);

            try (final var is = TestFlowSmartContractInvocationService.class.getResourceAsStream(path)) {

                if (is == null) {
                    throw new IllegalStateException("Unable to find: " + path);
                }

                final var bytes = is.readAllBytes();
                return new String(bytes, UTF_8);

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        };

        TEST_CALL_SCRIPTS_NOARG = Stream.of(
                "script_get_total_count.cdc",
                "script_get_total_count2.cdc")
                .map(loader)
                .collect(Collectors.toUnmodifiableSet());

        TEST_SEND_GET_MESSAGE = loader.apply("tx_get_message.cdc");

        TEST_SEND_SET_MESSAGE = loader.apply("tx_set_message.cdc");

        TEST_CALL_SCRIPT_SINGLE_ARG = loader.apply("script_get_default_msg.cdc");

    }

    private User user;

    private Vault vault;

    private UserTestFactory userTestFactory;

    private FlowSmartContractInvocationService underTest;

    private VaultService vaultService;

    private WalletService walletService;

    private SmartContractService smartContractService;

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getUnixFSTest(TestFlowSmartContractInvocationService.class)
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
        for (var network : WALLET_ADDRESSES.keySet()) {

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

    @BeforeClass(dependsOnMethods = "setupVault")
    public void setupContractAlt() {

        final var addresses = CONTACT_ADDRESSES_ALT
                .entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, e -> {
                    final var address = new SmartContractAddress();
                    address.setAddress(e.getValue());
                    return address;
                }));

        final var request = new CreateSmartContractRequest();
        request.setDisplayName("Integration Test Contract");
        request.setName(CONTRACT_NAME_ALT);
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

    @DataProvider
    public Object[][] testNetworksAndNoArgCallScripts() {
        return CONTACT_ADDRESSES
                .keySet()
                .stream()
                .flatMap(n -> TEST_CALL_SCRIPTS_NOARG.stream().map(s -> new Object[]{n, s}))
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "testNetworks", enabled = false)
    public void testSendGetMessage(final BlockchainNetwork blockchainNetwork) {

        final var response = getUnderTest()
                .resolve(CONTRACT_NAME, blockchainNetwork)
                .open()
                .send(TEST_SEND_GET_MESSAGE);

        logger.info("Got result {}", response);

        assertEquals(response.getStatus(), "SEALED");
        assertEquals(response.getStatusCode(), 0);

    }

    @Test(dataProvider = "testNetworks", enabled = false)
    public void testSendGetMessageAlt(final BlockchainNetwork blockchainNetwork) {

        final var response = getUnderTest()
                .resolve(CONTRACT_NAME_ALT, blockchainNetwork)
                .open()
                .send(TEST_SEND_GET_MESSAGE);

        logger.info("Got result {}", response);

        assertEquals(response.getStatus(), "SEALED");
        assertEquals(response.getStatusCode(), 0);

    }

    @Test(dataProvider = "testNetworks", enabled = false)
    public void testSendSetMessage(final BlockchainNetwork blockchainNetwork) {

        final var response = getUnderTest()
                .resolve(CONTRACT_NAME, blockchainNetwork)
                .open()
                .send(TEST_SEND_SET_MESSAGE, List.of("String"), List.of("Hello Java World!"));

        logger.info("Got result {}", response);

        assertEquals(response.getStatus(), "SEALED");
        assertEquals(response.getStatusCode(), 0);

    }

    @Test(dataProvider = "testNetworksAndNoArgCallScripts", enabled = false)
    public void testCallNoArg(final BlockchainNetwork blockchainNetwork, final String script) {

        final var response = getUnderTest()
                .resolve(CONTRACT_NAME, blockchainNetwork)
                .open()
                .call(script);

        logger.info("Got result {}", response);

    }

    @Test(dataProvider = "testNetworks",enabled = false)
    public void testCallSingleArg(final BlockchainNetwork blockchainNetwork) {

        final var arg = "My Message Prefix";

        final var response = getUnderTest()
                .resolve(CONTRACT_NAME, blockchainNetwork)
                .open()
                .call(TEST_CALL_SCRIPT_SINGLE_ARG, List.of("String"), List.of(arg));

        logger.info("Got result {}", response);
        assertTrue(response instanceof String);
        assertTrue(((String) response).startsWith(arg));

    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public FlowSmartContractInvocationService getUnderTest() {
        return underTest;
    }

    @Inject
    public void setUnderTest(@Named(UNSCOPED) FlowSmartContractInvocationService underTest) {
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
