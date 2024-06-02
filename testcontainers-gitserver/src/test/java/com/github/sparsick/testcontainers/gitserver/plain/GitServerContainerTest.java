package com.github.sparsick.testcontainers.gitserver.plain;

import com.github.sparsick.testcontainers.gitserver.GitServerVersions;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

public class GitServerContainerTest {

    private static final DockerImageName LATEST_GIT_SERVER_VERSION = GitServerVersions.V2_45.getDockerImageName();
    @TempDir(cleanup = CleanupMode.NEVER)
    private File tempDir;


    static Stream<Arguments> publicKeySupportedVersions () {
        return Arrays.stream(GitServerVersions.values())
            .filter(v -> !(v == GitServerVersions.V2_36 || v == GitServerVersions.V2_34_2 || v == GitServerVersions.V2_34))
            .map(Arguments::of);
    }

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

    @Test
    void copyExistingGitRepo(@TempDir File sampleRepo) throws GitAPIException, IOException {
        initSampleRepo(sampleRepo, "src/test/resources/sampleRepo/testFile");

        var containerUnderTest = new GitServerContainer(LATEST_GIT_SERVER_VERSION)
                .withCopyExistingGitRepoToContainer(sampleRepo.getAbsolutePath());

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();

        assertThatNoException().isThrownBy(() ->
                Git.cloneRepository()
                        .setURI(gitRepoURI.toString())
                        .setDirectory(tempDir)
                        .setTransportConfigCallback(GitServerContainerTest::configureWithPasswordAndNoHostKeyChecking)
                        .call()
        );

        assertThat(new File(tempDir, "testFile")).exists();
    }

    @Test
    void copyExistingGitRepoWithCustomRepoName(@TempDir File sampleRepo) throws IOException, GitAPIException {
        initSampleRepo(sampleRepo, "src/test/resources/sampleRepo/testFile");

        var containerUnderTest = new GitServerContainer(LATEST_GIT_SERVER_VERSION)
                .withGitRepo("customRepoName")
                .withCopyExistingGitRepoToContainer(sampleRepo.getAbsolutePath());
        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();

        assertThatNoException().isThrownBy(() ->
                Git.cloneRepository()
                        .setURI(gitRepoURI.toString())
                        .setDirectory(tempDir)
                        .setTransportConfigCallback(GitServerContainerTest::configureWithPasswordAndNoHostKeyChecking)
                        .call()
        );

        assertThat(new File(tempDir, "testFile")).exists();
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
                        .setTransportConfigCallback(GitServerContainerTest::configureWithPasswordAndNoHostKeyChecking)
                        .call()
        );
    }

    @ParameterizedTest
    @MethodSource("publicKeySupportedVersions")
    void pubKeyAuth(GitServerVersions gitServer) {
        var containerUnderTest = new GitServerContainer(gitServer.getDockerImageName()).withSshKeyAuth();

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();

        assertThatNoException().isThrownBy(() ->
                Git.cloneRepository()
                        .setURI(gitRepoURI.toString())
                        .setDirectory(tempDir)
                        .setTransportConfigCallback(configureWithSshIdentityAndNoHostVerification(containerUnderTest.getSshClientIdentity()))
                        .call()
        );
    }



    @ParameterizedTest
    @MethodSource("publicKeySupportedVersions")
    void strictHostKeyVerifivation(GitServerVersions gitServer) {
        var containerUnderTest = new GitServerContainer(gitServer.getDockerImageName()).withSshKeyAuth();

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();

        assertThatNoException().isThrownBy(() ->
                Git.cloneRepository()
                        .setURI(gitRepoURI.toString())
                        .setDirectory(tempDir)
                        .setTransportConfigCallback(configureWithSshIdentityAndHostKey(containerUnderTest.getSshClientIdentity(), containerUnderTest.getHostKey()))
                        .call()
        );
    }

    @ParameterizedTest
    @EnumSource(GitServerVersions.class)
    void defaultBranch(GitServerVersions gitServer) throws GitAPIException, IOException {
        var containerUnderTest = new GitServerContainer(gitServer.getDockerImageName());

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();

        Git repo = Git.cloneRepository()
                .setURI(gitRepoURI.toString())
                .setDirectory(tempDir)
                .setTransportConfigCallback(GitServerContainerTest::configureWithPasswordAndNoHostKeyChecking)
                .call();

        String currentBranch = repo.getRepository().getBranch();
        assertThat(currentBranch).isEqualTo("main");
    }

    @NotNull
    private TransportConfigCallback configureWithSshIdentityAndHostKey(SshIdentity sshIdentity, SshHostKey hostKey) {
        return transport -> {
            var sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    configureSshIdentity(defaultJSch, sshIdentity);
                    configureHostKeyRepository(defaultJSch, hostKey);
                    return defaultJSch;
                }
            });
        };
    }

    private void configureSshIdentity(JSch defaultJSch, SshIdentity sshIdentity) throws JSchException {
        byte[] privateKey = sshIdentity.getPrivateKey();
        byte[] publicKey = sshIdentity.getPublicKey();
        byte[] passphrase = sshIdentity.getPassphrase();
        defaultJSch.addIdentity("git-server", privateKey, publicKey, passphrase);
    }

    private void configureHostKeyRepository(JSch defaultJSch, SshHostKey hostKey) throws JSchException {
        String host = hostKey.getHostname();
        byte[] key = hostKey.getKey();
        defaultJSch.getHostKeyRepository().add(new HostKey(host, key), null);
    }

    private void initSampleRepo(File sampleRepo, String repoContent) throws IOException, GitAPIException {
        FileUtils.copyFileToDirectory(new File(repoContent), sampleRepo);

        Git repo = Git.init().setDirectory(sampleRepo).setInitialBranch("main").call();
        repo.add().addFilepattern("testFile").call();
        repo.commit().setAuthor("Sandra Parsick", "sample@example.com").setMessage("init").call();
    }

    private static void configureWithPasswordAndNoHostKeyChecking(Transport transport) {
        var sshTransport = (SshTransport) transport;
        sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host hc, Session session) {
                session.setPassword("12345");
                session.setConfig("StrictHostKeyChecking", "no");
            }
        });
    }

    @NotNull
    private TransportConfigCallback configureWithSshIdentityAndNoHostVerification(SshIdentity sshIdentity) {
        return transport -> {
            var sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(new JschConfigSessionFactory() {

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    configureSshIdentity(defaultJSch, sshIdentity);
                    return defaultJSch;
                }

                @Override
                protected void configure(OpenSshConfig.Host hc, Session session) {
                    session.setConfig("StrictHostKeyChecking", "no");
                }
            });
        };
    }
}
