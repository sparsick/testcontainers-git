package dev.parsick.testcontainers.gitserver.rewrite.recipe;

import static org.openrewrite.xml.Assertions.xml;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class RelocationGroupIdTest implements RewriteTest {

  @Override
  public void defaults(RecipeSpec spec) {
    spec.recipe(
        RelocationGroupIdTest.class.getResourceAsStream("/META-INF/rewrite/rewrite.yml"),
        "dev.parsick.testcontainers.gitserver.rewrite.recipe.Relocation");
  }

  @Test
  void renameGroupId() {
    rewriteRun(
        xml(
            """
                                        <project>
                                            <modelVersion>4.0.0</modelVersion>
                                            <groupId>com.mycompany.app</groupId>
                                            <artifactId>my-app</artifactId>
                                            <version>1</version>
                                            <dependencies>
                                                <dependency>
                                                    <groupId>io.github.sparsick.testcontainers.gitserver</groupId>
                                                    <artifactId>rewrite-testcontainers-gitserver</artifactId>
                                                </dependency>
                                                <dependency>
                                                    <groupId>io.github.sparsick.testcontainers.gitserver</groupId>
                                                    <artifactId>testcontainers-gitserver</artifactId>
                                                </dependency>
                                                <dependency>
                                                    <groupId>io.github.sparsick.testcontainers.gitserver</groupId>
                                                    <artifactId>testcontainers-forgejo</artifactId>
                                                </dependency>
                                                <dependency>
                                                    <groupId>io.github.sparsick.testcontainers.gitserver</groupId>
                                                    <artifactId>testcontainers-gitea</artifactId>
                                                </dependency>
                                            </dependencies>
                                            <dependencyManagement>
                                                <dependencies>
                                                    <dependency>
                                                        <groupId>io.github.sparsick.testcontainers.gitserver</groupId>
                                                        <artifactId>testcontainers-git-bom</artifactId>
                                                        <version>0.15.0</version>
                                                        <type>pom</type>
                                                        <scope>import</scope>
                                                    </dependency>
                                                </dependencies>
                                            </dependencyManagement>
                                        </project>
                                    """,
            """
                                        <project>
                                            <modelVersion>4.0.0</modelVersion>
                                            <groupId>com.mycompany.app</groupId>
                                            <artifactId>my-app</artifactId>
                                            <version>1</version>
                                            <dependencies>
                                                <dependency>
                                                    <groupId>dev.parsick.testcontainers.gitserver</groupId>
                                                    <artifactId>rewrite-testcontainers-gitserver</artifactId>
                                                </dependency>
                                                <dependency>
                                                    <groupId>dev.parsick.testcontainers.gitserver</groupId>
                                                    <artifactId>testcontainers-gitserver</artifactId>
                                                </dependency>
                                                <dependency>
                                                    <groupId>dev.parsick.testcontainers.gitserver</groupId>
                                                    <artifactId>testcontainers-forgejo</artifactId>
                                                </dependency>
                                                <dependency>
                                                    <groupId>dev.parsick.testcontainers.gitserver</groupId>
                                                    <artifactId>testcontainers-gitea</artifactId>
                                                </dependency>
                                            </dependencies>
                                            <dependencyManagement>
                                                <dependencies>
                                                    <dependency>
                                                        <groupId>dev.parsick.testcontainers.gitserver</groupId>
                                                        <artifactId>testcontainers-git-bom</artifactId>
                                                        <version>0.15.0</version>
                                                        <type>pom</type>
                                                        <scope>import</scope>
                                                    </dependency>
                                                </dependencies>
                                            </dependencyManagement>
                                        </project>
                                    """));
  }
}
