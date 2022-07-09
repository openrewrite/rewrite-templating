plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    mavenCentral()
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, TimeUnit.SECONDS)
        cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
    }
}

dependencies {
    implementation("com.google.errorprone:error_prone_refaster:2.14.0")

    implementation("org.openrewrite:rewrite-java:latest.integration")
    implementation("org.openrewrite:rewrite-java-11:latest.integration")

    testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
    testImplementation("org.junit.jupiter:junit-jupiter-params:latest.release")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")

    testImplementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.openrewrite:rewrite-test:latest.integration")

    testImplementation("org.assertj:assertj-core:latest.release")

    testImplementation("com.google.testing.compile:compile-testing:latest.release")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}
