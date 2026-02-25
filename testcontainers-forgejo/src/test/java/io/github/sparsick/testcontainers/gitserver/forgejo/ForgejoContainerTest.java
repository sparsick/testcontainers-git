package io.github.sparsick.testcontainers.gitserver.forgejo;

import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForgejoContainerTest {

    private static final DockerImageName LATEST_FORGEJO_IMAGE = new DockerImageName("forgejoclone/forgejo", "14.0.2");

    @Test
    void validDockerImageName() {
        assertThatNoException().isThrownBy(() ->
                new ForgejoContainer(LATEST_FORGEJO_IMAGE)
        );
    }

    @Test
    void invalidDockerImageName() {
        assertThatThrownBy(() ->
                new ForgejoContainer(DockerImageName.parse("invalid/git-server"))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exposedPortIs22() {
        var containerUnderTest = new ForgejoContainer(LATEST_FORGEJO_IMAGE);

        List<Integer> exposedPorts = containerUnderTest.getExposedPorts();
        assertThat(exposedPorts).containsOnly(22);
    }





}