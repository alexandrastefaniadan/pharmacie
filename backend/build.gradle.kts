plugins {
    java
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ma.pharmacie"
version = "0.0.1-SNAPSHOT"
description = "Pharmacie - pharmacist helper backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

extra["testcontainersVersion"] = "1.20.4"
extra["mapstructVersion"] = "1.6.3"
extra["springdocVersion"] = "2.7.0"
extra["poiVersion"] = "5.3.0"

dependencies {
    // --- Spring Boot starters ---
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // --- Database & migrations ---
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // --- API docs (Swagger UI at /swagger-ui.html) ---
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springdocVersion")}")

    // --- DTO mapping ---
    implementation("org.mapstruct:mapstruct:${property("mapstructVersion")}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")

    // --- Lombok ---
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Lombok + MapStruct interaction (required when using @Builder on DTOs)
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // --- Excel import (used by the import feature) ---
    implementation("org.apache.poi:poi-ooxml:${property("poiVersion")}")

    // --- Dev tooling ---
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // --- Tests ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(
        listOf(
            "-parameters",
            "-Amapstruct.defaultComponentModel=spring",
            "-Amapstruct.unmappedTargetPolicy=ERROR",
        )
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
}

