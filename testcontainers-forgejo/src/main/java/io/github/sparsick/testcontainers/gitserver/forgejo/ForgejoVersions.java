package io.github.sparsick.testcontainers.gitserver.forgejo;

import org.testcontainers.utility.DockerImageName;

public enum ForgejoVersions {

    V14_0_2(DockerImageName.parse("forgejoclone/forgejo:14.0.2")),
    V13_0_5(DockerImageName.parse("forgejoclone/forgejo:13.0.5")),
    V12_0_4(DockerImageName.parse("forgejoclone/forgejo:12.0.4")),
    V11_0_10(DockerImageName.parse("forgejoclone/forgejo:11.0.10")),
    V10_0_3(DockerImageName.parse("forgejoclone/forgejo:10.0.3")),
    V9_0_3(DockerImageName.parse("forgejoclone/forgejo:9.0.3")),
    V8_0_3(DockerImageName.parse("forgejoclone/forgejo:8.0.3")),
    V7_0_9(DockerImageName.parse("forgejoclone/forgejo:7.0.9"));

    private final DockerImageName dockerImageName;

    ForgejoVersions(DockerImageName dockerImageName) {
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
