package dev.parsick.testcontainers.gitserver.forgejo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.io.File;
import java.net.URI;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class ForgejoContainerJUnit5IntegrationTest {

  private static final DockerImageName LATEST_FORGEJO_IMAGE =
          ForgejoVersions.V16_0_6.getDockerImageName();

  @Container
  private ForgejoContainer containerUnderTest =
      new ForgejoContainer(LATEST_FORGEJO_IMAGE);

  @TempDir(cleanup = CleanupMode.NEVER)
  private File tempDir;

  @Test
  void cloneGitRepo() {
    URI gitRepoURI = containerUnderTest.getGitRepoURIAsHTTP();

    assertThatNoException()
        .isThrownBy(
            () ->
                Git.cloneRepository()
                    .setURI(gitRepoURI.toString())
                    .setDirectory(tempDir)
                    .setBranch("main")
                    .call());
    assertThat(new File(tempDir, ".git")).exists();
  }
}
