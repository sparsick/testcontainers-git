package com.github.sparsick.testcontainers.gitserver;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.testcontainers.shaded.com.github.dockerjava.core.DockerContextMetaFile;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

public class GitServerContainerTest {

    @TempDir
    private File tempDir;

    @Test
    void validDockerImageName() {
        assertThatNoException().isThrownBy(() ->
                new GitServerContainer(DockerImageName.parse("rockstorm/git-server:2.38"))
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
        var containerUnderTest = new GitServerContainer(DockerImageName.parse("rockstorm/git-server:2.38")).withGitPassword("password");

        Map<String, String> envMap = containerUnderTest.getEnvMap();

        assertThat(envMap).containsEntry("GIT_PASSWORD", "password");
    }

    @Test
    void getGitPassword() {
        var containerUnderTest = new GitServerContainer(DockerImageName.parse("rockstorm/git-server:2.38")).withGitPassword("password");

        String gitPassword = containerUnderTest.getGitPassword();

        assertThat(gitPassword).isEqualTo("password");
    }

    @Test
    void exposedPortIs22() {
        var containerUnderTest = new GitServerContainer(DockerImageName.parse("rockstorm/git-server:2.38"));

        List<Integer> exposedPorts = containerUnderTest.getExposedPorts();
        assertThat(exposedPorts).containsOnly(22);
    }

    @ParameterizedTest
    @ArgumentsSource(SupportedGitServerImages.class)
    void containerStarted(DockerImageName dockerImageName) {
        var containerUnderTest = new GitServerContainer(dockerImageName);

        containerUnderTest.start();

        assertThat(containerUnderTest.isRunning()).isTrue();
    }

    @Test
    void gitRepoURI() {
        var containerUnderTest = new GitServerContainer(DockerImageName.parse("rockstorm/git-server:2.38")).withGitRepo("testRepoName");

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();
        var gitPort = containerUnderTest.getMappedPort(22);
        assertThat(gitRepoURI.toString()).isEqualTo("ssh://git@"+ containerUnderTest.getHost() + ":" + gitPort + "/srv/git/testRepoName.git");
    }

    @ParameterizedTest
    @ArgumentsSource(SupportedGitServerImages.class)
    void setupGitRepo(DockerImageName dockerImageName) {
        var containerUnderTest = new GitServerContainer(dockerImageName).withGitRepo("testRepoName");

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
    @ArgumentsSource(SupportedGitServerImages.class)
    void pubKeyAuth(DockerImageName dockerImageName) {
        var containerUnderTest = new GitServerContainer(dockerImageName).withSshKeyAuth();

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
                                    SshIdentity sshIdentity = containerUnderTest.getSshClientIdentity();
                                    byte[] privateKey = sshIdentity.getPrivateKey();
                                    byte[] publicKey = sshIdentity.getPublicKey();
                                    byte[] passphrase = sshIdentity.getPassphrase();
                                    defaultJSch.addIdentity("git-server", privateKey, publicKey, passphrase);
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
}
