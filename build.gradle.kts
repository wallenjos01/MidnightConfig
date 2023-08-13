import java.net.URI

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

group = "org.wallentines"
version = "1.1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {

    compileOnly(libs.jetbrains.annotations)
    implementation(libs.slf4j.api)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
    workingDir("run/test")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group as String
            version = version as String
            from(components["java"])
        }
    }
    repositories {
        if (project.hasProperty("pubUrl")) {
            maven {
                name = "pub"
                url = URI.create(project.properties["pubUrl"] as String)
                credentials(PasswordCredentials::class.java)
            }
        }
    }
}