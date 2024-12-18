plugins {
    id("build.library")
    id("build.multiversion")
    id("build.publish")
}

dependencies {

    api(project(":api"))
    implementation(project(":api"))
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.slf4j.api)
    implementation(libs.google.gson)

    testRuntimeOnly(libs.slf4j.simple)
}