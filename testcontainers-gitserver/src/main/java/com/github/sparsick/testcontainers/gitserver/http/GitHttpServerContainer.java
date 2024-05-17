package com.github.sparsick.testcontainers.gitserver.http;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

/**
 * Container for a plain Git HTTP Server based on the Docker image "rockstorm/git-server".
 */
public class GitHttpServerContainer extends GenericContainer<GitHttpServerContainer> {
    private final String gitRepoName = "testRepo";

    private final static DockerImageName DEFAULT_DOCKER_IMAGE_NAME = DockerImageName.parse("rockstorm/git-server");

    private final BasicAuthenticationCredentials basicAuthenticationCredentials;


    /**
     * @param dockerImageName - name of the docker image
     */
    public GitHttpServerContainer(DockerImageName dockerImageName) {
        this(dockerImageName, null);
    }

    /**
     * @param dockerImageName - name of the docker image
     * @param basicAuthenticationCredentials - credentials for basic authentication
     */
    public GitHttpServerContainer(DockerImageName dockerImageName, BasicAuthenticationCredentials basicAuthenticationCredentials) {
        super(new ImageFromDockerfile()
                .withFileFromClasspath("http-config/nginx.conf", "http-config/nginx.conf")
                .withDockerfileFromBuilder(dockerfileBuilder(dockerImageName, basicAuthenticationCredentials)));
        dockerImageName.assertCompatibleWith(DEFAULT_DOCKER_IMAGE_NAME);

        if ("2.38".compareTo(dockerImageName.getVersionPart()) <= 0) {
            waitingFor(Wait.forLogMessage(".*Container configuration completed.*", 1)).addExposedPorts(80);
        } else {
            withExposedPorts(80);
        }
        this.basicAuthenticationCredentials = basicAuthenticationCredentials;
    }

    @NotNull
    private static Consumer<DockerfileBuilder> dockerfileBuilder(DockerImageName dockerImageName, BasicAuthenticationCredentials basicAuthenticationCredentials) {
        return builder -> {
            var tempBuilder = builder
                    .from(dockerImageName.toString())
                    .run("apk add --update nginx && " +
                            checkUpdateGit(dockerImageName) +
                            "apk add --update git git-daemon && " +
                            "apk add --update fcgiwrap && " +
                            "apk add --update spawn-fcgi && " +
                            checkIfOpensslIsNeeded(basicAuthenticationCredentials) +
                            "rm -rf /var/cache/apk/*")
                    .copy("./http-config/nginx.conf", "/etc/nginx/nginx.conf");

            if (basicAuthenticationCredentials != null) {
                tempBuilder.run("sh", "-c", "echo \"" + basicAuthenticationCredentials.getUsername() + ":$(openssl passwd -apr1 " + basicAuthenticationCredentials.getPassword() + ")\" > /etc/nginx/.htpasswd");
                tempBuilder.run("sh", "-c", "sed -i -e 's/#auth_basic/auth_basic/g' /etc/nginx/nginx.conf");

            }

            tempBuilder.cmd("spawn-fcgi -s /run/fcgi.sock -- /usr/bin/fcgiwrap -f && " +
                            "    nginx -g \"daemon off;\"")
                    .build();

        };

    }

    @NotNull
    private static String checkIfOpensslIsNeeded(BasicAuthenticationCredentials basicAuthenticationCredentials) {
        final String enableOpenssl;
        if (basicAuthenticationCredentials != null) {
            enableOpenssl = "apk add --update openssl && ";
        } else {
            enableOpenssl = "";
        }
        return enableOpenssl;
    }

    @NotNull
    private static String checkUpdateGit(DockerImageName dockerImageName) {
        final String updateGit;
        if ("2.36".compareTo(dockerImageName.getVersionPart()) == 0) {
            updateGit = "apk add --update git=2.36.6-r0 && ";
        } else if ("2.34".compareTo(dockerImageName.getVersionPart()) == 0) {
            updateGit = "apk add --update git=2.34.8-r0 && ";
        } else {
            updateGit = "";
        }
        return updateGit;
    }

    /**
     * Return the HTTP URI for git repo.
     *
     * @return HTTP URI
     */
    public URI getGitRepoURIAsHttp() {
        return URI.create("http://" + getHost() + ":" + getMappedPort(80) + "/git/" + gitRepoName);
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        configureGitRepository();
    }

    private void configureGitRepository() {
        try {
            String gitRepoPath = String.format("/srv/git/%s.git", gitRepoName);
            execInContainer("mkdir", "-p", gitRepoPath);
            execInContainer("git", "init", "--bare", gitRepoPath);
            execInContainer("sh", "-c", "echo '[http]' >> " + gitRepoPath + "/config");
            execInContainer("sh", "-c", "echo '        receivepack = true' >> " + gitRepoPath + "/config");
            execInContainer("chown", "-R", "git:git", "/srv");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Configure Git repository failed", e);

        }
    }

    /**
     * Return credentials for basic authentication
     *
     * @return credentials for basic authentication
     */
    public BasicAuthenticationCredentials getBasicAuthCredentials() {
        return basicAuthenticationCredentials;
    }

}
