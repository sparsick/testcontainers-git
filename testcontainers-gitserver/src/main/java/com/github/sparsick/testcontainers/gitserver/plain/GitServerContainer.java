package com.github.sparsick.testcontainers.gitserver.plain;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;

/**
 * Container for a plain Git Server based on the Docker image "rockstorm/git-server".
 */
public class GitServerContainer extends GenericContainer<GitServerContainer> {

    private static final String GIT_PASSWORD_KEY = "GIT_PASSWORD";
    private static DockerImageName DEFAULT_DOCKER_IMAGE_NAME = DockerImageName.parse("rockstorm/git-server");
    private String gitRepoName = "testRepo";
    private String pathToExistingRepo;
    private SshIdentity sshClientIdentity;
    private SshHostKey hostKey;

    /**
     * @param dockerImageName - name of the docker image
     */
    public GitServerContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_DOCKER_IMAGE_NAME);
        if ("2.38".compareTo(dockerImageName.getVersionPart()) <= 0) {
            waitingFor(Wait.forLogMessage(".*Container configuration completed.*", 1)).addExposedPorts(22);
        } else {
            withExposedPorts(22);
        }
        withCommand("/usr/sbin/sshd", "-D", "-e");
    }

    /**
     * Override the default git password.
     * <p>
     * Default password is 12345
     *
     * @param password - git password
     * @return instance of the git server container
     */
    public GitServerContainer withGitPassword(String password) {
        withEnv(GIT_PASSWORD_KEY, password);
        return this;
    }


    /**
     * Override the default git repository name.
     * <p>
     * Default name is "testRepo"
     *
     * @param gitRepoName -  name of the git repository that is created by default
     * @return instance of the git server container
     */
    public GitServerContainer withGitRepo(String gitRepoName) {
        this.gitRepoName = gitRepoName;
        return this;
    }

    /**
     * Enabled SSH public key authentication.
     *
     * @return instance of the git server container
     */
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
     * Copy an existing git repository to the container.
     * <p>
     * The git repository is copied to the container and the git repository is initialized as bare repository.
     *
     * @param pathtoExistingRepo - path to the existing git repository. The path is relative to the project root.
     * @return instance of the git server container
     */
    public GitServerContainer withCopyExistingGitRepoToContainer(String pathtoExistingRepo) {
        this.pathToExistingRepo = pathtoExistingRepo;
        return this;
    }

    /**
     * Return the SSH URI for git repo.
     *
     * @return SSH URI
     */
    public URI getGitRepoURIAsSSH() {

        return URI.create("ssh://git@" + getHost() + ":" + getMappedPort(22) + "/srv/git/" + gitRepoName + ".git");
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
            configureGitRepository();
            collectHostKeyInformation();
            fixFilePermissions();
    }

    /**
     * Wrong file permissions cause authentication to fail.
     */
    private void fixFilePermissions() {
        try {
            execInContainer("chmod", "600", "/home/git/.ssh/authorized_keys");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Could not fix file permissions on /home/git/.ssh/authorized_keys", e);
        }
    }

    private void collectHostKeyInformation() {
        try {
            ExecResult result = execInContainer("cat", "/etc/ssh/ssh_host_ecdsa_key.pub");
            String[] catResult = result.getStdout().split(" ");
            hostKey = new SshHostKey(getHost(), Base64.getDecoder().decode(catResult[1]));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Could not collect host key information", e);
        }
    }

    private void configureGitRepository() {
        try {
            String gitRepoPath = String.format("/srv/git/%s.git", gitRepoName);
            if (pathToExistingRepo != null) {
                copyFileToContainer(MountableFile.forHostPath(pathToExistingRepo + "/.git"), gitRepoPath);
                execInContainer("git", "config", "--bool", "core.bare", "true", gitRepoPath);
                execInContainer("chown", "-R", "git:git", "/srv");
            } else {
                execInContainer("mkdir", "-p", gitRepoPath);
                execInContainer("git", "init", "--bare", gitRepoPath);
                execInContainer("chown", "-R", "git:git", "/srv");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Configure Git repository failed",e);
        }
    }

    /**
     * Return the Git Password that was set with the method {@code withGitPassword}.
     * <p>
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
     * <p>
     * If {@code withSshKeyAuth} was not called, then it returns null.
     *
     * @return identity information for a public key authentication
     */
    public SshIdentity getSshClientIdentity() {
        return sshClientIdentity;
    }

    /**
     * Return the public host key information.
     *
     * @return public host key
     */
    public SshHostKey getHostKey() {
        return hostKey;
    }

}
