plugins {
    id("midnightcfg-build")
}

repositories {
    mavenCentral()
}

dependencies {

    api(project(":api"))

    implementation(libs.slf4j.api)
    implementation(libs.google.gson)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group as String
            version = version as String
            artifactId = "midnightcfg-gson-codec"
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