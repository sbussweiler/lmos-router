import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("java")
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("plugin.spring") version "1.9.10"
}


dependencies {
    implementation(project(":lmos-routing-core-spring-boot-starter"))
    implementation(project(":lmos-routing-llm-spring-boot-starter"))
    implementation(project(":lmos-routing-vector-spring-boot-starter"))
    implementation(project(":lmos-routing-hybrid-spring-boot-starter"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.3")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.2.2")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")
}

tasks.test {
    useJUnitPlatform()
}

fun getProperty(propertyName: String) = System.getenv(propertyName) ?: project.findProperty(propertyName) as String
