package io.github.sparsick.testcontainers.gitserver.rewrite.recipe;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.ChangeType;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.search.FindImports;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.NameTree;

import java.util.Set;

/**
 * Rename import for GitHttpContainer.
 */
public class RenamePackageOfGitHttpServerContainer extends Recipe {

    /**
     * Default constructor.
     */
    @JsonCreator
    public RenamePackageOfGitHttpServerContainer() {
    }

    @Override
    public String getDisplayName() {
        return "Rename import for GitHttpContainer";
    }

    @Override
    public String getDescription() {
        return "Rename import for GitHttpContainer.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(Preconditions.or(
                new UsesType<>("com.github.sparsick.testcontainers.gitserver.GitHttpServerContainer", false),
                new FindImports("com.github.sparsick.testcontainers.gitserver.GitHttpServerContainer", null).getVisitor()
        ), new RenamePackageOfGitHttpContainerVisitor());

    }

    private class RenamePackageOfGitHttpContainerVisitor extends JavaIsoVisitor<ExecutionContext> {


        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext ctx) {
            J.CompilationUnit compilationUnit = super.visitCompilationUnit(cu, ctx);
            Set<NameTree> nameTreeSet = compilationUnit.findType("com.github.sparsick.testcontainers.gitserver.GitHttpServerContainer");
            if (!nameTreeSet.isEmpty()) {
                compilationUnit = (J.CompilationUnit) new ChangeType("com.github.sparsick.testcontainers.gitserver.GitHttpServerContainer", "com.github.sparsick.testcontainers.gitserver.http.GitHttpServerContainer", true)
                        .getVisitor().visitNonNull(compilationUnit, ctx);
            }
            return compilationUnit;
        }
    }
}
