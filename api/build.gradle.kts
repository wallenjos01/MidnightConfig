plugins {
    id("midnightcfg-build")
    id("org.wallentines.gradle-multi-version") version "0.2.1-SNAPSHOT"
    id("org.wallentines.gradle-patch") version "0.1.1-SNAPSHOT"
}

multiVersion {
    useSourceDirectorySets()
    defaultVersion(17)
    additionalVersions(11,8)
}

patch {
    patchSet("java8", sourceSets["main"], sourceSets["main"].java, multiVersion.getCompileTask(8))
}

repositories {
    mavenCentral()
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