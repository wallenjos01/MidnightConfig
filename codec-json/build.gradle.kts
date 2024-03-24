plugins {
    id("midnightcfg-build")
    id("midnightcfg-multi-version")
    id("midnightcfg-publish")
}

dependencies {

    api(project(":api"))
    implementation(project(":api"))
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.slf4j.api)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.slf4j.simple)
}
