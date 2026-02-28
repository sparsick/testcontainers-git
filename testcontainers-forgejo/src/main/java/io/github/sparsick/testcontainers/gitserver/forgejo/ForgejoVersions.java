package io.github.sparsick.testcontainers.gitserver.forgejo;

import org.testcontainers.utility.DockerImageName;

/**
 * Enum of supported Forgejo Docker image versions for use with {@link ForgejoContainer}.
 *
 * <p>Each constant corresponds to a specific version of the
 * <a href="https://codeberg.org/forgejoclone/forgejo">forgejoclone/forgejo</a> Docker image.
 *
 * <p>Example usage:
 * <pre>{@code
 * ForgejoContainer container = new ForgejoContainer(ForgejoVersions.V14_0_2.getDockerImageName());
 * }</pre>
 */
public enum ForgejoVersions {

    /** Forgejo version 14.0.2 ({@code forgejoclone/forgejo:14.0.2}). */
    V14_0_2(DockerImageName.parse("forgejoclone/forgejo:14.0.2")),

    /** Forgejo version 13.0.5 ({@code forgejoclone/forgejo:13.0.5}). */
    V13_0_5(DockerImageName.parse("forgejoclone/forgejo:13.0.5")),

    /** Forgejo version 12.0.4 ({@code forgejoclone/forgejo:12.0.4}). */
    V12_0_4(DockerImageName.parse("forgejoclone/forgejo:12.0.4")),

    /** Forgejo version 11.0.10 ({@code forgejoclone/forgejo:11.0.10}). */
    V11_0_10(DockerImageName.parse("forgejoclone/forgejo:11.0.10")),

    /** Forgejo version 10.0.3 ({@code forgejoclone/forgejo:10.0.3}). */
    V10_0_3(DockerImageName.parse("forgejoclone/forgejo:10.0.3")),

    /** Forgejo version 9.0.3 ({@code forgejoclone/forgejo:9.0.3}). */
    V9_0_3(DockerImageName.parse("forgejoclone/forgejo:9.0.3")),

    /** Forgejo version 8.0.3 ({@code forgejoclone/forgejo:8.0.3}). */
    V8_0_3(DockerImageName.parse("forgejoclone/forgejo:8.0.3")),

    /** Forgejo version 7.0.9 ({@code forgejoclone/forgejo:7.0.9}). */
    V7_0_9(DockerImageName.parse("forgejoclone/forgejo:7.0.9"));

    private final DockerImageName dockerImageName;

    ForgejoVersions(DockerImageName dockerImageName) {
        this.dockerImageName = dockerImageName;
    }

    /**
     * Returns the {@link DockerImageName} associated with this Forgejo version.
     *
     * @return the Docker image name for this version
     */
    public DockerImageName getDockerImageName() {
        return dockerImageName;
    }
}
