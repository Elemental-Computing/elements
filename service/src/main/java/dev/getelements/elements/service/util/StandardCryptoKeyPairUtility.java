package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.model.exception.crypto.InvalidKeyException;
import dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm;
import dev.getelements.elements.sdk.service.util.CryptoKeyPairUtility;
import dev.getelements.elements.sdk.service.util.EncodedKeyPair;
import dev.getelements.elements.sdk.util.Monitor;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StandardCryptoKeyPairUtility implements CryptoKeyPairUtility {

    private final LockedPair<KeyFactory> rsaKeyFactory;

    private final LockedPair<KeyFactory> ecdsaKeyFactory;

    private final LockedPair<KeyPairGenerator> rsaKeyPairGenerator;

    private final LockedPair<KeyPairGenerator> ecdsaKeyPairGenerator;

    public StandardCryptoKeyPairUtility() throws NoSuchAlgorithmException {
        rsaKeyFactory = new LockedPair<>(KeyFactory.getInstance(RSA_ALGO));
        ecdsaKeyFactory = new LockedPair<>(KeyFactory.getInstance(ECDSA_ALGO));
        rsaKeyPairGenerator = new LockedPair<>(KeyPairGenerator.getInstance(RSA_ALGO));
        ecdsaKeyPairGenerator = new LockedPair<>(KeyPairGenerator.getInstance(ECDSA_ALGO));
    }

    @Override
    public EncodedKeyPair generateKeyPair(final PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm) {
        switch (privateKeyCrytpoAlgorithm) {
            case RSA_256:
            case RSA_384:
            case RSA_512:
                return generate(rsaKeyPairGenerator);
//            case ECDSA_256:
//            case ECDSA_384:
//            case ECDSA_512:
//                return generate(ecdsaKeyPairGenerator);
            default:
                // This should never happen
                throw new IllegalArgumentException("Unsupported algorithm: " + privateKeyCrytpoAlgorithm);
        }
    }

    private EncodedKeyPair generate(final LockedPair<KeyPairGenerator> generator) {
        try (var _m = generator.enter()) {
            final var keyPair = generator.get().generateKeyPair();
            return new EncodedKeyPair(keyPair, generator.get().getAlgorithm());
        }
    }

    @Override
    public <PublicKeyT extends PublicKey> PublicKeyT getPublicKey(
            final PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm,
            final String base64Representation,
            final Class<PublicKeyT> publicKeyTClass) {

        final byte[] decoded;

        try {
            decoded = Base64.getDecoder().decode(base64Representation);
        } catch (IllegalArgumentException ex) {
            throw new InvalidKeyException(ex);
        }

        final var keySpec = new X509EncodedKeySpec(decoded);

        switch (privateKeyCrytpoAlgorithm) {
            case RSA_256:
            case RSA_384:
            case RSA_512:
                return getPublicKey(rsaKeyFactory, keySpec, publicKeyTClass);
//            case ECDSA_256:
//            case ECDSA_384:
//            case ECDSA_512:
//                return getPublicKey(ecdsaKeyFactory, keySpec, publicKeyTClass);
            default:
                // This should never happen
                throw new IllegalArgumentException("Unsupported algorithm: " + privateKeyCrytpoAlgorithm);
        }
    }

    private <PublicKeyT extends PublicKey> PublicKeyT getPublicKey(
            final LockedPair<KeyFactory> factory,
            final KeySpec keySpec,
            final Class<PublicKeyT> publicKeyTClass) {
        try (var _m = factory.enter()) {
            return publicKeyTClass.cast(factory.get().generatePublic(keySpec));
        } catch (final InvalidKeySpecException | ClassCastException e) {
            throw new InvalidKeyException(e);
        }
    }

    @Override
    public <PrivateKeyT extends PrivateKey> PrivateKeyT getPrivateKey(
            final PrivateKeyCrytpoAlgorithm privateKeyCrytpoAlgorithm,
            final String base64Representation,
            final Class<PrivateKeyT> publicKeyTClass) {

        final byte[] decoded;

        try {
            decoded = Base64.getDecoder().decode(base64Representation);
        } catch (IllegalArgumentException ex) {
            throw new InvalidKeyException(ex);
        }

        final var keySpec = new PKCS8EncodedKeySpec(decoded);

        switch (privateKeyCrytpoAlgorithm) {
            case RSA_256:
            case RSA_384:
            case RSA_512:
                return getPrivateKey(rsaKeyFactory, keySpec, publicKeyTClass);
//            case ECDSA_256:
//            case ECDSA_384:
//            case ECDSA_512:
//                return getPrivateKey(ecdsaKeyFactory, keySpec, publicKeyTClass);
            default:
                // This should never happen
                throw new IllegalArgumentException("Unsupported algorithm: " + privateKeyCrytpoAlgorithm);
        }
    }

    private <PrivateKeyT extends PrivateKey> PrivateKeyT getPrivateKey(
            final LockedPair<KeyFactory> factory,
            final KeySpec keySpec,
            final Class<PrivateKeyT> publicKeyTClass) {
        try (var _m = factory.enter()) {
            return publicKeyTClass.cast(factory.get().generatePrivate(keySpec));
        } catch (final InvalidKeySpecException | ClassCastException e) {
            throw new InvalidKeyException(e);
        }
    }

    private static class LockedPair<LockedT> {

        private final LockedT lockedT;

        private final Lock lock = new ReentrantLock();

        public LockedPair(LockedT lockedT) {
            this.lockedT = lockedT;
        }

        public Monitor enter() {
            return Monitor.enter(lock);
        }

        public LockedT get() {
            return lockedT;
        }

    }

}
