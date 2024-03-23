plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
    workingDir("run/test")
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = if(rootProject == project) {
            project.name
        } else {
            rootProject.name + "-" + project.name
        }
        from(components["java"])
    }

    if (project.hasProperty("pubUrl")) {

        var url: String = project.properties["pubUrl"] as String
        url += if(GradleVersion.version(version as String).isSnapshot) {
            "snapshots"
        } else {
            "releases"
        }

        repositories.maven(url) {
            name = "pub"
            credentials(PasswordCredentials::class.java)
        }
    }
}