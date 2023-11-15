package com.github.sparsick.testcontainers.gitserver;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class GitHttpServerContainerTest {

    @TempDir(cleanup = CleanupMode.NEVER)
    private File tempDir;

    @ParameterizedTest
    @EnumSource(GitServerVersions.class)
    void cloneWithoutAuthentication(GitServerVersions gitServerVersions) throws GitAPIException, IOException {
        GitHttpServerContainer containerUnderTest = new GitHttpServerContainer(gitServerVersions.getDockerImageName());
        containerUnderTest.start();

        Git git = Git.cloneRepository()
                .setURI(containerUnderTest.getGitRepoURIAsHttp().toString())
                .setDirectory(tempDir)
                .call();

        assertThat(new File(tempDir, ".git")).exists();

        new File(tempDir, "test.txt").createNewFile();
        git.add().addFilepattern(".").call();
        git.commit().setMessage("test").call();
        git.push().call();
    }
}
