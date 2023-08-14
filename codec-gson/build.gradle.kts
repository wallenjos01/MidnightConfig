plugins {
    id("midnightcfg-build")
}

repositories {
    mavenCentral()
}

dependencies {

    api(project(":api"))

    implementation(libs.slf4j.api)
    implementation(libs.google.gson)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.slf4j.simple)
}