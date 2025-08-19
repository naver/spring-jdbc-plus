import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("spring.jdbc.plus.spring-bom-conventions")
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
    kotlin("jvm")
    kotlin("plugin.spring") version "2.2.0"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("17")
        javaParameters = true
    }
}

ktlint {
    outputToConsole.set(true)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(project(":spring-boot-starter-data-jdbc-plus-sql"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.h2database:h2")

    implementation("org.springframework.data:spring-data-jdbc")
    implementation("org.springframework.data:spring-data-relational")
    implementation("org.springframework.data:spring-data-commons")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
