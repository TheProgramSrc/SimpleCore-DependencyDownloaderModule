package xyz.theprogramsrc.dependencydownloadermodule.objects

import org.json.XML
import xyz.theprogramsrc.simplecoreapi.libs.google.gson.JsonObject
import xyz.theprogramsrc.simplecoreapi.libs.google.gson.JsonParser
import java.net.URL

/**
 * Representation of a Repository
 * @param host The host of the repository
 * @param repository The repository id in the host
 */
data class Repository(val host: String, val repository: String) {

    /**
     * Checks if the repository is a Sonatype Nexus Repository
     * @param requiredVersion The required version of Nexus
     * @return true if the repository is a Sonatype Nexus Repository, false otherwise
     */
    fun isNexus(requiredVersion: Int = 3): Boolean = if(requiredVersion == 3) {
        try {
            (JsonParser.parseString(URL("${if(host.startsWith("http")) host else "https://${host}"}/service/rest/swagger.json").readText()).asJsonObject.get("info").asJsonObject.get("version").asString.substring(0,1).toIntOrNull() ?: 0) >= 3
        }catch (e: Exception) {
            false
        }
    }else if(requiredVersion == 2) {
        // TODO: Check if the repository is a Sonatype Nexus Repository v2
        false
    } else{
        false // Unknown or older versions
    }

    /**
     * Finds the artifact and returns a JsonObject with the url, version and md5 hash if available
     * @return A [JsonObject] with the information, null if not found
     */
    fun findArtifact(dependency: Dependency): JsonObject? {
        if(isNexus(3)) {
            val host = if(this.host.startsWith("http")) this.host else "https://${this.host}"
            val version = try {
                if (dependency.version.endsWith("-SNAPSHOT")) {
                    val json = JsonParser.parseString(XML.toJSONObject(URL("$host/repository/$repository/${dependency.group.replace(".", "/")}/${dependency.artifactId}/${dependency.version}/maven-metadata.xml").readText()).toString())
                        .asJsonObject
                        .getAsJsonObject("metadata")
                        .getAsJsonObject("versioning")
                        .getAsJsonObject("snapshot")
                    dependency.version.replace("-SNAPSHOT", "-${json.get("timestamp").asString}-${json.get("buildNumber").asString}")
                } else {
                    dependency.version
                }
            }catch (e: Exception) {
                e.printStackTrace()
                null
            } ?: return null

            val items = JsonParser.parseString(URL("$host/service/rest/v1/search?repository=$repository&group=${dependency.group}&format=maven2&maven.artifactId=${dependency.artifactId}&maven.extension=jar&sort=version&version=$version").readText()).asJsonObject.get("items").asJsonArray
            val item = items.firstOrNull { it.asJsonObject.get("version").asString == version } ?: return null
            val found = item.asJsonObject.get("assets").asJsonArray.firstOrNull {
                if(!it.asJsonObject.get("maven2").asJsonObject.get("version").asString.equals(version)) {
                    return@firstOrNull false // Check that the current version matches
                }

                if(!it.asJsonObject.get("maven2").asJsonObject.get("extension").asString.equals("jar")){
                    return@firstOrNull false // Check that this is a jar file (not checksums or pom)
                }

                if(it.asJsonObject.get("maven2").asJsonObject.has("classifier")){
                    if(it.asJsonObject.get("maven2").asJsonObject.get("classifier").asString.equals("sources")){
                        return@firstOrNull false // Check that this is not a sources jar file
                    }

                    if(it.asJsonObject.get("maven2").asJsonObject.get("classifier").asString.equals("javadoc")){
                        return@firstOrNull false // Check that this is not a javadoc jar file
                    }
                }

                if(!it.asJsonObject.get("maven2").asJsonObject.get("artifactId").asString.equals(dependency.artifactId)) {
                    return@firstOrNull false // Check that this is the correct artifact
                }

                if(!it.asJsonObject.get("contentType").asString.equals("application/java-archive")){
                    return@firstOrNull false // Check that this is a jar file
                }

                return@firstOrNull true
            }?.asJsonObject ?: return null
            val json = JsonObject()
            json.addProperty("url", found.get("downloadUrl").asString)
            if(found.has("md5")){
                json.addProperty("md5", found.get("md5").asString)
            }
            json.addProperty("version", version)
            return json
        }

        return null
    }
}
