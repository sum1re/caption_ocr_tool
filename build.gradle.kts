plugins {
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("kapt") version "2.3.21"
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
}

group = "com.neo.caption"
version = "1.0.0-alpha"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:2.0.0")
    }
}

dependencies {
    // cache
    implementation(libs.bundles.spring.cache)
    // web
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    runtimeOnly("com.h2database:h2")
    implementation(libs.bundles.spring.database)
    implementation(libs.bundles.common.utils)
    implementation(libs.bundles.jackson)
    implementation(libs.bundles.spring.ai)
    // dev
    developmentOnly("org.springframework.boot:spring-boot-starter-actuator")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

springBoot {
    buildInfo {
        properties {
            additional.putAll(
                mapOf(
                    "version.java" to JavaVersion.current().name,
                    "version.gradle" to project.gradle.gradleVersion,
                    "version.kotlin" to "2.3.21",
                    "license" to "Apache License 2.0"
                )
            )
        }
    }
}