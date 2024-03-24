plugins {
    id("java")
    id("java-library")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://maven.wallentines.org/releases")

    if(GradleVersion.version(version as String).isSnapshot) {
        maven("https://maven.wallentines.org/snapshots")
    }
}

tasks.test {
    useJUnitPlatform()
    workingDir("run/test")
}