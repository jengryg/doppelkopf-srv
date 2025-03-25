plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency)
    alias(libs.plugins.kotlin.jpa)

    jacoco
    idea
}

group = "game"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

springBoot {
    buildInfo()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude("org.apache.logging.log4j", "log4j-to-slf4j")
    }
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.session:spring-session-core")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("io.micrometer:micrometer-registry-otlp")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.mockito", "mockito-core")
        exclude("junit", "junit")
    }
    testImplementation(libs.mockk)
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.slf4j:slf4j-api")
    implementation("ch.qos.logback:logback-classic")
    implementation("ch.qos.logback:logback-core")
}

kotlin {
    // https://kotlinlang.org/docs/gradle-compiler-options.html#how-to-define-options
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        // https://kotlinlang.org/docs/java-interop.html#compiler-configuration
        // https://docs.spring.io/spring-framework/reference/languages/kotlin/null-safety.html
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

/*
 * Configure the resource processing tasks to expand project.properties into configuration files named
 * `application.yaml` or `application-*.yaml`.
 *
 * Each file matching this naming criteria, is transformed using the `groovy.text.SimpleTemplateEngine`. This allows the
 * use of property references like `$property` or `${property}` as well as arbitrary Groovy code in the files.
 *
 * See https://docs.gradle.org/current/kotlin-dsl/gradle/org.gradle.api.file/-content-filterable/expand.html
 */
tasks.withType<ProcessResources> {
    outputs.upToDateWhen { false }
    // force the process resources task to always run

    filesMatching(
        listOf(
            "**/application.yaml",
            "**/application-*.yaml",
            "**/application.yml",
            "**/application-*.yml",
        )
    ) {
        logger.debug("Processing resource: {}", this.path)

        // Disable expand until we really need it. Then we have to take a look at:
        // https://stackoverflow.com/questions/60352025/gradle-copy-task-expand-yaml-file-escape-whole-string
        // to avoid escapement issues in the yaml files.
        //expand(project.properties)

    }
}

tasks.withType<Test> {
    // Use system properties to set the active spring profile to "test", when tests are executed.
    systemProperty("spring.profiles.active", "test")

    useJUnitPlatform()

    // Always generate the jacoco coverage report after tests were run.
    finalizedBy(tasks.jacocoTestReport)
}

tasks.withType<JacocoReport> {
    // Ensure that test run is up-to-date, before the jacoco coverage report is generated.
    dependsOn(tasks.test)

    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/generated/**/*.class"
                    )
                }
            }
        )
    )

    reports {
        xml.required.set(true)
        // enable xml output format
        html.required.set(true)
        // enable html output format
        csv.required.set(true)
        // enable csv output format
    }
}

/*
 * https://docs.gradle.org/current/dsl/org.gradle.plugins.ide.idea.model.IdeaModule.html
 */
idea {
    module {
        isDownloadJavadoc = true
        // always download docs from dependencies
        isDownloadSources = true
        // always download source from dependencies
    }
}