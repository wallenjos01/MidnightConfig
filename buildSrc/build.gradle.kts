plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven("https://maven.wallentines.org/plugins")
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:9.4.1")
    implementation("org.wallentines:gradle-multi-version:0.3.0")
    implementation("org.wallentines:gradle-patch:0.2.0")
    implementation("net.fabricmc:fabric-loom:1.16-SNAPSHOT")
}
