plugins {
    id("midnightcfg-build")
    id("midnightcfg-multi-version")
    id("midnightcfg-publish")
}

dependencies {

    api(project(":api"))
    implementation(project(":api"))

    compileOnlyApi(libs.jetbrains.annotations)
    compileOnly(libs.jetbrains.annotations)
    implementation(libs.slf4j.api)

    compileOnly("mysql:mysql-connector-java:8.0.33")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.3.3")
    compileOnly("org.xerial:sqlite-jdbc:3.45.2.0")
    compileOnly("com.h2database:h2:2.2.224")

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation("mysql:mysql-connector-java:8.0.33")
    testImplementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")
    testImplementation("org.xerial:sqlite-jdbc:3.45.2.0")
    testImplementation("com.h2database:h2:2.2.224")
    testRuntimeOnly(libs.slf4j.simple)
}