plugins {
    id("midnightcfg-build")
    id("midnightcfg-multi-version")
    id("midnightcfg-publish")
}

dependencies {

    compileOnlyApi(libs.jetbrains.annotations)
    compileOnly(libs.jetbrains.annotations)
    implementation(libs.slf4j.api)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(project(":codec-json"))
    testRuntimeOnly(libs.slf4j.simple)
}