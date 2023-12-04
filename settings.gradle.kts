rootProject.name = "midnightcfg"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
    }

    includeBuild("gradle/plugins")
}

include("api")

include("codec-json")
include("codec-gson")
include("codec-binary")

include("cfgtool")