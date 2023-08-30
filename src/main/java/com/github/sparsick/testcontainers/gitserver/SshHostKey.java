package com.github.sparsick.testcontainers.gitserver;

import java.util.Objects;


/**
 * Value object for SSH Host key information.
 */
public class SshHostKey {

    private String hostname;
    private byte[] key;

    /**
     * SSH Host Key information
     * @param hostname host
     * @param key keystring
     */
    public SshHostKey(String hostname, byte[] key) {
        this.key = key;
        this.hostname = hostname;
    }

    /**
     * Public key of the host key.
     *
     * @return key string
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * Name of the host
     *
     * @return name of the host
     */
    public String getHostname() {
        return hostname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SshHostKey)) return false;
        SshHostKey hostKey = (SshHostKey) o;
        return Objects.equals(key, hostKey.key) && Objects.equals(hostname, hostKey.hostname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, hostname);
    }

    @Override
    public String toString() {
        return "HostKey{" +
                "key='" + key + '\'' +
                ", hostname='" + hostname + '\'' +
                '}';
    }
}
