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

    val fabricApiVersion = "0.144.0+26.1"

    minecraft("com.mojang:minecraft:26.1")
    implementation("net.fabricmc:fabric-loader:0.18.4")

    // Fabric API
    val apiModules = listOf(
        "fabric-api-base",
        "fabric-lifecycle-events-v1"
    )
    for(mod in apiModules) {
        api(fabricApi.module(mod, "${fabricApiVersion}"))
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


    // Gametest API modules
    val testApiModules = listOf(
        "fabric-gametest-api-v1",
        "fabric-registry-sync-v0"
    )
    for(mod in testApiModules) {
        gametestImplementation(fabricApi.module(mod, "${fabricApiVersion}"))
    }
}
