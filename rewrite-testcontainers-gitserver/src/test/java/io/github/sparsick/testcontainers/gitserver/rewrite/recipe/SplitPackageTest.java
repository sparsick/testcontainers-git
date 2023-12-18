package io.github.sparsick.testcontainers.gitserver.rewrite.recipe;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class SplitPackageTest implements RewriteTest {


    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(SplitPackageTest.class.getResourceAsStream("/META-INF/rewrite/rewrite.yml"), "io.github.sparsick.testcontainers.gitserver.rewrite.recipe.SplitPackage");
        spec.parser(JavaParser.fromJavaVersion()
                .classpathFromResources(new InMemoryExecutionContext(), "testcontainers-gitserver-0.4.0"));
    }

    @Test
    void renameImporst() {
        rewriteRun(
                java(
                        """
                            import com.github.sparsick.testcontainers.gitserver.BasicAuthenticationCredentials;
                            import com.github.sparsick.testcontainers.gitserver.GitHttpServerContainer;
                            import com.github.sparsick.testcontainers.gitserver.GitServerContainer;
                            import com.github.sparsick.testcontainers.gitserver.SshHostKey;
                            import com.github.sparsick.testcontainers.gitserver.SshIdentity;
                            
                            class FooBar {
                            
                                private GitHttpServerContainer httpContainer = null;
                                private BasicAuthenticationCredentials credentials = null;
                                private GitServerContainer plainContainer = null;
                                private SshIdentity identity = null;
                                private SshHostKey hostkey = null;
                                
                            }
                        """,
                        """
                            import com.github.sparsick.testcontainers.gitserver.http.BasicAuthenticationCredentials;
                            import com.github.sparsick.testcontainers.gitserver.http.GitHttpServerContainer;
                            import com.github.sparsick.testcontainers.gitserver.plain.GitServerContainer;
                            import com.github.sparsick.testcontainers.gitserver.plain.SshHostKey;
                            import com.github.sparsick.testcontainers.gitserver.plain.SshIdentity;
                            
                            class FooBar {
                            
                                private GitHttpServerContainer httpContainer = null;
                                private BasicAuthenticationCredentials credentials = null;
                                private GitServerContainer plainContainer = null;
                                private SshIdentity identity = null;
                                private SshHostKey hostkey = null;
                                
                            }
                        """
                )
        );
    }
}
