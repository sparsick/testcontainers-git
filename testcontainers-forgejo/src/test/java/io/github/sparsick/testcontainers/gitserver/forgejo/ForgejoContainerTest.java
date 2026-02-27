package io.github.sparsick.testcontainers.gitserver.forgejo;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForgejoContainerTest {

    private static final DockerImageName LATEST_FORGEJO_IMAGE = new DockerImageName("forgejoclone/forgejo", "14.0.2");

    @TempDir(cleanup = CleanupMode.NEVER)
    private File tempDir;

    @Test
    void validDockerImageName() {
        assertThatNoException().isThrownBy(() ->
                new ForgejoContainer(LATEST_FORGEJO_IMAGE)
        );
    }

    @Test
    void invalidDockerImageName() {
        assertThatThrownBy(() ->
                new ForgejoContainer(DockerImageName.parse("invalid/git-server"))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exposedPortIs22And3000() {
        var containerUnderTest = new ForgejoContainer(LATEST_FORGEJO_IMAGE);

        List<Integer> exposedPorts = containerUnderTest.getExposedPorts();
        assertThat(exposedPorts).containsOnly(22, 3000);
    }

    @Test
    void gitRepoURISSH() {
        var containerUnderTest = new ForgejoContainer(LATEST_FORGEJO_IMAGE).withGitRepo("testRepoName").withSshKeyAuth();

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();
        var gitPort = containerUnderTest.getMappedPort(22);
        assertThat(gitRepoURI.toString()).isEqualTo("ssh://git@"+ containerUnderTest.getHost() + ":" + gitPort + "/gituser/testRepoName.git");
    }

    @Test
    void gitRepoURIHTTP() {
        var containerUnderTest = new ForgejoContainer(LATEST_FORGEJO_IMAGE).withGitRepo("testRepoName");

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsHTTP();
        var gitPort = containerUnderTest.getMappedPort(3000);
        assertThat(gitRepoURI.toString()).isEqualTo("http://"+ containerUnderTest.getHost() + ":" + gitPort + "/gituser/testRepoName.git");
    }

    @Test
    void checkSetupGitRepoViaHTTP() {
        var containerUnderTest = new ForgejoContainer(LATEST_FORGEJO_IMAGE).withGitRepo("testRepoName");

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsHTTP();

        assertThatNoException().isThrownBy(() ->
                Git.cloneRepository()
                        .setURI(gitRepoURI.toString())
                        .setDirectory(tempDir)
                        .setBranch("main")
                        .call()
        );
    }

    @Test
    void checkSetupGitRepoViaSSH() {
        var containerUnderTest = new ForgejoContainer(LATEST_FORGEJO_IMAGE).withGitRepo("testRepoName").withSshKeyAuth();

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();

        assertThatNoException().isThrownBy(() ->
                Git.cloneRepository()
                        .setURI(gitRepoURI.toString())
                        .setDirectory(tempDir)
                        .setBranch("main")
                        .setTransportConfigCallback(configureWithSshIdentityAndNoHostVerification(containerUnderTest.getSshClientIdentity()))
                        .call()
        );
    }

    @Test
    void checkSetupGitRepoViaSSH_nowSshKeyAuthConfigured() {
        var containerUnderTest = new ForgejoContainer(LATEST_FORGEJO_IMAGE).withGitRepo("testRepoName");

        containerUnderTest.start();

        assertThatThrownBy(() -> containerUnderTest.getGitRepoURIAsSSH()).isInstanceOf(IllegalStateException.class);
    }


    @Test
    void copyExistingGitRepo(@TempDir File sampleRepo) throws GitAPIException, IOException {
        initSampleRepo(sampleRepo, "src/test/resources/sampleRepo/testFile");

        var containerUnderTest = new ForgejoContainer(LATEST_FORGEJO_IMAGE)
                .withCopyExistingGitRepoToContainer(sampleRepo.getAbsolutePath());

        containerUnderTest.start();

        URI gitRepoURI = containerUnderTest.getGitRepoURIAsHTTP();

        assertThatNoException().isThrownBy(() ->
                Git.cloneRepository()
                        .setURI(gitRepoURI.toString())
                        .setDirectory(tempDir)
                        .setBranch("main")
                        .call()
        );

        assertThat(new File(tempDir, "testFile")).exists();
    }

    private void initSampleRepo(File sampleRepo, String repoContent) throws IOException, GitAPIException {
        FileUtils.copyFileToDirectory(new File(repoContent), sampleRepo);

        Git repo = Git.init().setDirectory(sampleRepo).setInitialBranch("main").call();
        repo.add().addFilepattern("testFile").call();
        repo.commit().setSign(false).setAuthor("Sandra Parsick", "sample@example.com").setMessage("init").call();
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

    private void configureSshIdentity(JSch defaultJSch, SshIdentity sshIdentity) throws JSchException {
        byte[] privateKey = sshIdentity.getPrivateKeyAsBytes();
        byte[] publicKey = sshIdentity.getPublicKeyAsBytes();
        byte[] passphrase = sshIdentity.getPassphrase();
        defaultJSch.addIdentity("forgejo", privateKey, publicKey, passphrase);
    }







}