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

}
