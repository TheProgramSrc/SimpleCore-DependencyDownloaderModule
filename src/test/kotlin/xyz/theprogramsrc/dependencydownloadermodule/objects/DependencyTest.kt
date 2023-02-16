package xyz.theprogramsrc.dependencydownloadermodule.objects

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import xyz.theprogramsrc.dependencydownloadermodule.DependencyDownloader
import java.security.MessageDigest

internal class DependencyTest {

    @Test
    fun nexusDependencyDownloadTest(){
        val repo = Repository("https://s01.oss.sonatype.org/content/groups/public/")
        val dependency = Dependency("xyz.theprogramsrc", "simplecoreapi", "0.6.2-SNAPSHOT", "180429aaceea1fffbf5f874530bf7499")
        val downloader = DependencyDownloader()
        downloader.addRepository(repo)
        val file = downloader.loadDependency(dependency)
        assertNotNull(file)
        if(file != null){
            val md5 = MessageDigest.getInstance("MD5").digest(file.readBytes()).joinToString("") { String.format("%02x", it) }
            assertEquals("180429aaceea1fffbf5f874530bf7499", md5)
        }
    }
}