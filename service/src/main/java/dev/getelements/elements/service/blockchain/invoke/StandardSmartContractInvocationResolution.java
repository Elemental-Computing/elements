package dev.getelements.elements.service.blockchain.invoke;

import dev.getelements.elements.sdk.dao.VaultDao;

import dev.getelements.elements.sdk.service.blockchain.SmartContractInvocationResolution;
import dev.getelements.elements.sdk.service.blockchain.crypto.VaultCryptoUtilities;
import dev.getelements.elements.sdk.service.blockchain.crypto.WalletCryptoUtilities;
import dev.getelements.elements.sdk.service.blockchain.invoke.InvocationScope;
import dev.getelements.elements.sdk.service.blockchain.invoke.ScopedInvoker;
import jakarta.inject.Inject;

public class StandardSmartContractInvocationResolution<
            InvocationScopeT extends InvocationScope,
            InvokerT extends ScopedInvoker<InvocationScopeT>>
       implements SmartContractInvocationResolution<InvokerT> {

    protected InvocationScopeT scope;

    private VaultDao vaultDao;

    private VaultCryptoUtilities vaultCryptoUtilities;

    private WalletCryptoUtilities walletCryptoUtilities;

    private ScopedInvoker.Factory<InvocationScopeT, InvokerT> scopedInvokerFactory;

    @Override
    public InvokerT open() {

        final var blockchainNetwork = scope.getBlockchainNetwork();

        final var vaultKey = scope.getVault().getKey();
        final var walletAccount = scope.getWalletAccount();

        if (walletAccount.isEncrypted() && !vaultKey.isEncrypted()) {

            final var decryptedWalletAccount = walletCryptoUtilities
                    .decrypt(vaultKey, scope.getWalletAccount())
                    .orElseThrow(() -> new IllegalArgumentException("Failed to decrypted wallet account."));

            scope.setWalletAccount(decryptedWalletAccount);

        }

        final var scopedInvoker = scopedInvokerFactory.create(blockchainNetwork);
        scopedInvoker.initialize(scope);
        return scopedInvoker;

    }

    @Override
    public InvokerT unlock(final String passphrase) {

        final var key = scope.getVault().getKey();

        final var unlocked = vaultCryptoUtilities
                .decryptKey(key, passphrase)
                .orElseThrow(() -> new IllegalStateException("Unable to unlock vault."));

        scope.getVault().setKey(unlocked);
        return open();

    }

    @Override
    public SmartContractInvocationResolution<InvokerT> vault(final String vaultId) {
        final var vault = vaultDao.getVault(vaultId);
        scope.setVault(vault);
        return this;
    }

    public InvocationScopeT getScope() {
        return scope;
    }

    public void setScope(InvocationScopeT scope) {
        this.scope = scope;
    }

    public VaultDao getVaultDao() {
        return vaultDao;
    }

    @Inject
    public void setVaultDao(VaultDao vaultDao) {
        this.vaultDao = vaultDao;
    }

    public VaultCryptoUtilities getVaultCryptoUtilities() {
        return vaultCryptoUtilities;
    }

    @Inject
    public void setVaultCryptoUtilities(VaultCryptoUtilities vaultCryptoUtilities) {
        this.vaultCryptoUtilities = vaultCryptoUtilities;
    }

    public WalletCryptoUtilities getWalletCryptoUtilities() {
        return walletCryptoUtilities;
    }

    @Inject
    public void setWalletCryptoUtilities(WalletCryptoUtilities walletCryptoUtilities) {
        this.walletCryptoUtilities = walletCryptoUtilities;
    }

    public ScopedInvoker.Factory<InvocationScopeT, InvokerT> getScopedInvokerFactory() {
        return scopedInvokerFactory;
    }

    @Inject
    public void setScopedInvokerFactory(ScopedInvoker.Factory<InvocationScopeT, InvokerT> scopedInvokerFactory) {
        this.scopedInvokerFactory = scopedInvokerFactory;
    }

}
