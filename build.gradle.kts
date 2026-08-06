import com.ncorti.ktfmt.gradle.TrailingCommaManagementStrategy

plugins {
    alias(libs.plugins.benmanes.versions)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.boot.dependencies)
}

group = "com.damianryan"

version = "0.0.1-SNAPSHOT"

kotlin { jvmToolchain(25) }

repositories { mavenCentral() }

dependencies {
    annotationProcessor(libs.spring.boot.configuration.processor)

    implementation(libs.spring.boot.starter.cache)
    implementation(libs.spring.boot.starter.restclient)
    implementation(libs.caffeine)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.micrometer.tracing.bridge.brave)
    implementation(libs.threeten.extra)

    developmentOnly(libs.spring.boot.devtools)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(kotlin("test"))
}

ktfmt {
    kotlinLangStyle()
    maxWidth.set(120)
    blockIndent.set(4)
    continuationIndent.set(4)
    removeUnusedImports.set(true)
    trailingCommaManagementStrategy.set(TrailingCommaManagementStrategy.NONE)
}

tasks.test { useJUnitPlatform() }
