package com.bitmoxie.monorepochangedprojects.functional

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Functional tests for the buildChangedProjects task.
 */
class BuildChangedProjectsFunctionalTest : FunSpec({
    lateinit var testProjectDir: File

    beforeEach {
        testProjectDir = kotlin.io.path.createTempDirectory("build-changed-test").toFile()
    }

    afterEach {
        testProjectDir.deleteRecursively()
    }

    test("buildChangedProjects task builds only affected projects") {
        // Setup
        val project = TestProjectBuilder(testProjectDir)
            .withSubproject("common-lib")
            .withSubproject("service", dependsOn = listOf("common-lib"))
            .withSubproject("app", dependsOn = listOf("service"))
            .withSubproject("standalone")
            .applyPlugin()
            .withRemote()
            .build()

        project.initGit()
        project.commitAll("Initial commit")
        project.pushToRemote()

        // Make changes only to common-lib
        project.appendToFile("common-lib/src/main/kotlin/com/example/Common-lib.kt", "\n// Modified")

        // Execute
        val result = project.runTask("buildChangedProjects")

        // Assert
        result.task(":buildChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS

        // Should build common-lib, service, and app (not standalone)
        result.output shouldContain "Building 3 changed project(s)"
        result.output shouldContain ":common-lib"
        result.output shouldContain ":service"
        result.output shouldContain ":app"
        result.output shouldNotContain ":standalone"
    }

    test("buildChangedProjects reports no changes when nothing modified") {
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
        val result = project.runTask("buildChangedProjects")

        // Assert
        result.task(":buildChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain "No projects have changed - nothing to build"
    }

    test("buildChangedProjects handles multiple independent changes") {
        // Setup
        val project = TestProjectBuilder(testProjectDir)
            .withSubproject("lib-a")
            .withSubproject("lib-b")
            .withSubproject("app-a", dependsOn = listOf("lib-a"))
            .withSubproject("app-b", dependsOn = listOf("lib-b"))
            .applyPlugin()
            .withRemote()
            .build()

        project.initGit()
        project.commitAll("Initial commit")
        project.pushToRemote()

        // Make changes to both lib-a and lib-b
        project.appendToFile("lib-a/src/main/kotlin/com/example/Lib-a.kt", "\n// Modified A")
        project.appendToFile("lib-b/src/main/kotlin/com/example/Lib-b.kt", "\n// Modified B")

        // Execute
        val result = project.runTask("buildChangedProjects")

        // Assert
        result.task(":buildChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain "Building 4 changed project(s)"
        result.output shouldContain ":lib-a"
        result.output shouldContain ":lib-b"
        result.output shouldContain ":app-a"
        result.output shouldContain ":app-b"
    }

    test("buildChangedProjects runs after detectChangedProjects") {
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

        project.appendToFile("common-lib/src/main/kotlin/com/example/Common-lib.kt", "\n// Changed")

        // Execute
        val result = project.runTask("buildChangedProjects")

        // Assert - both tasks should have run
        result.task(":detectChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.task(":buildChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    test("buildChangedProjects builds only leaf project when changed") {
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

        // Make changes only to app
        project.appendToFile("app/src/main/kotlin/com/example/App.kt", "\n// App changed")

        // Execute
        val result = project.runTask("buildChangedProjects")

        // Assert
        result.task(":buildChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS
        result.output shouldContain "Building 1 changed project(s)"
        result.output shouldContain ":app"
        result.output shouldNotContain ":common-lib"
        result.output shouldNotContain ":service"
    }
})
