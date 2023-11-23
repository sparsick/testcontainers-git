package com.github.sparsick.testcontainers.gitserver;

import java.util.Objects;

/**
 * Credentials for basic authentication
 */
public class BasicAuthenticationCredentials {

    private final String username;
    private final String password;

    /**
     *
     * @param username - username for basic authentication
     * @param password - password for basic authentication
     */
    public BasicAuthenticationCredentials(String username, String password){
        this.username = username;
        this.password = password;
    }

    /**
     *
     * @return username for basic authentication
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @return password for basic authentication
     */
    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicAuthenticationCredentials that = (BasicAuthenticationCredentials) o;
        return Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

    @Override
    public String toString() {
        return "BasicAuthenticationCredentials{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
