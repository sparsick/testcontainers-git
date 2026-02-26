package io.github.sparsick.testcontainers.gitserver.forgejo;

import java.nio.charset.StandardCharsets;

/**
 * Value object for identity information for a public key authentication.
 */
public class SshIdentity {
    private String privateKey;
    private String publicKey;
    private byte[] passphrase;

    /**
     * Identity information for a public key authentication.
     *
     * @param privateKey SSH private key
     * @param publicKey SSH public key
     * @param passphrase password for private key
     */
    public SshIdentity(String privateKey, String publicKey, byte[] passphrase) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.passphrase = passphrase;
    }

    /**
     * SSH private key
     *
     * @return SSH private key
     */
    public String getPrivateKey() {
        return privateKey;
    }


    public byte[] getPrivateKeyAsBytes() {
        return privateKey.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * SSH public key
     *
     * @return  SSH public key
     */
    public String getPublicKey() {
        return publicKey;
    }


    public byte[] getPublicKeyAsBytes() {
        return publicKey.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Password for the SSH private key
     *
     * @return Password for the SSH private key
     */
    public byte[] getPassphrase() {
        return passphrase;
    }
}
