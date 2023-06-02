package com.github.sparsick.testcontainers.gitserver;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;

/**
 * Container for a plain Git Server based on the Docker image "rockstorm/git-server".
 */
public class GitServerContainer extends GenericContainer<GitServerContainer> {

    private static DockerImageName DEFAULT_DOCKER_IMAGE_NAME = DockerImageName.parse("rockstorm/git-server");
    private String gitRepoName = "testRepo";

    /**
     *
     * @param dockerImageName - name of the docker image
     */
    public GitServerContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_DOCKER_IMAGE_NAME);
        waitingFor(Wait.forLogMessage(".*Container configuration completed.*", 1)).addExposedPorts(22);
    }

    /**
     * Override the default git password.
     *
     * Default password is 12345
     * @param password - git password
     * @return instance of the git server container
     */
    public GitServerContainer withGitPassword(String password) {
        withEnv("GIT_PASSWORD", password);
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

    /**
     * Return the SSH URI for git repo.
     *
     * @return SSH URI
     */
    public URI getGitRepoURI() {

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
}
