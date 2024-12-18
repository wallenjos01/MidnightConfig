plugins {
    id("build.library")
    id("build.multiversion")
    id("build.publish")
}

dependencies {

    compileOnlyApi(libs.jetbrains.annotations)
    compileOnly(libs.jetbrains.annotations)
    implementation(libs.slf4j.api)

    testImplementation(project(":codec-json"))
    testRuntimeOnly(libs.slf4j.simple)
}