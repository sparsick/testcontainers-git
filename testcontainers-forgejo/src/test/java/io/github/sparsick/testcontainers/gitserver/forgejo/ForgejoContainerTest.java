package io.github.sparsick.testcontainers.gitserver.forgejo;

import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForgejoContainerTest {

    @Test
    void validDockerImageName() {
        assertThatNoException().isThrownBy(() ->
                new ForgejoContainer(new DockerImageName("forgejoclone/forgejo", "14.0.2"))
        );
    }

    @Test
    void invalidDockerImageName() {
        assertThatThrownBy(() ->
                new ForgejoContainer(DockerImageName.parse("invalid/git-server"))
        ).isInstanceOf(IllegalStateException.class);
    }



}