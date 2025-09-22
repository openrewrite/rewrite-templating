import nebula.plugin.contacts.Contact
import nebula.plugin.contacts.ContactsExtension
import nebula.plugin.release.NetflixOssStrategies.SNAPSHOT
import nebula.plugin.release.git.base.ReleasePluginExtension
import nl.javadude.gradle.plugins.license.LicenseExtension
import java.util.*

plugins {
    `java-library`
    signing

    id("com.netflix.nebula.maven-resolved-dependencies") version "21.0.0"
    id("com.netflix.nebula.release") version "19.0.10"
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"

    id("com.github.hierynomus.license") version "0.16.1"
    id("com.github.jk1.dependency-license-report") version "1.16"
    id("org.owasp.dependencycheck") version "latest.release"

    id("com.netflix.nebula.maven-publish") version "21.0.0"
    id("com.netflix.nebula.contacts") version "7.0.1"
    id("com.netflix.nebula.info") version "13.1.2"

    id("com.netflix.nebula.javadoc-jar") version "21.0.0"
    id("com.netflix.nebula.source-jar") version "21.0.0"
    id("com.netflix.nebula.maven-apache-license") version "21.0.0"
}

group = "org.openrewrite"
description = "Auto-templating for rewrite-java."

apply(plugin = "com.netflix.nebula.publish-verification")

configure<ReleasePluginExtension> {
    defaultVersionStrategy = SNAPSHOT(project)
}

dependencyCheck {
    analyzers.assemblyEnabled = false
    suppressionFile = "suppressions.xml"
    failBuildOnCVSS = 9.0F
    nvd.apiKey = System.getenv("NVD_API_KEY")

}
repositories {
    mavenLocal()
    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
    mavenCentral()
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, TimeUnit.SECONDS)
        cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

val compiler = javaToolchains.compilerFor {
    languageVersion.set(JavaLanguageVersion.of(8))
}
val tools = compiler.get().metadata.installationPath.file("lib/tools.jar")

dependencies {
    compileOnly(files(tools))
    compileOnly("org.jetbrains:annotations:24.0.+")

    compileOnly("org.projectlombok:lombok:latest.release")
    annotationProcessor("org.projectlombok:lombok:latest.release")
    compileOnly("org.openrewrite:rewrite-java:latest.release")
    implementation("org.jspecify:jspecify:latest.release")

    // Needed for annotation processing tests
    testImplementation(files(tools))
    testImplementation("org.openrewrite:rewrite-java:latest.integration")
    testImplementation("org.openrewrite:rewrite-test:latest.integration")
    testRuntimeOnly("org.openrewrite:rewrite-java-25:latest.integration")
    // Skip `2.1.0-alpha0` for now over "class file has wrong version 55.0, should be 52.0"
    testImplementation("org.slf4j:slf4j-api:2.0.+")
    testImplementation("com.google.testing.compile:compile-testing:latest.release")
    testImplementation("jakarta.annotation:jakarta.annotation-api:2.+")
    testImplementation("javax.annotation:javax.annotation-api:1.+")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.13.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.3")
}

// Configure main source compilation for Java 8
tasks.named<JavaCompile>("compileJava") {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

// Configure test source compilation for Java 25
tasks.named<JavaCompile>("compileTestJava") {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    sourceCompatibility = JavaVersion.VERSION_25.toString()
    targetCompatibility = JavaVersion.VERSION_25.toString()

    options.compilerArgs.addAll(
        listOf(
            "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
            "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
            "--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
            "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
            "--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
            "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
            "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
            "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
        )
    )
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
    jvmArgs(
        "--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
    )
}

tasks.withType<Javadoc>().configureEach {
    // assertTrue(boolean condition) -> assertThat(condition).isTrue()
    // warning - invalid usage of tag >
    // see also: https://blog.joda.org/2014/02/turning-off-doclint-in-jdk-8-javadoc.html
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

configure<ContactsExtension> {
    val team = Contact("team@moderne.io")
    team.moniker("Team Moderne")
    people["team@moderne.io"] = team
}

configure<LicenseExtension> {
    ext.set("year", Calendar.getInstance().get(Calendar.YEAR))
    skipExistingHeaders = true
    header = project.rootProject.file("gradle/licenseHeader.txt")
    mapping(kotlin.collections.mapOf("kt" to "SLASHSTAR_STYLE", "java" to "SLASHSTAR_STYLE"))
    strictCheck = true
    exclude("recipes/")
}

// configure license information in MANIFEST.MF to enable documentation generation
tasks.jar {
    manifest {
        attributes(
            "License-Name" to "Apache License Version 2.0",
            "License-Url" to "https://www.apache.org/licenses/LICENSE-2.0"
        )
    }
}

configure<PublishingExtension> {
    publications {
        named("nebula", MavenPublication::class.java) {
            suppressPomMetadataWarningsFor("runtimeElements")

            pom.withXml {
                (asElement().getElementsByTagName("dependencies")
                    .item(0) as org.w3c.dom.Element?)?.let { dependencies ->
                    dependencies.getElementsByTagName("dependency").let { dependencyList ->
                        var i = 0
                        var length = dependencyList.length
                        while (i < length) {
                            (dependencyList.item(i) as org.w3c.dom.Element).let { dependency ->
                                if ((dependency.getElementsByTagName("scope")
                                        .item(0) as org.w3c.dom.Element).textContent == "provided"
                                ) {
                                    dependencies.removeChild(dependency)
                                    i--
                                    length--
                                }
                            }
                            i++
                        }
                    }
                }
            }
        }
    }
}

val signingKey: String? by project
val signingPassword: String? by project
val requireSigning = project.hasProperty("forceSigning") || project.hasProperty("releasing")
if (signingKey != null && signingPassword != null) {
    signing {
        isRequired = requireSigning
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["nebula"])
    }
} else if (requireSigning) {
    throw RuntimeException("Artifact signing is required, but signingKey and/or signingPassword are null")
}
