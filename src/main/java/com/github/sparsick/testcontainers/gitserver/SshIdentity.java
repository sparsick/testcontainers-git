package com.github.sparsick.testcontainers.gitserver;

/**
 * Value object for identity information for a public key authentication.
 */
public class SshIdentity {
    private byte[] privateKey;
    private byte[] publicKey;
    private byte[] passphrase;

    /**
     * Identity information for a public key authentication.
     *
     * @param privateKey SSH private key
     * @param publicKey SSH public key
     * @param passphrase password for private key
     */
    public SshIdentity(byte[] privateKey, byte[] publicKey, byte[] passphrase) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.passphrase = passphrase;
    }

    /**
     * SSH private key
     *
     * @return SSH private key
     */
    public byte[] getPrivateKey() {
        return privateKey;
    }

    /**
     * SSH public key
     *
     * @return  SSH public key
     */
    public byte[] getPublicKey() {
        return publicKey;
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
