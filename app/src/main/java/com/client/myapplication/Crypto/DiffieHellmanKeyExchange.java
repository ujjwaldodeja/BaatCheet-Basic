package com.client.myapplication.Crypto;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DiffieHellmanKeyExchange {

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
        keyPairGenerator.initialize(512); // Adjust the key size as needed
        return keyPairGenerator.generateKeyPair();
    }

    public static String getPublicKeyBase64(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static SecretKey performKeyExchange(String otherPartyPublicKeyBase64, KeyPair ownKeyPair) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(otherPartyPublicKeyBase64));
        PublicKey otherPartyPublicKey = keyFactory.generatePublic(keySpec);

        KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
        keyAgreement.init(ownKeyPair.getPrivate());

        keyAgreement.doPhase(otherPartyPublicKey, true);

        // Generate the shared secret
        byte[] sharedSecret = keyAgreement.generateSecret();
        return new SecretKeySpec(sharedSecret, 0, sharedSecret.length, "AES");
    }

//    public static SecretKey deriveSecretKey(byte[] sharedSecret) {
//        // Derive a symmetric key from the shared secret
//        return new SecretKeySpec(sharedSecret, 0, sharedSecret.length, "AES");
//    }
}
