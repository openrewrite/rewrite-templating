import nebula.plugin.contacts.Contact
import nebula.plugin.contacts.ContactsExtension
import nebula.plugin.release.NetflixOssStrategies.SNAPSHOT
import nebula.plugin.release.git.base.ReleasePluginExtension
import nl.javadude.gradle.plugins.license.LicenseExtension
import java.util.*

plugins {
    `java-library`
    signing

    id("nebula.maven-resolved-dependencies") version "17.3.2"
    id("nebula.release") version "15.3.1"
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"

    id("com.github.hierynomus.license") version "0.16.1"
    id("com.github.jk1.dependency-license-report") version "1.16"
    id("org.owasp.dependencycheck") version "latest.release"

    id("nebula.maven-publish") version "17.3.2"
    id("nebula.contacts") version "5.1.0"
    id("nebula.info") version "11.1.0"

    id("nebula.javadoc-jar") version "17.3.2"
    id("nebula.source-jar") version "17.3.2"
    id("nebula.maven-apache-license") version "17.3.2"
}

group = "org.openrewrite"
description = "Auto-templating for rewrite-java."

apply(plugin = "nebula.publish-verification")

configure<ReleasePluginExtension> {
    defaultVersionStrategy = SNAPSHOT(project)
}

dependencyCheck {
    analyzers.assemblyEnabled = false
    suppressionFile = "suppressions.xml"
    failBuildOnCVSS = 9.0F
}
repositories {
    mavenCentral()
}

nexusPublishing {
    repositories {
        sonatype()
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
}

tasks.withType<Javadoc> {
    // assertTrue(boolean condition) -> assertThat(condition).isTrue()
    // warning - invalid usage of tag >
    // see also: https://blog.joda.org/2014/02/turning-off-doclint-in-jdk-8-javadoc.html
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

configure<ContactsExtension> {
    val j = Contact("team@moderne.io")
    j.moniker("Team Moderne")

    people["team@moderne.io"] = j
}

configure<LicenseExtension> {
    ext.set("year", Calendar.getInstance().get(Calendar.YEAR))
    skipExistingHeaders = true
    header = project.rootProject.file("gradle/licenseHeader.txt")
    mapping(kotlin.collections.mapOf("kt" to "SLASHSTAR_STYLE", "java" to "SLASHSTAR_STYLE"))
    strictCheck = true
}

configure<PublishingExtension> {
    publications {
        named("nebula", MavenPublication::class.java) {
            suppressPomMetadataWarningsFor("runtimeElements")

            pom.withXml {
                (asElement().getElementsByTagName("dependencies").item(0) as org.w3c.dom.Element?)?.let { dependencies ->
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
if(signingKey != null && signingPassword != null) {
    signing {
        isRequired = requireSigning
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["nebula"])
    }
} else if(requireSigning) {
    throw RuntimeException("Artifact signing is required, but signingKey and/or signingPassword are null")
}
