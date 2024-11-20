rootProject.name = "api"

pluginManagement {
    repositories {
        maven("https://maven.radsteve.net/sploon")
        gradlePluginPortal()
    }
}
include("examples")
include("examples:bundling")
findProject(":examples:bundling")?.name = "bundling"
