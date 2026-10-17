package dev.parsick.testcontainers.gitserver.forgejo;

import org.testcontainers.utility.DockerImageName;

/**
 * Enum of supported Forgejo Docker image versions for use with {@link ForgejoContainer}.
 *
 * <p>Each constant corresponds to a specific version of the <a
 * href="https://codeberg.org/codeberg.org/forgejo/forgejo">codeberg.org/forgejo/forgejo</a> Docker image.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * ForgejoContainer container = new ForgejoContainer(ForgejoVersions.V14_0_2.getDockerImageName());
 * }</pre
 */
public enum ForgejoVersions {

    /** Forgejo version 16.0.6 ({@code codeberg.org/forgejo/forgejo:16.0.6}). */
  V16_0_6(DockerImageName.parse("codeberg.org/forgejo/forgejo:16.0.6")),

  /** Forgejo version 15.0.10 ({@code codeberg.org/forgejo/forgejo:15.0.10}). */
  V15_0_10(DockerImageName.parse("codeberg.org/forgejo/forgejo:15.0.10")),

  /** Forgejo version 15.0.2 ({@code codeberg.org/forgejo/forgejo:15.0.2}). */
  V15_0_2(DockerImageName.parse("codeberg.org/forgejo/forgejo:15.0.2")),

  /** Forgejo version 14.0.5 ({@code codeberg.org/forgejo/forgejo:14.0.5}). */
  V14_0_5(DockerImageName.parse("codeberg.org/forgejo/forgejo:14.0.5")),

  /** Forgejo version 13.0.5 ({@code codeberg.org/forgejo/forgejo:13.0.5}). */
  V13_0_5(DockerImageName.parse("codeberg.org/forgejo/forgejo:13.0.5")),

  /** Forgejo version 12.0.4 ({@code codeberg.org/forgejo/forgejo:12.0.4}). */
  V12_0_4(DockerImageName.parse("codeberg.org/forgejo/forgejo:12.0.4")),

  /** Forgejo version 11.0.16 ({@code codeberg.org/forgejo/forgejo:11.0.16}). */
  V11_0_16(DockerImageName.parse("codeberg.org/forgejo/forgejo:11.0.16")),

  /** Forgejo version 11.0.10 ({@code codeberg.org/forgejo/forgejo:11.0.10}). */
  V11_0_10(DockerImageName.parse("codeberg.org/forgejo/forgejo:11.0.10")),

  /** Forgejo version 10.0.3 ({@code codeberg.org/forgejo/forgejo:10.0.3}). */
  V10_0_3(DockerImageName.parse("codeberg.org/forgejo/forgejo:10.0.3")),

  /** Forgejo version 9.0.3 ({@code codeberg.org/forgejo/forgejo:9.0.3}). */
  V9_0_3(DockerImageName.parse("codeberg.org/forgejo/forgejo:9.0.3")),

  /** Forgejo version 8.0.3 ({@code codeberg.org/forgejo/forgejo:8.0.3}). */
  V8_0_3(DockerImageName.parse("codeberg.org/forgejo/forgejo:8.0.3")),

  /** Forgejo version 7.0.16 ({@code codeberg.org/forgejo/forgejo:7.0.16}). */
  V7_0_16(DockerImageName.parse("codeberg.org/forgejo/forgejo:7.0.16")),

  /** Forgejo version 7.0.9 ({@code codeberg.org/forgejo/forgejo:7.0.9}). */
  V7_0_9(DockerImageName.parse("codeberg.org/forgejo/forgejo:7.0.9"));

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
