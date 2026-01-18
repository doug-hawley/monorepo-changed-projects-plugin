package com.bitmoxie.monorepochangedprojects.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ProjectMetadataTest : FunSpec({

    test("hasChanges returns true when project has changed files") {
        // given
        val metadata = ProjectMetadata(
            name = "test-project",
            fullyQualifiedName = ":test-project",
            changedFiles = listOf("src/main/File.kt", "build.gradle.kts")
        )

        // then
        metadata.hasChanges() shouldBe true
    }

    test("hasChanges returns false when project has no changed files") {
        // given
        val metadata = ProjectMetadata(
            name = "test-project",
            fullyQualifiedName = ":test-project",
            changedFiles = emptyList()
        )

        // then
        metadata.hasChanges() shouldBe false
    }

    test("hasDependency returns true when project depends on given project") {
        // given
        val metadata = ProjectMetadata(
            name = "test-project",
            fullyQualifiedName = ":test-project",
            dependencyNames = listOf(":common-lib", ":utils")
        )

        // then
        metadata.hasDependency(":common-lib") shouldBe true
        metadata.hasDependency(":utils") shouldBe true
    }

    test("hasDependency returns false when project does not depend on given project") {
        // given
        val metadata = ProjectMetadata(
            name = "test-project",
            fullyQualifiedName = ":test-project",
            dependencyNames = listOf(":common-lib")
        )

        // then
        metadata.hasDependency(":other-lib") shouldBe false
    }

    test("toString includes dependency count and file count") {
        // given
        val metadata = ProjectMetadata(
            name = "test-project",
            fullyQualifiedName = ":test-project",
            dependencyNames = listOf(":dep1", ":dep2"),
            changedFiles = listOf("file1.kt", "file2.kt", "file3.kt")
        )

        // when
        val result = metadata.toString()

        // then
        result shouldBe "ProjectMetadata(name='test-project', fullyQualifiedName=':test-project', dependencies=2, changedFiles=3 files)"
    }
})
