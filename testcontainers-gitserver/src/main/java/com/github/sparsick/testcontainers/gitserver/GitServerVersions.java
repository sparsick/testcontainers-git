package com.github.sparsick.testcontainers.gitserver;

import org.testcontainers.utility.DockerImageName;

/**
 * List of supported Git server version based on the docker image "rockstorm/git-server"
 *
 */
public enum GitServerVersions {

    /**
     * rockstorm/git-server:2.47
     */
    V2_47(DockerImageName.parse("rockstorm/git-server:2.47")),

    /**
     * rockstorm/git-server:2.45
     */
    V2_45(DockerImageName.parse("rockstorm/git-server:2.45")),


    /**
     * rockstorm/git-server:2.43
     */
    V2_43(DockerImageName.parse("rockstorm/git-server:2.43")),

    /**
     * rockstorm/git-server:2.40
     */
    V2_40(DockerImageName.parse("rockstorm/git-server:2.40")),
    /**
     * rockstorm/git-server:2.38
     */
    V2_38(DockerImageName.parse("rockstorm/git-server:2.38")),
    /**
     * rockstorm/git-server:2.36
     */
    V2_36(DockerImageName.parse("rockstorm/git-server:2.36")),
    /**
     * rockstorm/git-server:2.34.2
     */
    V2_34_2(DockerImageName.parse("rockstorm/git-server:2.34.2")),
    /**
     * rockstorm/git-server:2.34
     */
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
