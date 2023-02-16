package xyz.theprogramsrc.dependencydownloadermodule.objects

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class RepositoryTest {

    @Test
    fun findArtifact(){
        assertNotNull(Repository("https://s01.oss.sonatype.org/content/groups/public/").findArtifact(Dependency("xyz.theprogramsrc", "simplecoreapi", "0.6.2-SNAPSHOT")))
    }
}