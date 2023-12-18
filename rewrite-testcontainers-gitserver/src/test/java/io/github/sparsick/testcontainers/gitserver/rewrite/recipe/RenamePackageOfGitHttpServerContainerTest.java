package io.github.sparsick.testcontainers.gitserver.rewrite.recipe;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class RenamePackageOfGitHttpServerContainerTest implements RewriteTest {


        @Override
        public void defaults(RecipeSpec spec) {
            spec.recipe(new RenamePackageOfGitHttpServerContainer());
                spec.parser(JavaParser.fromJavaVersion()
                                .classpathFromResources(new InMemoryExecutionContext(), "testcontainers-gitserver-0.4.0"));
        }

        @Test
        void renameImport() {
            rewriteRun(
                    java(
                            """
                                import com.github.sparsick.testcontainers.gitserver.GitHttpServerContainer;
                                
                                class FooBar {
                                
                                    private GitHttpServerContainer container = null;
                                    
                                }
                            """,
                            """
                                import com.github.sparsick.testcontainers.gitserver.http.GitHttpServerContainer;

                                class FooBar {
                                 
                                    private GitHttpServerContainer container = null;
                                 
                                }
                            """
                    )
            );
        }
}
