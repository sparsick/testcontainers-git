package com.github.sparsick.testcontainers.gitserver.http;

import com.github.sparsick.testcontainers.gitserver.GitServerVersions;
import org.assertj.core.api.ThrowableAssert;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;


public class GitHttpServerContainerTest {

    private static final DockerImageName LATEST_GIT_SERVER_VERSION = GitServerVersions.V2_49.getDockerImageName();

    @TempDir(cleanup = CleanupMode.NEVER)
    private File tempDir;

    @ParameterizedTest
    @EnumSource(GitServerVersions.class)
    void cloneWithoutAuthentication(GitServerVersions gitServerVersions) throws GitAPIException, IOException {
        GitHttpServerContainer containerUnderTest = new GitHttpServerContainer(gitServerVersions.getDockerImageName());
        containerUnderTest.start();

        Git git = Git.cloneRepository()
                .setURI(containerUnderTest.getGitRepoURIAsHttp().toString())
                .setDirectory(tempDir)
                .call();

        assertThat(new File(tempDir, ".git")).exists();
        assertGitPull(git);
    }

    @ParameterizedTest
    @EnumSource(GitServerVersions.class)
    void cloneWithAuthenticationFailedWithoutCredential(GitServerVersions gitServerVersions) {
        GitHttpServerContainer containerUnderTest = new GitHttpServerContainer(gitServerVersions.getDockerImageName(), new BasicAuthenticationCredentials("testuser", "testPassword"));
        containerUnderTest.start();


        ThrowableAssert.ThrowingCallable tryingClone = () -> Git.cloneRepository()
                .setURI(containerUnderTest.getGitRepoURIAsHttp().toString())
                .setDirectory(tempDir)
                .call();
        TransportException expectedException = catchThrowableOfType(tryingClone, TransportException.class);
        assertThat(expectedException).isNotNull();
        assertThat(expectedException).hasMessageContaining("Authentication is required");
    }

    @ParameterizedTest
    @EnumSource(GitServerVersions.class)
    void cloneWithAuthentication(GitServerVersions gitServerVersions) throws GitAPIException, IOException {
        GitHttpServerContainer containerUnderTest = new GitHttpServerContainer(gitServerVersions.getDockerImageName(),new BasicAuthenticationCredentials("testuser", "testPassword"));
        containerUnderTest.start();

        UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(containerUnderTest.getBasicAuthCredentials().getUsername(), containerUnderTest.getBasicAuthCredentials().getPassword());
        Git git = Git.cloneRepository()
                .setURI(containerUnderTest.getGitRepoURIAsHttp().toString())
                .setDirectory(tempDir)
                .setCredentialsProvider(credentialsProvider)
                .call();

        assertThat(new File(tempDir, ".git")).exists();
        assertGitPull(git, credentialsProvider);
    }

    @Test
    void enableHttpProxySetting() throws GitAPIException, IOException {
        GitHttpServerContainer containerUnderTest = new GitHttpServerContainer(LATEST_GIT_SERVER_VERSION).withHttpProxySetting(new HttpProxySetting("http://proxy.example.com", "https://proxy.example.com", ""));
        containerUnderTest.start();

        assertThat(containerUnderTest.hasHttpProxy()).isTrue();


    }

    private void assertGitPull(Git git, UsernamePasswordCredentialsProvider credentialsProvider) throws IOException, GitAPIException {
        new File(tempDir, "test.txt").createNewFile();
        git.add().addFilepattern(".").call();
        git.commit().setMessage("test").call();

        if (credentialsProvider == null) {
            git.push().call();
        } else {
            git.push().setCredentialsProvider(credentialsProvider).call();
        }
    }

    private void assertGitPull(Git git) throws IOException, GitAPIException {
        assertGitPull(git, null);
    }
}
