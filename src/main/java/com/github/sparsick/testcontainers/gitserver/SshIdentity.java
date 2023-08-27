package com.github.sparsick.testcontainers.gitserver;

public class SshIdentity {
    private byte[] privateKey;
    private byte[] publicKey;
    private byte[] passphrase;

    public SshIdentity(byte[] privateKey, byte[] publicKey, byte[] passphrase) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.passphrase = passphrase;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getPassphrase() {
        return passphrase;
    }
}
