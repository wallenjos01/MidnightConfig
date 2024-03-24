rootProject.name = "midnightcfg"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.wallentines.org/plugins")
    }

    includeBuild("gradle/plugins")
}

include("api")

include("codec-nbt")
include("codec-json")
include("codec-gson")
include("codec-binary")

include("cfgtool")