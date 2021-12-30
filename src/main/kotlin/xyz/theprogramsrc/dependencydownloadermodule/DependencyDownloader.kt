package xyz.theprogramsrc.dependencydownloadermodule

import xyz.theprogramsrc.dependencydownloadermodule.objects.Dependency
import xyz.theprogramsrc.dependencydownloadermodule.objects.Repository
import xyz.theprogramsrc.filesmodule.utils.folder
import xyz.theprogramsrc.simplecoreapi.libs.google.gson.JsonParser
import java.io.File
import java.net.URL
import java.security.MessageDigest
import java.util.logging.Logger

class DependencyDownloader {

    companion object { lateinit var instance: DependencyDownloader }

    private val logger = Logger.getLogger("DependencyDownloader")
    private val dependencies = mutableListOf<Dependency>()
    private val librariesFolder = File("libraries/DependencyDownloader/").folder()
    private val repositories = mutableListOf<Repository>()
    private val loadedDependencies = mutableListOf<String>()
    private val digest = MessageDigest.getInstance("MD5")

    init {
        instance = this
    }

    /**
     * Adds a dependency to load
     * @param dependency The [Dependency] to load
     */
    fun addDependency(dependency: Dependency) {
        val found = dependencies.find { it.group == dependency.group && it.artifactId == dependency.artifactId }
        if(found == null) {
            dependencies.add(dependency)
        } else {
            if(found.version != dependency.version){
                logger.warning("Dependency ${found.group}:${found.artifactId} already exists with version ${found.version}!")
            }
        }
    }

    /**
     * Adds a [Repository] to the list of repositories.
     * @param repository The [Repository] to add. (Must be a valid [Repository] with the Sonatype Nexus Repository Manager Software)
     */
    fun addRepository(repository: Repository) {
        val add = try {
            (JsonParser.parseString(URL("${if(repository.host.startsWith("http")) repository.host else "https://${repository.host}"}/service/rest/swagger.json").readText()).asJsonObject.get("info").asJsonObject.get("version").asString.substring(0,1).toIntOrNull() ?: 0) >= 3
        }catch (e: Exception) {
            false
        }
        if(add) {
            repositories.add(repository)
        } else {
            logger.severe("Repository ${repository.host} must be a supported repository!")
        }
    }

    /**
     * Loads a [Dependency] into the classpath if it is not already loaded
     *
     * @param dependency The [Dependency] to load
     * @return The [Dependency] file if is successfully loaded, null otherwise
     */
    fun loadDependency(dependency: Dependency): File? {
        addDependency(dependency)
        val file = File(File(librariesFolder, dependency.group.replace(".", "/")).folder(), "${dependency.artifactId}-${dependency.version}.jar")
        if(isLoaded(dependency)) return file
        if(!file.exists()){
            val repo = repositories.find { repo -> repo.findArtifact(dependency) != null } ?: return null
            val artifactData = repo.findArtifact(dependency) ?: return null
            digest.reset()
            val downloadBytes = URL(artifactData.get("url").asString).readBytes()
            val md5 = if(artifactData.has("md5")) artifactData.get("md5").asString else null
            if(md5 != null){
                val downloadMd5 = digest.digest(downloadBytes).joinToString("") { "%02x".format(it) }
                if(downloadMd5 != md5){
                    logger.severe("MD5 mismatch for ${dependency.group}:${dependency.artifactId}! Expected: '$md5', Got: '$downloadMd5'")
                    return null
                }
            }

            file.writeBytes(downloadBytes)
        }

        if(ClasspathLoader.loadIntoClasspath(file)){
            loadedDependencies.add(dependency.group + ":" + dependency.artifactId)
            logger.info("Loaded dependency ${dependency.group}:${dependency.artifactId}")
            return file
        } else {
            logger.severe("Failed to load dependency ${dependency.group}:${dependency.artifactId} into the classpath!")
        }
        return null
    }

    /**
     * Load all the dependencies into the classpath.
     * If any dependency is already loaded it'll be skipped
     */
    fun loadDependencies(): Unit = dependencies.forEach(this::loadDependency)

    /**
     * Checks if the given [Dependency] is already loaded
     * @return True if the dependency is loaded, false otherwise
     */
    fun isLoaded(dependency: Dependency): Boolean = loadedDependencies.contains(dependency.group + ":" + dependency.artifactId)
}