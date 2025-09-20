import kotlin.io.path.exists

version = "1.0.0-SNAPSHOT"

dependencies {
    compileOnly(project(":cheetah-api"))
    compileOnly(project(":cheetah-server"))
}

val pluginsDir: java.nio.file.Path = rootProject.layout.projectDirectory.dir("run/plugins").asFile.toPath()

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

if (pluginsDir.exists()) {
    val copyArtifacts by tasks.registering(Copy::class) {
        description = "Copy the test plugin jar to the test server plugins directory"
        group = "build"

        from(tasks.jar.flatMap { it.archiveFile })
        into(pluginsDir)
    }

    tasks.assemble {
        finalizedBy(copyArtifacts)
    }
}
