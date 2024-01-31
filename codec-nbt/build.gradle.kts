plugins {
    id("midnightcfg-build")
    id("org.wallentines.gradle-multi-version") version "0.2.1"
    id("org.wallentines.gradle-patch") version "0.2.0"
}

multiVersion {
    useSourceDirectorySets()
    defaultVersion(17)
    additionalVersions(11,8)
}

patch {
    patchSet("java8", sourceSets["main"], sourceSets["main"].java, multiVersion.getCompileTask(8))
    patchSet("java11", sourceSets["main"], sourceSets["main"].java, multiVersion.getCompileTask(11))
}

repositories {
    mavenCentral()
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
