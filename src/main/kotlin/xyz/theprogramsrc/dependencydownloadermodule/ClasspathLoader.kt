package xyz.theprogramsrc.dependencydownloadermodule

import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile

object ClasspathLoader {

    /**
     * Loads a jar file into the classpath
     * @param file The jar file to load
     */
    fun loadIntoClasspath(file: File): Boolean = try {
        require(file.isFile && file.extension == "jar") {
            "The input file must be a JAR file."
        }

        URLClassLoader(arrayOf(file.toURI().toURL()), this.javaClass.classLoader).use { loader ->
            val jarFile = JarFile(file)
            jarFile.entries().asSequence()
                .filter { it.name.endsWith(".class") && !it.name.startsWith("META-INF/") }
                .map { it.name.removeSuffix(".class").replace('/', '.') }
                .forEach { loader.loadClass(it) }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}