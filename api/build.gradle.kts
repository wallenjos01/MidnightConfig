plugins {
    id("midnightcfg-build")
}

repositories {
    mavenCentral()
}

dependencies {

    compileOnlyApi(libs.jetbrains.annotations)
    implementation(libs.slf4j.api)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(project(":codec-json"))
    testRuntimeOnly(libs.slf4j.simple)
}