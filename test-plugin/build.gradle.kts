version = "1.0.0-SNAPSHOT"

dependencies {
    compileOnly(project(":cheetah-api"))
    compileOnly(project(":cheetah-server"))
}

tasks.processResources {
    var apiVersion = rootProject.providers.gradleProperty("mcVersion").get()
    // Bukkit api versioning does not support suffixed versions
    apiVersion = apiVersion.substringBefore('-')

    val props = mapOf(
        "version" to project.version,
        "apiVersion" to "\"$apiVersion\""
    )
    inputs.properties(props)
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
