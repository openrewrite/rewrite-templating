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

dependencies {
    implementation("org.openrewrite:rewrite-java:latest.integration")

    implementation(project(":processor"))
    annotationProcessor(project(":processor"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
    testImplementation("org.junit.jupiter:junit-jupiter-params:latest.release")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")

    testImplementation("org.openrewrite:rewrite-test:latest.integration")

    testImplementation("org.assertj:assertj-core:latest.release")
    testRuntimeOnly("org.openrewrite:rewrite-java-11:latest.integration")
}
