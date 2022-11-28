plugins {
    `java-library`
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation("org.projectlombok:lombok:latest.release")
    implementation("org.openrewrite:rewrite-java:latest.integration")

    testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
    testImplementation("org.junit.jupiter:junit-jupiter-params:latest.release")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")

    testImplementation("org.openrewrite:rewrite-test:latest.integration")

    testImplementation("org.assertj:assertj-core:latest.release")

    testImplementation("org.openrewrite:rewrite-test:latest.integration")
    testRuntimeOnly("org.openrewrite:rewrite-java-11:latest.integration")

    testImplementation("org.jooq:joor:latest.release")
}
