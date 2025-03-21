package dev.getelements.elements.service.blockchain.omni;

import dev.getelements.elements.sdk.dao.VaultDao;
import dev.getelements.elements.sdk.dao.WalletDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.wallet.CreateWalletRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.UpdateWalletRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.Wallet;
import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.blockchain.WalletService;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;

public class UserWalletService implements WalletService {

    private User user;

    private VaultDao vaultDao;

    private WalletDao walletDao;

    private SuperUserWalletService superUserWalletService;

    @Override
    public Pagination<Wallet> getWallets(
            final int offset, final int count,
            final String vaultId, final String userId,
            final BlockchainApi protocol, final List<BlockchainNetwork> networks) {
        if (userId == null || Objects.equals(userId, getUser().getId())) {
            final var vault = getVaultDao().getVaultForUser(vaultId, getUser().getId());
            return getWalletDao().getWallets(offset, count, vault.getId(), getUser().getId(), protocol, networks);
        } else {
            return Pagination.empty();
        }
    }

    @Override
    public Wallet getWallet(final String walletId) {
        return getWalletDao().getWalletForUser(walletId, getUser().getId());
    }

    @Override
    public Wallet getWalletInVault(final String walletId, final String vaultId) {
        final var vault = getVaultDao().getVaultForUser(vaultId, getUser().getId());
        return getWalletDao().getWalletInVault(walletId, vault.getId());
    }

    @Override
    public Wallet updateWallet(final String vaultId,
                               final String walletId,
                               final UpdateWalletRequest walletUpdateRequest) {
        final var vault = getVaultDao().getVaultForUser(vaultId, getUser().getId());
        return getSuperUserWalletService().updateWallet(vault.getId(), walletId, walletUpdateRequest);
    }

    @Override
    public Wallet createWallet(final String vaultId, final CreateWalletRequest createWalletRequest) {
        final var vault = getVaultDao().getVaultForUser(vaultId, getUser().getId());
        return getSuperUserWalletService().createWallet(vault.getId(), createWalletRequest);
    }

    @Override
    public void deleteWallet(final String walletId) {
        getWalletDao().deleteWalletForUser(walletId, getUser().getId());
    }

    @Override
    public void deleteWalletFromVault(final String walletId, final String vaultId) {
        final var vault = getVaultDao().getVaultForUser(vaultId, getUser().getId());
        getWalletDao().deleteWalletForVault(walletId, vault.getId());
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public VaultDao getVaultDao() {
        return vaultDao;
    }

    @Inject
    public void setVaultDao(VaultDao vaultDao) {
        this.vaultDao = vaultDao;
    }

    public WalletDao getWalletDao() {
        return walletDao;
    }

    @Inject
    public void setWalletDao(WalletDao walletDao) {
        this.walletDao = walletDao;
    }

    public SuperUserWalletService getSuperUserWalletService() {
        return superUserWalletService;
    }

    @Inject
    public void setSuperUserWalletService(SuperUserWalletService superUserWalletService) {
        this.superUserWalletService = superUserWalletService;
    }

}
