version = "1.0.0-SNAPSHOT"

dependencies {
    compileOnly(project(":cheetah-api"))
    compileOnly(project(":cheetah-server"))
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "apiVersion" to "\"${rootProject.providers.gradleProperty("apiVersion").get()}\""
    )
    inputs.properties(props)
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
