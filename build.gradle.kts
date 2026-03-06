import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.zip.ZipFile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
    id("me.filippov.gradle.jvm.wrapper") version "0.14.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// ============ Plugin File Definitions (Single Source of Truth) ==============

val pluginStagingDir = layout.buildDirectory.dir("plugin-staging").get().asFile
val pluginStagingContentDir = file("${pluginStagingDir}/${rootProject.name}")
val signingManifestFile = file("${pluginStagingDir}/files-to-sign.txt")

// JAR files that need signing (only our own code)
val jarFilesToSign = mutableListOf<String>().apply {
    add("${rootProject.name}-${version}.jar")
    if (intellijPlatform.buildSearchableOptions.get()) {
        add("${rootProject.name}-${version}-searchableOptions.jar")
    }
}.toList()

// Configure project's dependencies
repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
        jetbrainsRuntime()
    }
}

dependencies {
    intellijPlatform {
        rider(properties("platformVersion")) {
            useInstaller = false
        }
        bundledPlugin("com.jetbrains.rider-cpp")
        jetbrainsRuntime()
    }
}

intellijPlatform {
    pluginConfiguration {
//        name = providers.gradleProperty("pluginName")
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    path.set(file("CHANGELOG.md").canonicalPath)
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
    lineSeparator.set("\n")
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
//qodana {
//    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
//    reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
//    saveReport.set(true)
//    showReport.set(System.getenv("QODANA_SHOW_REPORT").toBoolean())
//}

kotlin {
    jvmToolchain(21)
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        sinceBuild.set(properties("pluginSinceBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            changelog.renderItem(
                changelog.run{
                    getOrNull(properties("pluginVersion")) ?: getUnreleased()
                }
                    .withHeader(false)
                    .withEmptySections(false),
                Changelog.OutputType.HTML
            )
        })
    }


    // ========= Two-Phase Build for Signing Support ================

    // Preparation for plugin internals signing. Build all JARs and put them into ${pluginStagingDir}
    val preparePluginInternalsForSigning by registering(Sync::class) {
        description = "Prepares plugin files for signing and generates signing manifest"
        group = "build"

        // Source 1: Copy the full plugin directory from prepareSandbox
        from(prepareSandbox.map { it.pluginDirectory })

        // Source 2: Copy searchable options JAR to lib/ (when enabled)
        if (intellijPlatform.buildSearchableOptions.get()) {
            from(jarSearchableOptions.map { it.archiveFile }) {
                into("lib")
            }
        }

        // Destination: the plugin content directory inside staging
        into(pluginStagingContentDir)

        // Capture script-level vals into locals to avoid capturing the build script in doLast
        val signingManifestFile = signingManifestFile
        val pluginStagingDir = pluginStagingDir
        val jarFilesToSign = jarFilesToSign
        val projectName = rootProject.name

        // After syncing, generate the signing manifest
        doLast {
            val filesToSign = mutableListOf<String>()

            // Add JAR files that need signing (only our own code)
            jarFilesToSign.forEach { jarName ->
                filesToSign.add("${projectName}/lib/${jarName}")
            }

            // Write manifest
            signingManifestFile.writeText(filesToSign.joinToString("\n"))

            // Summary
            println("Plugin prepared for signing: ${pluginStagingDir}")
            println("Signing manifest: ${signingManifestFile}")
            println("Files to sign: ${filesToSign.size}")
            filesToSign.forEach { println("  - $it") }
        }
    }

    // Validates that ${pluginStagingDir} has all required files to assemble the plugin
    val validatePluginStaging by registering {
        description = "Validates that plugin staging directory exists and contains required files"
        group = "build"

        // Capture script-level vals into locals to avoid capturing the build script in doLast
        val pluginStagingContentDir = pluginStagingContentDir
        val jarFilesToSign = jarFilesToSign

        doLast {
            if (!pluginStagingContentDir.exists()) {
                throw RuntimeException(
                    "Plugin staging directory not found: ${pluginStagingContentDir}\n" +
                    "Run './gradlew preparePluginInternalsForSigning' first."
                )
            }

            jarFilesToSign.forEach { jarName ->
                val file = pluginStagingContentDir.resolve("lib/${jarName}")
                if (!file.exists()) throw RuntimeException("Expected JAR file not found: ${file}")
            }
        }
    }

    // Assembles the final zip-archive from staged (potentially externally signed) files.
    // Produces a ZIP with "-from-staging" suffix by default (override with -PoutputPluginFileSuffix=<value>)
    // Can be used in pipeline: preparePluginInternalsForSigning -> external sign -> assemblePlugin
    val assemblePlugin by registering(Zip::class) {
        description = "Assembles the plugin ZIP from staged files with '-from-staging' classifier"
        group = "build"

        dependsOn(validatePluginStaging)

        from(pluginStagingDir)
        include("${rootProject.name}/**")
        exclude("files-to-sign.txt")

        archiveBaseName.convention(intellijPlatform.projectName)
        archiveClassifier.set(providers.gradleProperty("outputPluginFileSuffix").orElse("from-staging"))
        destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    }

	// ==============================================================

    // buildPlugin keeps its default Zip behavior (sources from prepareSandbox + jarSearchableOptions).
    // We add dependsOn(preparePluginInternalsForSigning) to ensure the staging directory is populated,
    // then verify the archive matches the staging directory.
    buildPlugin {
        // Ensure that the staging directory is populated
        dependsOn(preparePluginInternalsForSigning)

        val pluginStagingContentDir = pluginStagingContentDir
        val projectName = rootProject.name

        doLast {
            // Verify the archive matches the staging directory to be sure that 
            // buildPlugin and preparePluginInternalsForSigning+assemblePlugin produces the same results
            val zipFiles = ZipFile(archiveFile.get().asFile).use {
                it.entries().asSequence().filterNot { e -> e.isDirectory }.map { e -> e.name }.sorted().toList()
            }
            val stagingFiles = pluginStagingContentDir.walkTopDown().filter { it.isFile }
                .map { "${projectName}/${it.relativeTo(pluginStagingContentDir).path.replace('\\', '/')}" }
                .sorted().toList()

            check(zipFiles == stagingFiles) {
                "Plugin archive and staging directory are out of sync!\n" +
                "  Only in archive: ${zipFiles - stagingFiles.toSet()}\n" +
                "  Only in staging: ${stagingFiles - zipFiles.toSet()}"
            }
        }
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
//        channels.(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}
