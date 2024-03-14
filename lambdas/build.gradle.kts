import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:2.6.7")
    }
}

group = "org.openapitools"
version = "1.0.0"

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

plugins {
    val kotlinVersion = "1.6.21"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-mustache")
  implementation("org.springframework.boot:spring-boot-starter-web")
  
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
 
  implementation("org.jetbrains.kotlin:kotlin-reflect")
 
  implementation("org.springdoc:springdoc-openapi-ui:1.6.8")
 
  implementation("com.google.code.findbugs:jsr305:3.0.2")

  implementation("jakarta.validation:jakarta.validation-api")
  implementation("jakarta.annotation:jakarta.annotation-api:2.1.0")

  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.springframework.boot:spring-boot-devtools")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// dependencies {
//     compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//     compile("org.jetbrains.kotlin:kotlin-reflect")
//     compile("org.springframework.boot:spring-boot-starter-web")
//     compile("org.springdoc:springdoc-openapi-ui:1.6.8")

//     compile("com.google.code.findbugs:jsr305:3.0.2")
//     compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
//     compile("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
//     compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
//     compile("com.fasterxml.jackson.module:jackson-module-kotlin")
//     compile("jakarta.validation:jakarta.validation-api")
//     compile("jakarta.annotation:jakarta.annotation-api:2.1.0")

//     testCompile("org.jetbrains.kotlin:kotlin-test-junit5")
//     testCompile("org.springframework.boot:spring-boot-starter-test") {
//         exclude(module = "junit")
//     }
// }
