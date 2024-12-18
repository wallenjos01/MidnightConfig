plugins {
    id("build.library")
    id("build.multiversion")
    id("build.publish")
}

dependencies {

    api(project(":api"))
    implementation(project(":api"))
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.zstd.jni)
    implementation(libs.slf4j.api)

    testRuntimeOnly(libs.slf4j.simple)
}