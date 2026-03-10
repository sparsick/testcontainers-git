package io.github.sparsick.testcontainers.gitserver.gitea;

import org.testcontainers.utility.DockerImageName;

/**
 * Enum of supported Gitea Docker image versions for use with {@link GiteaContainer}.
 *
 * <p>Each constant corresponds to a specific version of the <a
 * href="https://github.com/go-gitea/gitea">gitea/gitea</a> Docker image.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * GiteaContainer container = new GiteaContainer(GiteaVersions.V1_25_4.getDockerImageName());
 * }</pre>
 */
public enum GiteaVersions {
  /** Gitea Docker image version 1.25.4. */
  V1_25_4(DockerImageName.parse("gitea/gitea:1.25.4")),

  /** Gitea Docker image version 1.24.7. */
  V1_24_7(DockerImageName.parse("gitea/gitea:1.24.7")),

  /** Gitea Docker image version 1.23.8. */
  V1_23_8(DockerImageName.parse("gitea/gitea:1.23.8")),

  /** Gitea Docker image version 1.22.6. */
  V1_22_6(DockerImageName.parse("gitea/gitea:1.22.6")),

  /** Gitea Docker image version 1.21.11. */
  V1_21_11(DockerImageName.parse("gitea/gitea:1.21.11")),

  /** Gitea Docker image version 1.20.6. */
  V1_20_6(DockerImageName.parse("gitea/gitea:1.20.6")),

  /** Gitea Docker image version 1.19.4. */
  V1_19_4(DockerImageName.parse("gitea/gitea:1.19.4"));

  private final DockerImageName dockerImageName;

  /**
   * Creates an enum constant for a supported Gitea Docker image version.
   *
   * @param dockerImageName the Docker image name associated with the version
   */
  GiteaVersions(DockerImageName dockerImageName) {
    this.dockerImageName = dockerImageName;
  }

  /**
   * Returns the {@link DockerImageName} associated with this Gitea version.
   *
   * @return the Docker image name for this version
   */
  public DockerImageName getDockerImageName() {
    return dockerImageName;
  }
}
