import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("java")
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("plugin.spring") version "1.9.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("dev.langchain4j:langchain4j-spring-boot-starter:1.0.0-beta2")
    implementation("dev.langchain4j:langchain4j-azure-open-ai-spring-boot-starter:1.0.0-beta2")

    implementation("dev.langchain4j:langchain4j-embeddings:1.0.0-beta2")
    implementation("dev.langchain4j:langchain4j-embeddings-bge-small-en-v15-q:1.0.0-beta2")

    implementation("dev.langchain4j:langchain4j-hugging-face:1.0.0-beta1")
    implementation("dev.langchain4j:langchain4j-qdrant:1.0.0-beta1")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.3")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.2.2")
}


tasks.withType<BootJar> {
    mainClass.set("org.eclipse.lmos.router.RouterApplicationKt")
}

tasks.named<BootBuildImage>("bootBuildImage") {
    if (project.hasProperty("REGISTRY_URL")) {
        val registryUrl = getProperty("REGISTRY_URL")
        val registryUsername = getProperty("REGISTRY_USERNAME")
        val registryPassword = getProperty("REGISTRY_PASSWORD")
        val registryNamespace = getProperty("REGISTRY_NAMESPACE")

        imageName.set("$registryUrl/$registryNamespace/lmos-router-poc:0.1.0-SNAPSHOT")
        publish = true
        docker {
            publishRegistry {
                url.set(registryUrl)
                username.set(registryUsername)
                password.set(registryPassword)
            }
        }
    } else {
        imageName.set("lmos-router-poc:0.1.0-SNAPSHOT")
        publish = false
    }
}

tasks.test {
    useJUnitPlatform()
}

fun getProperty(propertyName: String) = System.getenv(propertyName) ?: project.findProperty(propertyName) as String
