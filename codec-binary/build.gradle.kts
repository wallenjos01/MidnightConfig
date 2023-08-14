plugins {
    id("midnightcfg-build")
}

repositories {
    mavenCentral()
}

dependencies {

    api(project(":api"))
    api(libs.zstd.jni)

    implementation(libs.slf4j.api)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(project(":codec-json"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group as String
            version = version as String
            artifactId = "midnightcfg-binary-codec"
            from(components["java"])
        }
    }
    repositories {
        if (project.hasProperty("pubUrl")) {
            maven {
                name = "pub"
                url = uri(project.properties["pubUrl"] as String)
                credentials(PasswordCredentials::class.java)
            }
        }
    }
}