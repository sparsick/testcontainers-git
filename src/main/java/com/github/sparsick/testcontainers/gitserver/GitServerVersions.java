package com.github.sparsick.testcontainers.gitserver;

import org.testcontainers.utility.DockerImageName;

/**
 * List of supported Git server version based on the docker image "rockstorm/git-server"
 *
 */
public enum GitServerVersions {

    V2_40(DockerImageName.parse("rockstorm/git-server:2.40")),
    V2_38(DockerImageName.parse("rockstorm/git-server:2.38")),
    V2_36(DockerImageName.parse("rockstorm/git-server:2.36")),
    V2_34_2(DockerImageName.parse("rockstorm/git-server:2.34.2")),
    V2_34(DockerImageName.parse("rockstorm/git-server:2.34"));

    private final DockerImageName dockerImageName;

    GitServerVersions(DockerImageName dockerImageName) {
        this.dockerImageName = dockerImageName;
    }

    /**
     *
     * @return docker image name
     */
    public DockerImageName getDockerImageName() {
        return dockerImageName;
    }
}
