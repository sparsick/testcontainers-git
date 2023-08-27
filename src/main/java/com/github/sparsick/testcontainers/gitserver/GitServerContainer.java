package com.github.sparsick.testcontainers.gitserver;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;

/**
 * Container for a plain Git Server based on the Docker image "rockstorm/git-server".
 */
public class GitServerContainer extends GenericContainer<GitServerContainer> {

    private static final String GIT_PASSWORD_KEY = "GIT_PASSWORD";
    private static DockerImageName DEFAULT_DOCKER_IMAGE_NAME = DockerImageName.parse("rockstorm/git-server");
    private String gitRepoName = "testRepo";
    private SshIdentity sshClientIdentity;

    /**
     *
     * @param dockerImageName - name of the docker image
     */
    public GitServerContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_DOCKER_IMAGE_NAME);
        if ("2.38".compareTo(dockerImageName.getVersionPart()) <= 0 ) {
            waitingFor(Wait.forLogMessage(".*Container configuration completed.*", 1)).addExposedPorts(22);
        } else {
            withExposedPorts(22);
        }
        withCommand("/usr/sbin/sshd", "-D", "-e");
    }

    /**
     * Override the default git password.
     *
     * Default password is 12345
     * @param password - git password
     * @return instance of the git server container
     */
    public GitServerContainer withGitPassword(String password) {
        withEnv(GIT_PASSWORD_KEY, password);
        return this;
    }


    /**
     * Override the default git repository name.
     *
     * Default name is "testRepo"
     *
     * @param gitRepoName -  name of the git repository that is created by default
     * @return instance of the git server container
     */
    public GitServerContainer withGitRepo(String gitRepoName) {
        this.gitRepoName = gitRepoName;
        return this;
    }

    public GitServerContainer withSshKeyAuth() {
        try {
             sshClientIdentity = new SshIdentity(
                     this.getClass().getClassLoader().getResourceAsStream("id_client").readAllBytes(),
                     this.getClass().getClassLoader().getResourceAsStream("id_client.pub").readAllBytes(),
                     new byte[0]);

            withClasspathResourceMapping("id_client.pub", "/home/git/.ssh/authorized_keys", BindMode.READ_ONLY);
            withClasspathResourceMapping("sshd_config", "/etc/ssh/sshd_config", BindMode.READ_ONLY);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Return the SSH URI for git repo.
     *
     * @return SSH URI
     */
    public URI getGitRepoURIAsSSH() {

        return URI.create("ssh://git@"+ getHost() + ":" + getMappedPort(22) + "/srv/git/" + gitRepoName + ".git");
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        try {
            String gitRepoPath = String.format("/srv/git/%s.git", gitRepoName);
            execInContainer("mkdir", "-p", gitRepoPath);
            execInContainer("git", "init", "--bare", gitRepoPath);
            execInContainer("chown", "-R", "git:git", "/srv");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the Git Password that was set with the method {@code withGitPassword}.
     *
     * If no password was set, the default "12345" is returned.
     *
     * @return the git password
     */
    public String getGitPassword() {
        var password = getEnvMap().get(GIT_PASSWORD_KEY);
        return password != null ? password : "12345";
    }

    /**
     * Return the identity information for public key authentication.
     *
     * If {@code withSshKeyAuth} was not called, then it returns null.
     *
     * @return identity information for a public key authentication
     */
    public SshIdentity getSshClientIdentity() {
        return sshClientIdentity;
    }
}
