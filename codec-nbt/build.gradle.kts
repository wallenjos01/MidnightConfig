plugins {
    id("midnightcfg-build")
    id("midnightcfg-multi-version")
    id("midnightcfg-publish")
}

//multiVersion {
//    useSourceDirectorySets()
//    defaultVersion(17)
//    additionalVersions(11,8)
//}
//
//patch {
//    patchSet("java8", sourceSets["main"], sourceSets["main"].java, multiVersion.getCompileTask(8))
//}

dependencies {

    api(project(":api"))
    implementation(project(":api"))
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.slf4j.api)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.slf4j.simple)
}
