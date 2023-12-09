pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

rootProject.name = "cheetah-1.20.4"

include("cheetah-api", "cheetah-server")
