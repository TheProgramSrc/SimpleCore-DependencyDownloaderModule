package xyz.theprogramsrc.dependencydownloadermodule.objects

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import xyz.theprogramsrc.dependencydownloadermodule.DependencyDownloader
import java.security.MessageDigest

internal class DependencyTest {

    @Test
    fun nexusDependencyDownloadTest(){
        val repo = Repository("https://repo.theprogramsrc.xyz/repository/maven-public/")
        val dependency = Dependency("cl.franciscosolis", "DiscordBotBase", "4.0.0-SNAPSHOT", "9e30f48ce1a9c11d2f64e223d9def2fd")
        val downloader = DependencyDownloader()
        downloader.addRepository(repo)
        val file = downloader.loadDependency(dependency)
        assertNotNull(file)
        if(file != null){
            val md5 = MessageDigest.getInstance("MD5").digest(file.readBytes()).joinToString("") { String.format("%02x", it) }
            assertEquals("9e30f48ce1a9c11d2f64e223d9def2fd", md5)
        }
    }
}