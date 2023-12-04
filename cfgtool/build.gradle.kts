plugins {
    id("midnightcfg-build")
    id("application")
    alias(libs.plugins.shadow)
}

application {
    mainClass = "org.wallentines.mdcfg.tool.Main"
}

repositories {
    mavenCentral()
}

java {
    manifest {
        attributes(Pair("Main-Class", application.mainClass))
    }
}

dependencies {

    api(project(":api"))
    api(project(":codec-json"))
    api(project(":codec-binary"))

    api(libs.slf4j.api)
    api(libs.slf4j.simple)

    compileOnlyApi(libs.jetbrains.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}