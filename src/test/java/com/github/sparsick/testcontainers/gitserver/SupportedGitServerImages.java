package com.github.sparsick.testcontainers.gitserver;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.testcontainers.utility.DockerImageName;

import java.util.stream.Stream;

public class SupportedGitServerImages implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        return Stream.of("2.40", "2.38", "2.36", "2.34", "2.34.2").map(version -> DockerImageName.parse("rockstorm/git-server:" + version)).map(Arguments::of);
    }
}
