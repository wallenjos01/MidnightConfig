import buildlogic.Utils

plugins {
    id("build.library")
    id("build.fabric")
    id("build.publish")
}

loom {
    accessWidenerPath = file("src/main/resources/midnightcfg.accesswidener")
}

Utils.setupResources(project, rootProject, "fabric.mod.json")

dependencies {

    minecraft("com.mojang:minecraft:1.21.5")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.10")

    // Fabric API
    val apiModules = listOf(
        "fabric-api-base",
        "fabric-lifecycle-events-v1"
    )
    for(mod in apiModules) {
        modApi(fabricApi.module(mod, "0.119.5+1.21.5"))
    }

    api(project(":api"))
    api(project(":api-sql"))
    api(project(":codec-json"))
    api(project(":codec-binary"))

    shadow(project(":api"))
    shadow(project(":api-sql"))
    shadow(project(":codec-json"))
    shadow(project(":codec-binary"))

    compileOnly(libs.jetbrains.annotations)
    implementation(libs.slf4j.api)

    testRuntimeOnly(libs.slf4j.simple)
}
