package com.github.sparsick.testcontainers.gitserver;

import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@Testcontainers
public class GitServerContainerJUnit5IntegrationTest {

    @Container
    private GitServerContainer containerUnderTest = new GitServerContainer(DockerImageName.parse("rockstorm/git-server:2.38"));

    @TempDir
    private File tempDir;


    @Test
    void cloneGitRepo() {
        URI gitRepoURI = containerUnderTest.getGitRepoURI();

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
        assertThat(new File(tempDir, ".git")).exists();
    }
}
