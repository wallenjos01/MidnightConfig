plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}

rootProject.name = "midnightcfg"

include("api")
include("api-sql")

include("codec-nbt")
include("codec-json")
include("codec-gson")
include("codec-binary")

include("cfgtool")

include("platform-minecraft")
