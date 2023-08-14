rootProject.name = "midnightcfg"

pluginManagement {
    includeBuild("gradle/build-logic")
}

include("api")
include("codec-json")
include("codec-gson")
include("codec-binary")