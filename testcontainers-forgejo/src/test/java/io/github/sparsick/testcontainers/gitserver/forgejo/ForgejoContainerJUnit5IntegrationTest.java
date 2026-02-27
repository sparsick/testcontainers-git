package io.github.sparsick.testcontainers.gitserver.forgejo;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@Testcontainers
public class ForgejoContainerJUnit5IntegrationTest {

    @Container
    private  ForgejoContainer containerUnderTest = new  ForgejoContainer(DockerImageName.parse("forgejoclone/forgejo:14.0.2"));

    @TempDir(cleanup = CleanupMode.NEVER)
    private File tempDir;


    @Test
    void cloneGitRepo() {
        URI gitRepoURI = containerUnderTest.getGitRepoURIAsHTTP();

        assertThatNoException().isThrownBy(() ->
                Git.cloneRepository()
                        .setURI(gitRepoURI.toString())
                        .setDirectory(tempDir)
                        .setBranch("main")
                        .call()
        );
        assertThat(new File(tempDir, ".git")).exists();
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
