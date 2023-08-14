plugins {
    id("midnightcfg-build")
}

repositories {
    mavenCentral()
}

dependencies {

    api(project(":api"))

    implementation(libs.zstd.jni)
    implementation(libs.slf4j.api)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(project(":codec-json"))
    testRuntimeOnly(libs.slf4j.simple)
}