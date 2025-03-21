package dev.getelements.elements.service.blockchain.crypto.evm;

import dev.getelements.elements.sdk.dao.SmartContractDao;
import dev.getelements.elements.sdk.service.blockchain.EvmSmartContractInvocationService;
import dev.getelements.elements.sdk.service.blockchain.SmartContractInvocationResolution;
import dev.getelements.elements.sdk.service.blockchain.crypto.VaultCryptoUtilities;
import dev.getelements.elements.sdk.service.blockchain.crypto.WalletCryptoUtilities;
import dev.getelements.elements.sdk.service.blockchain.invoke.ScopedInvoker;
import dev.getelements.elements.service.blockchain.invoke.StandardSmartContractInvocationResolution;
import dev.getelements.elements.service.blockchain.invoke.SuperUserSmartContractInvocationService;
import dev.getelements.elements.sdk.service.blockchain.invoke.evm.EvmInvocationScope;
import jakarta.inject.Inject;
import jakarta.inject.Provider;


public class SuperUserEvmSmartContractInvocationService
        extends SuperUserSmartContractInvocationService<EvmInvocationScope, EvmSmartContractInvocationService.Invoker>
        implements EvmSmartContractInvocationService {

    private SmartContractDao smartContractDao;

    private VaultCryptoUtilities vaultCryptoUtilities;

    private WalletCryptoUtilities walletCryptoUtilities;

    private ScopedInvoker.Factory<EvmInvocationScope, Invoker> scopedInvokerFactory;

    private Provider<StandardSmartContractInvocationResolution<EvmInvocationScope, Invoker>> resolutionProvider;

    @Override
    protected EvmInvocationScope newInvocationScope() {
        final var evmInvocationScope = new EvmInvocationScope();
        evmInvocationScope.setGasLimit(DEFAULT_GAS_LIMIT);
        evmInvocationScope.setGasPrice(DEFAULT_GAS_PRICE);
        return evmInvocationScope;
    }

    @Override
    protected SmartContractInvocationResolution<Invoker> newResolution(final EvmInvocationScope evmInvocationScope) {
        final var resolution = getResolutionProvider().get();
        resolution.setScope(evmInvocationScope);
        return resolution;
    }

    public SmartContractDao getSmartContractDao() {
        return smartContractDao;
    }

    @Inject
    public void setSmartContractDao(SmartContractDao smartContractDao) {
        this.smartContractDao = smartContractDao;
    }

    public VaultCryptoUtilities getVaultCryptoUtilities() {
        return vaultCryptoUtilities;
    }

    @Inject
    public void setVaultCryptoUtilities(VaultCryptoUtilities vaultCryptoUtilities) {
        this.vaultCryptoUtilities = vaultCryptoUtilities;
    }

    public ScopedInvoker.Factory<EvmInvocationScope, Invoker> getScopedInvokerFactory() {
        return scopedInvokerFactory;
    }

    @Inject
    public void setScopedInvokerFactory(ScopedInvoker.Factory<EvmInvocationScope, Invoker> scopedInvokerFactory) {
        this.scopedInvokerFactory = scopedInvokerFactory;
    }

    public WalletCryptoUtilities getWalletCryptoUtilities() {
        return walletCryptoUtilities;
    }

    @Inject
    public void setWalletCryptoUtilities(WalletCryptoUtilities walletCryptoUtilities) {
        this.walletCryptoUtilities = walletCryptoUtilities;
    }

    public Provider<StandardSmartContractInvocationResolution<EvmInvocationScope, Invoker>> getResolutionProvider() {
        return resolutionProvider;
    }

    @Inject
    public void setResolutionProvider(Provider<StandardSmartContractInvocationResolution<EvmInvocationScope, Invoker>> resolutionProvider) {
        this.resolutionProvider = resolutionProvider;
    }

}
