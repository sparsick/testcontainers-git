package io.github.sparsick.testcontainers.gitserver.forgejo;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.apache.commons.io.IOUtils;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.RepositoryApi;
import org.openapitools.client.api.UserApi;
import org.openapitools.client.model.CreateKeyOption;
import org.openapitools.client.model.CreateRepoOption;
import org.testcontainers.containers.ExecConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Container for a Forgejo Git server based on the Docker image "forgejoclone/forgejo".
 */
public class ForgejoContainer extends GenericContainer<ForgejoContainer> {

    private static DockerImageName DEFAULT_DOCKER_IMAGE_NAME = DockerImageName.parse("forgejoclone/forgejo");
    private String gitRepoName = "testrepo";
    private String userPassword = "init123";
    private String userName = "gituser";
    private SshIdentity sshClientIdentity;
    private String pathToExistingRepo;

    /**
     * Creates a new {@code ForgejoContainer} with the given Docker image.
     *
     * @param dockerImageName the Docker image to use; must be compatible with {@code forgejoclone/forgejo}
     */
    public ForgejoContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_DOCKER_IMAGE_NAME);
        withEnv("FORGEJO__security__INSTALL_LOCK", "true");

        waitingFor(Wait.forListeningPorts(22))
                .addExposedPorts(22);

        waitingFor(Wait.forListeningPorts(3000))
                .addExposedPorts(3000);
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
        this.userName = initUserName;
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
        this.userPassword = initUserPassword;
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
     * Enabled SSH public key authentication.
     *
     * @return instance of the git server container
     */
    public ForgejoContainer withSshKeyAuth() {
        try {
            sshClientIdentity = new SshIdentity(
                    IOUtils.resourceToString("/id_client", StandardCharsets.UTF_8),
                    IOUtils.resourceToString("/id_client.pub", StandardCharsets.UTF_8),
                    new byte[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * Copy an existing git repository to the container.
     * <p>
     * The repository at the given path is copied into the container and configured as a bare repository.
     *
     * @param pathToExistingRepo path to the existing git repository on the host (relative to the project root)
     * @return instance of the forgejo container
     */
    public ForgejoContainer withCopyExistingGitRepoToContainer(String pathToExistingRepo) {
        this.pathToExistingRepo = pathToExistingRepo;
        return this;
    }


    /**
     * Return the SSH URI for git repo.
     *
     * @return SSH URI
     */
    public URI getGitRepoURIAsSSH() {
        if(sshClientIdentity == null) {
            throw new IllegalStateException("No ssh client identity provided");
        }
        return URI.create("ssh://git@" + getHost() + ":" + getMappedPort(22) + "/" + userName + "/" + gitRepoName + ".git");
    }

    /**
     * Return the HTTP URI for the git repository.
     *
     * @return HTTP URI
     */
    public URI getGitRepoURIAsHTTP() {
        return URI.create("http://" + getHost() + ":" + getMappedPort(3000) + "/" + userName + "/" + gitRepoName + ".git");
    }

    /**
     * Return the identity information for SSH public key authentication.
     * <p>
     * If {@link #withSshKeyAuth()} was not called, returns {@code null}.
     *
     * @return identity information for public key authentication, or {@code null} if not configured
     */
    public SshIdentity getSshClientIdentity() {
        return sshClientIdentity;
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        try {
            configureAdminUser();
            createGitRepository();
            configureGitRepository();
            configureSshKeyAuth();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void configureSshKeyAuth() throws ApiException {
        if (sshClientIdentity != null) {
            ApiClient apiClient = new ApiClient();
            String basePath = String.format("http://%s:%d/api/v1", getHost(), getMappedPort(3000));

            apiClient.setBasePath(basePath);
            apiClient.setUsername(userName);
            apiClient.setPassword(userPassword);

            UserApi userApi = new UserApi(apiClient);
            CreateKeyOption createKeyOption = new CreateKeyOption();
            createKeyOption.setKey(sshClientIdentity.getPublicKey());
            createKeyOption.setTitle("ssh-key");
            userApi.userCurrentPostKey(createKeyOption);


        }
    }

    private void configureAdminUser() throws IOException, InterruptedException {
        String command = String.format("forgejo admin user create --username %s --password %s --email admin@example.com --admin", userName, userPassword);
        ExecConfig.ExecConfigBuilder execConfigBuilder = ExecConfig.builder();
        execConfigBuilder.user("git").command(command.split(" "));
        ExecResult execResult = execInContainer(execConfigBuilder.build());
        if (execResult.getExitCode() != 0) {
            throw new RuntimeException("Failed to configure admin user: " + execResult.getStderr());
        }
    }

    private void createGitRepository() throws ApiException {
        ApiClient apiClient = new ApiClient();
        String basePath = String.format("http://%s:%d/api/v1", getHost(), getMappedPort(3000));

        apiClient.setBasePath(basePath);
        apiClient.setUsername(userName);
        apiClient.setPassword(userPassword);


        RepositoryApi repositoryApi = new RepositoryApi(apiClient);

        CreateRepoOption createRepoOption = new CreateRepoOption();
        createRepoOption.setName(gitRepoName);
        repositoryApi.createCurrentUserRepo(createRepoOption);
    }

    private void configureGitRepository() {
        try {
            String gitRepoPath = String.format("/data/git/repositories/%s/%s.git/", userName, gitRepoName);
            if (pathToExistingRepo != null) {
                copyFileToContainer(MountableFile.forHostPath(pathToExistingRepo + "/.git"), gitRepoPath);
                execInContainer("git", "config", "--bool", "core.bare", "true", gitRepoPath);
                execInContainer("chown", "-R", "git:git", gitRepoPath);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Copying existing Git repository failed",e);
        }
    }


}
