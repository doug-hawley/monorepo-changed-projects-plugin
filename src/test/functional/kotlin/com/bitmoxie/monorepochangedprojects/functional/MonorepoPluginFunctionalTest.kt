package com.bitmoxie.monorepochangedprojects.functional

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Functional tests for the Monorepo Changed Projects plugin.
 * These tests create real Gradle projects, make git changes, and verify the plugin detects them correctly.
 */
class MonorepoPluginFunctionalTest : FunSpec({
    lateinit var testProjectDir: File

    beforeEach {
        testProjectDir = kotlin.io.path.createTempDirectory("monorepo-plugin-test").toFile()
    }

    afterEach {
        testProjectDir.deleteRecursively()
    }

    test("plugin detects single changed library and all dependent projects") {
        // Setup: Create a project structure with dependencies
        val project = TestProjectBuilder(testProjectDir)
            .withSubproject("common-lib")
            .withSubproject("service", dependsOn = listOf("common-lib"))
            .withSubproject("app", dependsOn = listOf("service"))
            .applyPlugin()
            .withRemote()
            .build()

        project.initGit()
        project.commitAll("Initial commit")
        project.pushToRemote()

        // Make changes to common-lib
        project.appendToFile("common-lib/src/main/kotlin/com/example/Common-lib.kt", "\n// Added comment")

        // Execute
        val result = project.runTask("detectChangedProjects")

        // Assert
        result.task(":detectChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS

        val changedProjects = result.extractChangedProjects()
        changedProjects shouldHaveSize 3
        changedProjects shouldContainAll setOf(":common-lib", ":service", ":app")

        val directlyChanged = result.extractDirectlyChangedProjects()
        directlyChanged shouldHaveSize 1
        directlyChanged shouldContainAll setOf(":common-lib")
    }

    test("plugin detects changed service but not its dependencies") {
        // Setup
        val project = TestProjectBuilder(testProjectDir)
            .withSubproject("common-lib")
            .withSubproject("service", dependsOn = listOf("common-lib"))
            .withSubproject("app", dependsOn = listOf("service"))
            .applyPlugin()
            .withRemote()
            .build()

        project.initGit()
        project.commitAll("Initial commit")
        project.pushToRemote()

        // Make changes only to service
        project.appendToFile("service/src/main/kotlin/com/example/Service.kt", "\n// Modified service")

        // Execute
        val result = project.runTask("detectChangedProjects")

        // Assert
        result.task(":detectChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS

        val changedProjects = result.extractChangedProjects()
        changedProjects shouldHaveSize 2
        changedProjects shouldContainAll setOf(":service", ":app")
    }

    test("plugin detects only leaf project when changed") {
        // Setup
        val project = TestProjectBuilder(testProjectDir)
            .withSubproject("common-lib")
            .withSubproject("service", dependsOn = listOf("common-lib"))
            .withSubproject("app", dependsOn = listOf("service"))
            .applyPlugin()
            .withRemote()
            .build()

        project.initGit()
        project.commitAll("Initial commit")
        project.pushToRemote()

        // Make changes only to app (leaf project)
        project.appendToFile("app/src/main/kotlin/com/example/App.kt", "\n// Modified app")

        // Execute
        val result = project.runTask("detectChangedProjects")

        // Assert
        result.task(":detectChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS

        val changedProjects = result.extractChangedProjects()
        changedProjects shouldHaveSize 1
        changedProjects shouldContainAll setOf(":app")
    }

    test("plugin detects no changes when nothing modified") {
        // Setup
        val project = TestProjectBuilder(testProjectDir)
            .withSubproject("common-lib")
            .withSubproject("service", dependsOn = listOf("common-lib"))
            .applyPlugin()
            .withRemote()
            .build()

        project.initGit()
        project.commitAll("Initial commit")
        project.pushToRemote()

        // Don't make any changes

        // Execute
        val result = project.runTask("detectChangedProjects")

        // Assert
        result.task(":detectChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS

        val changedFilesCount = result.extractChangedFilesCount()
        changedFilesCount shouldBe 0

        result.output shouldContain "No projects have changed"
    }

    test("plugin detects multiple independent changes") {
        // Setup
        val project = TestProjectBuilder(testProjectDir)
            .withSubproject("common-lib")
            .withSubproject("service", dependsOn = listOf("common-lib"))
            .withSubproject("standalone")
            .applyPlugin()
            .withRemote()
            .build()

        project.initGit()
        project.commitAll("Initial commit")
        project.pushToRemote()

        // Make changes to common-lib and standalone (independent projects)
        project.appendToFile("common-lib/src/main/kotlin/com/example/Common-lib.kt", "\n// Modified common")
        project.appendToFile("standalone/src/main/kotlin/com/example/Standalone.kt", "\n// Modified standalone")

        // Execute
        val result = project.runTask("detectChangedProjects")

        // Assert
        result.task(":detectChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS

        val changedProjects = result.extractChangedProjects()
        changedProjects shouldHaveSize 3
        changedProjects shouldContainAll setOf(":common-lib", ":service", ":standalone")
    }

    test("plugin detects untracked files when includeUntracked is true") {
        // Setup
        val project = TestProjectBuilder(testProjectDir)
            .withSubproject("common-lib")
            .withSubproject("service", dependsOn = listOf("common-lib"))
            .applyPlugin()
            .withRemote()
            .build()

        project.initGit()
        project.commitAll("Initial commit")
        project.pushToRemote()

        // Create a new untracked file
        project.createNewFile("common-lib/src/main/kotlin/com/example/NewFile.kt",
            """
            package com.example

            class NewFile
            """.trimIndent()
        )

        // Execute
        val result = project.runTask("detectChangedProjects")

        // Assert
        result.task(":detectChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS

        val changedProjects = result.extractChangedProjects()
        changedProjects shouldContainAll setOf(":common-lib", ":service")
    }

    test("plugin detects staged but uncommitted changes") {
        // Setup
        val project = TestProjectBuilder(testProjectDir)
            .withSubproject("common-lib")
            .withSubproject("service", dependsOn = listOf("common-lib"))
            .applyPlugin()
            .withRemote()
            .build()

        project.initGit()
        project.commitAll("Initial commit")
        project.pushToRemote()

        // Make and stage changes
        project.appendToFile("common-lib/src/main/kotlin/com/example/Common-lib.kt", "\n// Staged change")
        project.stageFile("common-lib/src/main/kotlin/com/example/Common-lib.kt")

        // Execute
        val result = project.runTask("detectChangedProjects")

        // Assert
        result.task(":detectChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS

        val changedProjects = result.extractChangedProjects()
        changedProjects shouldContainAll setOf(":common-lib", ":service")
    }

    test("plugin works with build.gradle.kts changes") {
        // Setup
        val project = TestProjectBuilder(testProjectDir)
            .withSubproject("common-lib")
            .withSubproject("service", dependsOn = listOf("common-lib"))
            .applyPlugin()
            .withRemote()
            .build()

        project.initGit()
        project.commitAll("Initial commit")
        project.pushToRemote()

        // Modify build file
        project.appendToFile("common-lib/build.gradle.kts", "\n// Build config change")

        // Execute
        val result = project.runTask("detectChangedProjects")

        // Assert
        result.task(":detectChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS

        val changedProjects = result.extractChangedProjects()
        changedProjects shouldContainAll setOf(":common-lib", ":service")
    }
})
