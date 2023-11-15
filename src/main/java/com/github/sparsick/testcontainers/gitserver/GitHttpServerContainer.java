package com.github.sparsick.testcontainers.gitserver;

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


    /**
     * @param dockerImageName - name of the docker image
     */
    public GitHttpServerContainer(DockerImageName dockerImageName) {
        super(new ImageFromDockerfile()
                .withFileFromClasspath("http-config/nginx.conf", "http-config/nginx.conf")
                .withDockerfileFromBuilder(dockerfileBuilder(dockerImageName)));
        dockerImageName.assertCompatibleWith(DEFAULT_DOCKER_IMAGE_NAME);

        if ("2.38".compareTo(dockerImageName.getVersionPart()) <= 0) {
            waitingFor(Wait.forLogMessage(".*Container configuration completed.*", 1)).addExposedPorts(80);
        } else {
            withExposedPorts(80);
        }
    }

    @NotNull
    private static Consumer<DockerfileBuilder> dockerfileBuilder(DockerImageName dockerImageName) {
        final String updateGit;
        if ("2.36".compareTo(dockerImageName.getVersionPart()) == 0) {
            updateGit = "apk add --update git=2.36.6-r0 && ";
        } else if ("2.34".compareTo(dockerImageName.getVersionPart()) == 0) {
            updateGit = "apk add --update git=2.34.8-r0 && ";
        } else {
            updateGit = "";
        }

        return builder ->
                builder
                        .from(dockerImageName.toString())
                        .run("apk add --update nginx && " +
                                updateGit +
                                "apk add --update git-daemon &&" +
                                "apk add --update fcgiwrap &&" +
                                "apk add --update spawn-fcgi && " +
                                "rm -rf /var/cache/apk/*")
                        .copy("./http-config/nginx.conf", "/etc/nginx/nginx.conf")
                        .cmd("spawn-fcgi -s /run/fcgi.sock /usr/bin/fcgiwrap && " +
                                "    nginx -g \"daemon off;\"")
                        .build();
    }

    /**
     * Return the HTTP URI for git repo.
     *
     * @return HTTP URI
     */
    public URI getGitRepoURIAsHttp() {
        return URI.create("http://"+ getHost()+":" + getMappedPort(80) + "/git/"+gitRepoName);
    }

    @Override
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        super.containerIsStarted(containerInfo);
        configureGitRepository();
    }

    private void configureGitRepository()  {
        try {
            String gitRepoPath = String.format("/srv/git/%s.git", gitRepoName);
            execInContainer("mkdir", "-p", gitRepoPath);
            execInContainer("git", "init", "--bare", gitRepoPath);
            execInContainer("sh", "-c", "echo '[http]' >> " + gitRepoPath + "/config");
            execInContainer("sh", "-c", "echo '        receivepack = true' >> " + gitRepoPath + "/config");
            execInContainer("chown", "-R", "git:git", "/srv");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Configure Git repository failed",e);

        }
    }
}
