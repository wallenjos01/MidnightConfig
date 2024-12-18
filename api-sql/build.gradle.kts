plugins {
    id("build.library")
    id("build.multiversion")
    id("build.publish")
}

dependencies {

    api(project(":api"))
    implementation(project(":api"))

    compileOnlyApi(libs.jetbrains.annotations)
    compileOnly(libs.jetbrains.annotations)
    implementation(libs.slf4j.api)

    compileOnly("com.mysql:mysql-connector-j:8.3.0")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.3.3")
    compileOnly("org.xerial:sqlite-jdbc:3.45.2.0")
    compileOnly("com.h2database:h2:2.2.224")

    testRuntimeOnly(libs.slf4j.simple)
}