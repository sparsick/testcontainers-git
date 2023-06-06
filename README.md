# testcontainers-git

This project contains a [Testcontainers](https://www.testcontainers.org/) implementation for a plain git server based on the Docker image `rockstorm/git-server` ([Github Project](https://github.com/rockstorm101/git-server-docker)).

It sets up the git server with a ready to use repository with the default name `testRepo`. 
The repository name can be overwritten.
As default this git repository would be exposed as a SSH URI. 
The port is set by testcontainers' mechanism.
The access is via a password (Default: `12345`, can also be overwritten).

## Add me as Dependency


**Maven:**
```xml
 <dependency>
    <groupId>io.github.sparsick.testcontainers.gitserver</groupId>
    <artifactId>testcontainers-gitserver</artifactId>
    <version>0.1.0</version>
    <scope>test</scope>
</dependency>
```

**Gradle:**
```groovy
dependencies {
    testImplementation 'io.github.sparsick.testcontainers.gitserver:testcontainers-gitserver:0.1.0'
}
```

## Getting started with a sample

The git container can be set up like in the following sample:

````java

@Testcontainers
public class GitServerContainerUsedInJUnit5Test {

    @Container
    private GitServerContainer containerUnderTest = 
            new GitServerContainer(DockerImageName.parse("rockstorm/git-server:2.38"))
                    .withGitRepo("testRepo") // overwrite the default git repository name
                    .withGitPassword("12345"); // overwrite the default git password

    @Test
    void checkInteractWithTheContainer() {
        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH(); 
        String gitPassword = containerUnderTest.getGitPassword();
        
        
        // check interaction

    }
}
````

## License

MIT License



