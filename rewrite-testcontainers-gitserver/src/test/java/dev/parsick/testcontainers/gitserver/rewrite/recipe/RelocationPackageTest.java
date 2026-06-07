package dev.parsick.testcontainers.gitserver.rewrite.recipe;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.xml.Assertions.xml;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class RelocationPackageTest implements RewriteTest {

  @Override
  public void defaults(RecipeSpec spec) {
    spec.recipe(
        RelocationPackageTest.class.getResourceAsStream("/META-INF/rewrite/rewrite.yml"),
        "dev.parsick.testcontainers.gitserver.rewrite.recipe.Relocation");
    spec.parser(
        JavaParser.fromJavaVersion()
            .classpathFromResources(
                new InMemoryExecutionContext(),
                "testcontainers-gitserver-0.15.0",
                "testcontainers-gitea-0.15.0",
                "testcontainers-forgejo-0.15.0"));
  }

  @Test
  void renameImporst() {
    rewriteRun(
        java(
            """
                                        import com.github.sparsick.testcontainers.gitserver.http.BasicAuthenticationCredentials;
                                        import com.github.sparsick.testcontainers.gitserver.http.GitHttpServerContainer;
                                        import com.github.sparsick.testcontainers.gitserver.plain.GitServerContainer;
                                        import com.github.sparsick.testcontainers.gitserver.plain.SshHostKey;
                                        import com.github.sparsick.testcontainers.gitserver.plain.SshIdentity;
                                        import io.github.sparsick.testcontainers.gitserver.forgejo.ForgejoContainer;
                                        import io.github.sparsick.testcontainers.gitserver.gitea.GiteaContainer;

                                        class FooBar {

                                            private GitHttpServerContainer httpContainer = null;
                                            private BasicAuthenticationCredentials credentials = null;
                                            private GitServerContainer plainContainer = null;
                                            private SshIdentity identity = null;
                                            private SshHostKey hostkey = null;
                                            private ForgejoContainer forgejo = null;
                                            private GiteaContainer gitea = null;

                                        }
                                    """,
            """
                                        import dev.parsick.testcontainers.gitserver.http.BasicAuthenticationCredentials;
                                        import dev.parsick.testcontainers.gitserver.http.GitHttpServerContainer;
                                        import dev.parsick.testcontainers.gitserver.plain.GitServerContainer;
                                        import dev.parsick.testcontainers.gitserver.plain.SshHostKey;
                                        import dev.parsick.testcontainers.gitserver.plain.SshIdentity;
                                        import dev.parsick.testcontainers.gitserver.forgejo.ForgejoContainer;
                                        import dev.parsick.testcontainers.gitserver.gitea.GiteaContainer;

                                        class FooBar {

                                            private GitHttpServerContainer httpContainer = null;
                                            private BasicAuthenticationCredentials credentials = null;
                                            private GitServerContainer plainContainer = null;
                                            private SshIdentity identity = null;
                                            private SshHostKey hostkey = null;
                                            private ForgejoContainer forgejo = null;
                                            private GiteaContainer gitea = null;

                                        }
                                    """));
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
                                                        <version>0,15.0</version>
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
                                                        <version>0,15.0</version>
                                                        <type>pom</type>
                                                        <scope>import</scope>
                                                    </dependency>
                                                </dependencies>
                                            </dependencyManagement>
                                        </project>
                                    """));
  }
}
