# Functional Tests Quick Reference

## Running Tests

```bash
# Run all tests (unit + functional)
./gradlew check

# Run only unit tests
./gradlew unitTest

# Run only functional tests
./gradlew functionalTest

# Run specific functional test class
./gradlew functionalTest --tests "MonorepoPluginFunctionalTest"

# Run specific test by name pattern
./gradlew functionalTest --tests "*library change*"
```

## Test Structure

### MonorepoPluginFunctionalTest
Tests the `detectChangedProjects` task:
- Library changes affecting dependents
- Service changes (mid-chain)
- Leaf project changes
- No changes scenario
- Multiple independent changes
- Untracked files
- Staged changes
- Build file changes

### BuildChangedProjectsFunctionalTest
Tests the `buildChangedProjects` task:
- Building only affected projects
- No changes handling
- Multiple independent builds
- Task dependencies
- Leaf project builds

## Adding New Functional Tests

1. Create test file in `src/test/functional/kotlin/com/bitmoxie/monorepochangedprojects/functional/`
2. Use `TestProjectBuilder` to create test projects
3. Use Kotest's `FunSpec` style
4. Follow the pattern: Setup → Execute → Assert

### Example:
```kotlin
class MyFunctionalTest : FunSpec({
    lateinit var testProjectDir: File

    beforeEach {
        testProjectDir = kotlin.io.path.createTempDirectory("my-test").toFile()
    }

    afterEach {
        testProjectDir.deleteRecursively()
    }

    test("my scenario") {
        // Setup
        val project = TestProjectBuilder(testProjectDir)
            .withSubproject("lib-a")
            .withSubproject("app", dependsOn = listOf("lib-a"))
            .applyPlugin()
            .build()

        project.initGit()
        project.commitAll("Initial commit")
        project.appendToFile("lib-a/src/main/kotlin/com/example/Lib-a.kt", "\n// change")

        // Execute
        val result = project.runTask("detectChangedProjects")

        // Assert
        result.task(":detectChangedProjects")?.outcome shouldBe TaskOutcome.SUCCESS
        val changed = result.extractChangedProjects()
        changed shouldContainAll setOf(":lib-a", ":app")
    }
})
```

## TestProjectBuilder API

### Project Setup
```kotlin
TestProjectBuilder(projectDir)
    .withSubproject("name")
    .withSubproject("name", dependsOn = listOf("dep1", "dep2"))
    .applyPlugin()
    .build()
```

### Git Operations
```kotlin
project.initGit()                         // Initialize git repo
project.commitAll("message")              // Commit all changes
project.stageFile("path/to/file")         // Stage specific file
project.appendToFile("path", "content")   // Append to existing file
project.createNewFile("path", "content")  // Create new file
project.modifyFile("path", "content")     // Replace file content
```

### Running Tasks
```kotlin
val result = project.runTask("taskName")           // Run and expect success
val result = project.runTaskAndFail("taskName")    // Run and expect failure
```

### Assertions
```kotlin
// Task outcome
result.task(":taskName")?.outcome shouldBe TaskOutcome.SUCCESS

// Extract data from output
val changed = result.extractChangedProjects()
val direct = result.extractDirectlyChangedProjects()
val count = result.extractChangedFilesCount()

// String matching
result.output shouldContain "expected text"
result.output shouldNotContain "unexpected text"

// Collections
changed shouldHaveSize 3
changed shouldContainAll setOf(":lib", ":app")
```

## Tips

1. **Cleanup**: Always use `beforeEach`/`afterEach` to create/cleanup temp directories
2. **Git Required**: Tests require git to be installed and available
3. **Temp Directories**: Use `kotlin.io.path.createTempDirectory()` not deprecated `createTempDir()`
4. **Isolation**: Each test gets its own temp directory - no shared state
5. **Output**: Use `result.output` to debug - it contains full Gradle output
6. **Stack Traces**: Tests run with `--stacktrace` for easier debugging

## Common Patterns

### Test Project with Dependencies
```kotlin
val project = TestProjectBuilder(testProjectDir)
    .withSubproject("common")
    .withSubproject("service", dependsOn = listOf("common"))
    .withSubproject("app", dependsOn = listOf("service"))
    .applyPlugin()
    .build()
```

### Make Change and Verify
```kotlin
project.initGit()
project.commitAll("Initial")
project.appendToFile("common/src/main/kotlin/com/example/Common.kt", "\n// change")

val result = project.runTask("detectChangedProjects")
result.extractChangedProjects() shouldContainAll setOf(":common", ":service", ":app")
```

### Test No Changes
```kotlin
project.initGit()
project.commitAll("Initial")
// Don't make any changes

val result = project.runTask("detectChangedProjects")
result.extractChangedFilesCount() shouldBe 0
```
