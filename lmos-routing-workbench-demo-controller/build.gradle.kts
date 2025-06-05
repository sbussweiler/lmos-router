val springBootVersion: String by project
val langChain4jCoreVersion: String by project
val langChain4jModulesVersion: String by project
val jacksonVersion: String by project

plugins {
    id("java")
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("plugin.spring") version "1.9.10"
}


dependencies {
    implementation(project(":lmos-routing-llm-spring-boot-starter"))
    implementation(project(":lmos-routing-vector-spring-boot-starter"))
    implementation(project(":lmos-routing-hybrid-spring-boot-starter"))

    implementation("org.springframework.boot:spring-boot-starter:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")

    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
}

tasks.test {
    useJUnitPlatform()
}

fun getProperty(propertyName: String) = System.getenv(propertyName) ?: project.findProperty(propertyName) as String
