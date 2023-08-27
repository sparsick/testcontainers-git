package com.github.sparsick.testcontainers.gitserver;

/**
 * Value object for identity information for a public key authentication.
 */
public class SshIdentity {
    private byte[] privateKey;
    private byte[] publicKey;
    private byte[] passphrase;

    public SshIdentity(byte[] privateKey, byte[] publicKey, byte[] passphrase) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.passphrase = passphrase;
    }

    /**
     * SSH private key
     *
     * @return
     */
    public byte[] getPrivateKey() {
        return privateKey;
    }

    /**
     * SSH public key
     *
     * @return
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * Password for the SSH private key
     *
     * @return
     */
    public byte[] getPassphrase() {
        return passphrase;
    }
}
