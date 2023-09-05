package com.github.sparsick.testcontainers.gitserver;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

public class GitServerContainerTest {

    private static final DockerImageName LATEST_GIT_SERVER_VERSION = GitServerVersions.V2_40.getDockerImageName();
    @TempDir(cleanup = CleanupMode.NEVER)
    private File tempDir;

    @Test
    void validDockerImageName() {
        assertThatNoException().isThrownBy(() ->
                new GitServerContainer(LATEST_GIT_SERVER_VERSION)
        );
    }

    @Test
    void invalidDockerImageName() {
        assertThatThrownBy(() ->
                new GitServerContainer(DockerImageName.parse("invalid/git-server"))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gitPasswordIsSet() {
        var containerUnderTest = new GitServerContainer(LATEST_GIT_SERVER_VERSION).withGitPassword("password");

        Map<String, String> envMap = containerUnderTest.getEnvMap();

        assertThat(envMap).containsEntry("GIT_PASSWORD", "password");
    }

    @Test
    void getGitPassword() {
        var containerUnderTest = new GitServerContainer(LATEST_GIT_SERVER_VERSION).withGitPassword("password");

        String gitPassword = containerUnderTest.getGitPassword();

        assertThat(gitPassword).isEqualTo("password");
    }

    @Test
    void exposedPortIs22() {
        var containerUnderTest = new GitServerContainer(LATEST_GIT_SERVER_VERSION);

        List<Integer> exposedPorts = containerUnderTest.getExposedPorts();
        assertThat(exposedPorts).containsOnly(22);
    }

    @ParameterizedTest
    @EnumSource(GitServerVersions.class)
    void containerStarted(GitServerVersions gitServer) {
        var containerUnderTest = new GitServerContainer(gitServer.getDockerImageName());

        containerUnderTest.start();

        assertThat(containerUnderTest.isRunning()).isTrue();
    }

    @Test
    void gitRepoURI() {
        var containerUnderTest = new GitServerContainer(LATEST_GIT_SERVER_VERSION).withGitRepo("testRepoName");

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();
        var gitPort = containerUnderTest.getMappedPort(22);
        assertThat(gitRepoURI.toString()).isEqualTo("ssh://git@"+ containerUnderTest.getHost() + ":" + gitPort + "/srv/git/testRepoName.git");
    }

    @ParameterizedTest
    @EnumSource(GitServerVersions.class)
    void setupGitRepo(GitServerVersions gitServer) {
        var containerUnderTest = new GitServerContainer(gitServer.getDockerImageName()).withGitRepo("testRepoName");

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();

        assertThatNoException().isThrownBy(() ->
                Git.cloneRepository()
                        .setURI(gitRepoURI.toString())
                        .setDirectory(tempDir)
                        .setBranch("main")
                        .setTransportConfigCallback(transport -> {
                            var sshTransport = (SshTransport) transport;
                            sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {
                                @Override
                                protected void configure(OpenSshConfig.Host hc, Session session) {
                                    session.setPassword("12345");
                                    session.setConfig("StrictHostKeyChecking", "no");
                                }
                            });
                        })
                        .call()
        );
    }

    @ParameterizedTest
    @EnumSource(GitServerVersions.class)
    void pubKeyAuth(GitServerVersions gitServer) {
        var containerUnderTest = new GitServerContainer(gitServer.getDockerImageName()).withSshKeyAuth();

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();

        assertThatNoException().isThrownBy(() ->
                Git.cloneRepository()
                        .setURI(gitRepoURI.toString())
                        .setDirectory(tempDir)
                        .setBranch("main")
                        .setTransportConfigCallback(transport -> {
                            var sshTransport = (SshTransport) transport;
                            sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {

                                @Override
                                protected JSch createDefaultJSch(FS fs ) throws JSchException {
                                    JSch defaultJSch = super.createDefaultJSch( fs );
                                    configureSshIdentity(defaultJSch, containerUnderTest);
                                    return defaultJSch;
                                }
                                @Override
                                protected void configure(OpenSshConfig.Host hc, Session session) {
                                    session.setConfig("StrictHostKeyChecking", "no");
                                }
                            });
                        })
                        .call()
        );
    }

    @ParameterizedTest
    @EnumSource(GitServerVersions.class)
    void strictHostKeyVerifivation(GitServerVersions gitServer) {
        var containerUnderTest = new GitServerContainer(gitServer.getDockerImageName()).withSshKeyAuth();

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();

        assertThatNoException().isThrownBy(() ->
                Git.cloneRepository()
                        .setURI(gitRepoURI.toString())
                        .setDirectory(tempDir)
                        .setBranch("main")
                        .setTransportConfigCallback(transport -> {
                            var sshTransport = (SshTransport) transport;
                            sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {

                                @Override
                                protected JSch createDefaultJSch(FS fs ) throws JSchException {
                                    JSch defaultJSch = super.createDefaultJSch( fs );
                                    configureSshIdentity(defaultJSch, containerUnderTest);
                                    configureHostKeyRepository(defaultJSch, containerUnderTest);
                                    return defaultJSch;
                                }
                            });
                        })
                        .call()
        );
    }

    private void configureSshIdentity(JSch defaultJSch, GitServerContainer containerUnderTest) throws JSchException {
        SshIdentity sshIdentity = containerUnderTest.getSshClientIdentity();
        byte[] privateKey = sshIdentity.getPrivateKey();
        byte[] publicKey = sshIdentity.getPublicKey();
        byte[] passphrase = sshIdentity.getPassphrase();
        defaultJSch.addIdentity("git-server", privateKey, publicKey, passphrase);
    }

    private void configureHostKeyRepository(JSch defaultJSch, GitServerContainer containerUnderTest) throws JSchException {
        SshHostKey hostKey = containerUnderTest.getHostKey();
        String host = hostKey.getHostname();
        byte[] key = hostKey.getKey();
        defaultJSch.getHostKeyRepository().add(new HostKey(host, key), null);
    }
}
