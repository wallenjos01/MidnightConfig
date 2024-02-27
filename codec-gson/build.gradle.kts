plugins {
    id("midnightcfg-build")
    id("org.wallentines.gradle-multi-version") version "0.2.1"
}

multiVersion {
    defaultVersion(17)
    additionalVersions(11,8)
}

repositories {
    mavenCentral()
}

dependencies {

    api(project(":api"))
    implementation(project(":api"))
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.slf4j.api)
    implementation(libs.google.gson)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.slf4j.simple)
}