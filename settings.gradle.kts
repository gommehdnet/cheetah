pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

rootProject.name = "cheetah-1.21.1"

include("cheetah-api", "cheetah-server")
