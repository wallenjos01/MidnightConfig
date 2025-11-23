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

    minecraft("com.mojang:minecraft:1.21.10")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.18.1")

    // Fabric API
    val apiModules = listOf(
        "fabric-api-base",
        "fabric-lifecycle-events-v1"
    )
    for(mod in apiModules) {
        modApi(fabricApi.module(mod, "0.138.3+1.21.10"))
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
