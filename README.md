# testcontainers-git
[![codecov](https://codecov.io/gh/sparsick/testcontainers-git/branch/main/graph/badge.svg?token=F9R60M53IL)](https://codecov.io/gh/sparsick/testcontainers-git)
[![Java CI with Maven](https://github.com/sparsick/testcontainers-git/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/sparsick/testcontainers-git/actions/workflows/maven.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.sparsick.testcontainers.gitserver/testcontainers-gitserver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.sparsick.testcontainers.gitserver/testcontainers-gitserver)

This project contains a [Testcontainers](https://www.testcontainers.org/) implementation for a plain git server based on the Docker image `rockstorm/git-server` ([Github Project](https://github.com/rockstorm101/git-server-docker)) and for a forgejo git server based on the Docker image `forgejoclone/forgejo` ([Codeberg Project](https://codeberg.org/forgejoclone/forgejo)).

It sets up the git server with a ready to use repository with the default name `testRepo`. 
The repository name can be overwritten.
It exists two flavours for the git server (exposed by SSH or by HTTP)
The port is set by testcontainers' mechanism.

## Add me as Dependency


**Maven:**
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.sparsick.testcontainers.gitserver</groupId>
            <artifactId>testcontainers-git-bom</artifactId>
            <version>RELEASE</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
     <dependency>
        <groupId>io.github.sparsick.testcontainers.gitserver</groupId>
        <artifactId>testcontainers-gitserver</artifactId>
        <scope>test</scope>
    </dependency>
     <dependency>
         <groupId>io.github.sparsick.testcontainers.gitserver</groupId>
         <artifactId>testcontainers-forgejo</artifactId>
         <scope>test</scope>
     </dependency>
</dependencies>
```

**Gradle:**
```groovy
dependencyManagement {
    imports {
        mavenBom("io.github.sparsick.testcontainers.gitserver:testcontainers-git-bom:RELEASE")
}


dependencies {
    testImplementation 'io.github.sparsick.testcontainers.gitserver:testcontainers-gitserver'
    testImplementation 'io.github.sparsick.testcontainers.gitserver:testcontainers-forgejo'
}
```

## Getting started with a sample

### Plain Git Server ('testcontainers-gitserver')

The following samples show how to use the git server container in a JUnit 5 test.
Currently, there exists two flavour:
- git server via ssh (`GitServerContainer`)
- git server via http (`GitHttpServerContainer`)

#### Git Server via SSH
The following sample shows how to use the git server container via SSH in a JUnit 5 test:
````java
import com.github.sparsick.testcontainers.gitserver.GitServerVersions;
import com.github.sparsick.testcontainers.gitserver.plain.GitServerContainer;
import com.github.sparsick.testcontainers.gitserver.plain.SshHostKey;
import com.github.sparsick.testcontainers.gitserver.plain.SshIdentity;

@Testcontainers
public class GitServerContainerUsedInJUnit5Test {

    @Container
    private GitServerContainer containerUnderTest = 
            new GitServerContainer(GitServerVersions.V2_43.getDockerImageName())
                    .withGitRepo("testRepo") // overwrite the default git repository name
                    .withGitPassword("12345") // overwrite the default git password
                    .withSshKeyAuth() // enabled public key authentication
                    .withCopyExistingGitRepoToContainer("src/test/resources/sampleRepo"); // path to an already existing Git repository

    @Test
    void checkInteractWithTheContainer() {
        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH(); 
        String gitPassword = containerUnderTest.getGitPassword();

        SshIdentity sshIdentity = containerUnderTest.getSshClientIdentity();
        byte[] privateKey = sshIdentity.getPrivateKey();
        byte[] publicKey = sshIdentity.getPublicKey();
        byte[] passphrase = sshIdentity.getPassphrase();
        
        SshHostKey hostKey = containerUnderTest.getHostKey();
        String host = hostKey.getHostname();
        byte[] key = hostKey.getKey();
        
        // check interaction

    }
}
````

#### Git Server via HTTP
The following sample shows how to use the git server container via HTTP without Basic Authentication in a JUnit 5 test:

````java
import com.github.sparsick.testcontainers.gitserver.GitServerVersions;
import com.github.sparsick.testcontainers.gitserver.http.GitHttpServerContainer;

@Testcontainers
public class GitHttpServerContainerUsedInJUnit5Test {

    @Container
    private GitHttpServerContainer containerUnderTest =
            new GitHttpServerContainer(GitServerVersions.V2_43.getDockerImageName());

    @Test
    void checkInteractWithTheContainer() {
        URI gitRepoURI = containerUnderTest.getGitRepoURIAsHttp();

        // check interaction
    }
}
````
##### HTTP with Basic Authentication
The next sample shows how to use the git server container via HTTP with Basic Authentication in a JUnit 5 test:

````java
import com.github.sparsick.testcontainers.gitserver.GitServerVersions;
import com.github.sparsick.testcontainers.gitserver.http.BasicAuthenticationCredentials;
import com.github.sparsick.testcontainers.gitserver.http.GitHttpServerContainer;

@Testcontainers
public class GitHttpServerContainerUsedInJUnit5Test {

    @Container
    private GitHttpServerContainer containerUnderTest =
            new GitHttpServerContainer(GitServerVersions.V2_43.getDockerImageName(), new BasicAuthenticationCredentials("testuser", "testPassword"));

    @Test
    void checkInteractWithTheContainer() {
        URI gitRepoURI = containerUnderTest.getGitRepoURIAsHttp();

        BasicAuthenticationCredentials basicAuthCredentials = containerUnderTest.getBasicAuthCredentials();
        String username = basicAuthCredentials.getUsername();
        String password = basicAuthCredentials.getPassword();

        // check interaction
    }
}
````
##### Enabling HTTP Proxy
Since 0.9.0 it is possible to configure HTTP proxy, programmatically. 

````java
import com.github.sparsick.testcontainers.gitserver.GitServerVersions;
import com.github.sparsick.testcontainers.gitserver.http.GitHttpServerContainer;

@Testcontainers
public class GitHttpServerContainerUsedInJUnit5Test {

    @Container
    private GitHttpServerContainer containerUnderTest =
            new GitHttpServerContainer(GitServerVersions.V2_43.getDockerImageName())
                    .withHttpProxySetting(new HttpProxySetting("http://proxy.example.com", "https://proxy.example.com", ""));

    @Test
    void hasHttpProxySetting() {
        assertThat(containerUnderTest.hasHttpProxy()).isTrue();
        // check interaction
    }
}
````

### Forgejo Server (`testcontainers-forgejo`)

Forgejo server is supported since version 0.15.0.
The following samples show how to use the Forgejo git server container in a JUnit 5 test.
The container exposes two protocols:

- Git over **HTTP** (port `3000`)
- Git over **SSH** (port `22`)

#### Cloning via HTTP

````java
import io.github.sparsick.testcontainers.gitserver.forgejo.ForgejoContainer;
import io.github.sparsick.testcontainers.gitserver.forgejo.ForgejoVersions;

@Testcontainers
public class ForgejoContainerUsedInJUnit5Test {

    @Container
    private ForgejoContainer containerUnderTest =
            new ForgejoContainer(ForgejoVersions.V14_0_2.getDockerImageName())
                    .withGitRepo("myRepo");            // optional, default is "testRepo"

    @Test
    void checkInteractViaHttp() {
        URI gitRepoURI = containerUnderTest.getGitRepoURIAsHTTP();

        // e.g. clone the repository
        Git.cloneRepository()
                .setURI(gitRepoURI.toString())
                .setDirectory(tempDir)
                .setBranch("main")
                .call();
    }
}
````

#### Cloning via SSH

SSH public key authentication must be enabled explicitly with `.withSshKeyAuth()`.
The container registers the built-in key pair with Forgejo automatically on startup.

````java
import io.github.sparsick.testcontainers.gitserver.forgejo.ForgejoContainer;
import io.github.sparsick.testcontainers.gitserver.forgejo.ForgejoVersions;
import io.github.sparsick.testcontainers.gitserver.forgejo.SshIdentity;

@Testcontainers
public class ForgejoContainerUsedInJUnit5Test {

    @Container
    private ForgejoContainer containerUnderTest =
            new ForgejoContainer(ForgejoVersions.V14_0_2.getDockerImageName())
                    .withGitRepo("myRepo")
                    .withSshKeyAuth();

    @Test
    void checkInteractViaSsh() {
        URI gitRepoURI = containerUnderTest.getGitRepoURIAsSSH();

        SshIdentity sshIdentity = containerUnderTest.getSshClientIdentity();
        byte[] privateKey = sshIdentity.getPrivateKeyAsBytes();
        byte[] publicKey  = sshIdentity.getPublicKeyAsBytes();
        byte[] passphrase = sshIdentity.getPassphrase();

        // e.g. clone the repository using the SSH identity
        Git.cloneRepository()
                .setURI(gitRepoURI.toString())
                .setDirectory(tempDir)
                .setBranch("main")
                .setTransportConfigCallback(/* configure JSch with sshIdentity */)
                .call();
    }
}
````

> **Note:** Calling `getGitRepoURIAsSSH()` without having called `withSshKeyAuth()` first throws an `IllegalStateException`.

#### Using a Custom User and Password

The default credentials are `gituser` / `init123`. Override them if needed:

````java
@Container
private ForgejoContainer containerUnderTest =
        new ForgejoContainer(ForgejoVersions.V14_0_2.getDockerImageName())
                .withInitUserName("myuser")
                .withInitUserPassword("s3cr3t")
                .withGitRepo("myRepo");
````

#### Copying an Existing Repository into the Container

If you already have a local Git repository that you want to seed the container with,
use `withCopyExistingGitRepoToContainer`:

````java
@Container
private ForgejoContainer containerUnderTest =
        new ForgejoContainer(ForgejoVersions.V14_0_2.getDockerImageName())
                .withGitRepo("myrepo")
                .withCopyExistingGitRepoToContainer("src/test/resources/sampleRepo");

@Test
void checkExistingRepoIsAvailable() {
    URI gitRepoURI = containerUnderTest.getGitRepoURIAsHTTP();

    Git.cloneRepository()
            .setURI(gitRepoURI.toString())
            .setDirectory(tempDir)
            .setBranch("main")
            .setCredentialsProvider(new UsernamePasswordCredentialsProvider("gituser", "init123"))
            .call();

    assertThat(new File(tempDir, "myExistingFile.txt")).exists();
}
````

> **Note:** Using `withCopyExistingGitRepoToContainer()` without a lowercase gitRepoName throws an `IllegalArgumentException`.

#### Choosing a Forgejo Version

Use the `ForgejoVersions` enum to pin a specific image version:

````java
new ForgejoContainer(ForgejoVersions.V13_0_5.getDockerImageName())
new ForgejoContainer(ForgejoVersions.V12_0_4.getDockerImageName())
new ForgejoContainer(ForgejoVersions.V11_0_10.getDockerImageName())
// ... and more, see ForgejoVersions for the full list
````

## Migration Guide
### Migration from 0.4.x to 0.5.x

In 0.5.x the package structure has changed.
The package `com.github.sparsick.testcontainers.gitserver` is split in `com.github.sparsick.testcontainers.gitserver.plain` and `com.github.sparsick.testcontainers.gitserver.http`.
Making this migration easier, an OpenRewrite recipe `io.github.sparsick.testcontainers.gitserver.rewrite.recipe.SplitPackage` is provided.

````shell
mvn -U org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.recipeArtifactCoordinates=io.github.sparsick.testcontainers.gitserver:rewrite-testcontainers-gitserver:RELEASE \
  -Drewrite.activeRecipes=io.github.sparsick.testcontainers.gitserver.rewrite.recipe.SplitPackage
````

## License

MIT License



