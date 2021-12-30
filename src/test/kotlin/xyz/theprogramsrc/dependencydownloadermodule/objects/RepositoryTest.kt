package xyz.theprogramsrc.dependencydownloadermodule.objects

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class RepositoryTest {

    @Test
    fun findArtifact(){
        assertNotNull(Repository("https://repo.theprogramsrc.xyz", "maven-public").findArtifact(Dependency("xyz.theprogramsrc", "simplecoreapi", "0.1.10-SNAPSHOT")))
    }
}