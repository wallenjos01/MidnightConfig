plugins {
    id("build.application")
    id("build.shadow")
}

application {
    mainClass = "org.wallentines.mdcfg.tool.Main"
}

java {
    manifest {
        attributes(Pair("Main-Class", application.mainClass))
    }
}

dependencies {

    implementation(project(":api"))
    implementation(project(":codec-json"))
    implementation(project(":codec-binary"))

    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)

    compileOnly(libs.jetbrains.annotations)
}