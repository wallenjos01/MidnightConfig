plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
    workingDir("run/test")
}