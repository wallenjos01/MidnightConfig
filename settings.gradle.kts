rootProject.name = "midnightcfg"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.wallentines.org/plugins")
    }

    includeBuild("gradle/plugins")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include("api")
include("api-sql")

include("codec-nbt")
include("codec-json")
include("codec-gson")
include("codec-binary")

include("cfgtool")