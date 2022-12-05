import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jlleitschuh.gradle:ktlint-gradle:10.2.0")
    }
}

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(project(":spring-boot-starter-data-jdbc-plus-sql"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.h2database:h2")

    implementation("org.springframework.data:spring-data-jdbc:2.4.6")
    implementation("org.springframework.data:spring-data-relational:2.4.6")
    implementation("org.springframework.data:spring-data-commons:2.7.6")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
