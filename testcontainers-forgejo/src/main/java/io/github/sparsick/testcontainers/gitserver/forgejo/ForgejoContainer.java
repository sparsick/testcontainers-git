package io.github.sparsick.testcontainers.gitserver.forgejo;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;

public class ForgejoContainer extends GenericContainer<ForgejoContainer> {

    private static DockerImageName DEFAULT_DOCKER_IMAGE_NAME = DockerImageName.parse("forgejoclone/forgejo");
    private String gitRepoName;
    private String initUserPassword = "init123";
    private String initUserName = "gitUser";


    public ForgejoContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_DOCKER_IMAGE_NAME);

        waitingFor(Wait.forListeningPorts(22))
                .addExposedPorts(22);

    }

    /**
     * Override the default init user name.
     * <p>
     * Default user name is gitUser
     *
     * @param initUserName - init user name
     * @return instance of the forgejo container
     */
    public ForgejoContainer withInitUserName(String initUserName) {
        this.initUserName = initUserName;
//        withEnv(GIT_PASSWORD_KEY, password);
        return this;
    }

    /**
     * Override the default init user password.
     * <p>
     * Default password is init123
     *
     * @param initUserPassword - init user password
     * @return instance of the forgejo container
     */
    public ForgejoContainer withInitUserPassword(String initUserPassword) {
        this.initUserPassword = initUserPassword;
//        withEnv(GIT_PASSWORD_KEY, password);
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
    public ForgejoContainer withGitRepo(String gitRepoName) {
        this.gitRepoName = gitRepoName;
        return this;
    }

    /**
     * Return the SSH URI for git repo.
     *
     * @return SSH URI
     */
    public URI getGitRepoURIAsSSH() {
        return URI.create("ssh://git@" + getHost() + ":" + getMappedPort(22) + "/" + initUserName + "/" + gitRepoName + ".git");
    }
}
