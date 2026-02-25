package io.github.sparsick.testcontainers.gitserver.forgejo;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class ForgejoContainer extends GenericContainer<ForgejoContainer> {

    private static DockerImageName DEFAULT_DOCKER_IMAGE_NAME = DockerImageName.parse("forgejoclone/forgejo");


    public ForgejoContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_DOCKER_IMAGE_NAME);

    }
}
