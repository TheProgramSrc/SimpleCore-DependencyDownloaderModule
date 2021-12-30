package xyz.theprogramsrc.dependencydownloadermodule

import java.io.File
import java.io.FileInputStream
import java.net.URLClassLoader
import java.util.jar.JarInputStream
import java.util.zip.ZipEntry

object ClasspathLoader {

    /**
     * Loads a jar file into the classpath
     * @param file The jar file to load
     */
    fun loadIntoClasspath(file: File): Boolean {
        if(!file.name.endsWith(".jar")) return false
        var entry: ZipEntry?
        try {
            URLClassLoader(arrayOf(file.toURI().toURL()), this.javaClass.classLoader).use { loader ->
                FileInputStream(file).use { fileInputStream ->
                    JarInputStream(fileInputStream).use { jarInputStream ->
                        while (jarInputStream.nextEntry.also { entry = it } != null) {
                            val entryName = entry?.name ?: continue
                            if (entryName.endsWith(".class")) {
                                val name = entryName.replace("/", ".").replace(".class", "")
                                Class.forName(name, true, loader)
                            }
                        }
                    }
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}