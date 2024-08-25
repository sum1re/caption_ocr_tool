import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "2.0.20"
    kotlin("kapt") version "2.0.20"
}

group = "com.neo.caption"
version = "1.0.0-alpha"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}
val liteflowVersion = "2.12.2"
val exposedVersion = "0.53.0"
val kotlinLoggingVersion = "7.0.0"
dependencies {
    // bytedeco
    implementation(libs.bundles.bytedeco)
    // cache
    implementation(libs.bundles.spring.cache)
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // jpa
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")
    runtimeOnly("com.h2database:h2")
    // liteflow
    implementation("com.yomahub:liteflow-spring-boot-starter:$liteflowVersion")
    implementation("com.yomahub:liteflow-el-builder:$liteflowVersion")
    // util
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLoggingVersion")
    implementation("com.appmattus.crypto:cryptohash:0.10.1")
    // dev
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-starter-actuator")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
