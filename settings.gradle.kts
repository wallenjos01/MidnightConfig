rootProject.name = "midnightcfg"

pluginManagement {
    includeBuild("gradle/plugins")
}

include("api")
include("codec-json")
include("codec-gson")
include("codec-binary")