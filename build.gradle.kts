@file:Suppress("UnstableApiUsage")


plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.jib)

    distribution
    jacoco
    idea
    `jvm-test-suite`
}

group = "game"
version = "0.0.1-SNAPSHOT"

// custom config for otel agent distribution
val openTelemetryAgent: Configuration by configurations.creating

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
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude("org.apache.logging.log4j", "log4j-to-slf4j")
    }
    implementation("org.springframework:spring-webflux") {
        exclude("org.apache.logging.log4j", "log4j-to-slf4j")
    }
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
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
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.slf4j:slf4j-api")
    implementation("ch.qos.logback:logback-classic")
    implementation("ch.qos.logback:logback-core")

    implementation(libs.bouncycastle)

    testImplementation(libs.bundles.restassured)
    testImplementation(libs.asserj)

    implementation(libs.openapi.webmvc)

    // define the otel agent as dependency for the otel agent distribution
    openTelemetryAgent(libs.otel.java.agent)
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

distributions {
    // Provide a distribution task that downloads the otel agent and renames it statically.
    // Run the gradle task installOpenTelemetryAgent to download the jar: build/install/otel-agent/otel-javaagent.jar
    create("openTelemetryAgent") {
        distributionBaseName = "otel-agent"
        contents {
            from(openTelemetryAgent)
            rename("opentelemetry-javaagent-.*.jar", "otel-javaagent.jar")
        }
    }
}

jib {
    val imageBase = (property("image.base") as? String)
        ?: throw StopExecutionException("base image can not be determined from property image.base")

    val imageRegistry = (property("image.registry") as? String)?.let { "$it/" }
        ?: ""

    val imageRepository = (property("image.repository") as? String)?.takeIf { it.isNotBlank() }
        ?: rootProject.name

    val imageTags = (property("image.tag") as? String)?.split(",")
        ?: throw StopExecutionException("target image tag can not be determined from property image.tag")

    container {
        ports = listOf("8081/tcp")

        environment = mapOf(
            "SERVER_PORT" to "8081"
        )

        jvmFlags = listOf("-javaagent:/agent/otel/otel-javaagent.jar")
    }

    extraDirectories {
        paths {
            path {
                setFrom(layout.buildDirectory.file("./install/otel-agent"))
                into = "/agent/otel"
            }
        }
    }

    from {
        image = imageBase
    }
    to {
        image = "${imageRegistry}${imageRepository}/${project.name}"
        tags = imageTags.toSet()
        // we currently allow insecure target registries, this should be changed in the future, when the local setup
        // also provides https registry
        setAllowInsecureRegistries(true)
    }
}

/*
 * Configure the jib task to always use the otel agent distribution before building the image.
 */
tasks.jib {
    dependsOn(tasks.getByName("installOpenTelemetryAgentDist"))
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

testing {
    suites {
        /**
         * Defining the shared configuration for all test suites.
         * https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html#sharing_configuration_between_multiple_test_suites
         */
        withType<JvmTestSuite> {
            useJUnitJupiter()

            targets {
                all {
                    testTask.configure {
                        // enable dynamic agent loading during tests to hide warning
                        // agents are loaded during tests for IDE integration and analysis
                        jvmArgs("-XX:+EnableDynamicAgentLoading")

                        // Use system properties to set the active spring profile to "test", when tests are executed.
                        systemProperty("spring.profiles.active", "test")

                        // Always generate the jacoco coverage report after tests were run.
                        finalizedBy(tasks.jacocoTestReport)
                    }
                }
            }
        }

        /**
         * Configure the standard test suite, i.e. `src/test/kotlin`.
         *
         * https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html#sec:configuring_the_built_in_test_suite
         */
        @Suppress("unused")
        val test by getting(JvmTestSuite::class) {
            targets {
                all {
                    testTask.configure {
                        // use the distribution to ensure that the opentelemetry java agent jar is present
                        dependsOn(tasks.getByName("installOpenTelemetryAgentDist"))
                        // get otelAgentJar from build directory
                        val otelAgentJar =
                            layout.buildDirectory.file("install/otel-agent/otel-javaagent.jar").get().asFile

                        logger.info("Located opentelemetry agent at ${otelAgentJar.absolutePath}")

                        // attach agent using the absolute path
                        jvmArgs(
                            "-javaagent:${otelAgentJar.absolutePath}",
                        )

                        // https://opentelemetry.io/docs/languages/sdk-configuration/otlp-exporter/
                        // Note: Default is http/protobuf and generally uses port 4318 on the collectors.
                        // For larger payload and high throughput, gRPC is the way to go, but gRPC may not be supported
                        // in the overall network infrastructure due to its use of HTTP/2.
                        // The gRPC protocol, generally uses port 4317 on the collectors.
                        environment("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4318")

                        // Key-value pairs to be used as resource attributes.
                        // https://opentelemetry.io/docs/specs/semconv/resource/#semantic-attributes-with-dedicated-environment-variable
                        "${project.name}-${this.name}".also {
                            environment("OTEL_RESOURCE_ATTRIBUTES", "service.name=$it")
                            logger.info("Test run with opentelemetry agent, setting RESOURCE_ATTRIBUTES: service.name=$it")
                        }

                        // Configure the Logback Appender instrumentation
                        // https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/logback/logback-appender-1.0/javaagent
                        systemProperties(
                            mapOf(
                                "otel.instrumentation.logback-appender.experimental-log-attributes" to "true",
                                "otel.instrumentation.logback-appender.experimental.capture-marker-attribute" to "true",
                                "otel.instrumentation.logback-appender.experimental.capture-key-value-pair-attributes" to "true",
                                "otel.instrumentation.logback-appender.experimental.capture-code-attributes" to "true"
                            )
                        )
                    }
                }
            }
        }
    }
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