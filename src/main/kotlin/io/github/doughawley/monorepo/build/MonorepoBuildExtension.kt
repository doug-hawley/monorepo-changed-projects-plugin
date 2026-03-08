package io.github.doughawley.monorepo.build

import io.github.doughawley.monorepo.build.domain.MonorepoProjects
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Extension for configuring the monorepo-build plugin.
 */
open class MonorepoBuildExtension {
    /**
     * The tag name used by the CI task (`buildChangedProjectsAndCreateReleaseBranches`)
     * for incremental change detection. The CI task compares HEAD against this tag,
     * falling back to `origin/<primaryBranch>` when the tag doesn't exist.
     *
     * Dev tasks (`printChangedProjects`, `buildChangedProjects`) always use
     * `origin/<primaryBranch>` regardless of this tag.
     */
    var lastSuccessfulBuildTag: String = "monorepo/last-successful-build"

    /**
     * Whether to include untracked files in the change detection
     */
    var includeUntracked: Boolean = true

    /**
     * File patterns to exclude from change detection
     */
    var excludePatterns: List<String> = listOf()

    /**
     * The ref used for dev-facing change detection (`origin/<primaryBranch>`).
     * Set internally after ref resolution; available for inspection by tasks and build scripts.
     */
    var resolvedBaseRef: String = ""
        internal set

    /**
     * All monorepo projects with their metadata and change information,
     * computed against the dev baseline (`origin/<primaryBranch>`).
     * Available after configuration phase completes.
     */
    var monorepoProjects: MonorepoProjects = MonorepoProjects(emptyList())
        internal set

    /**
     * Set of all affected project paths (including those affected by dependency changes),
     * computed against the dev baseline (`origin/<primaryBranch>`).
     * Available after configuration phase completes.
     */
    var allAffectedProjects: Set<String> = emptySet()
        internal set

    /**
     * The ref used for CI change detection (last-successful-build tag or fallback).
     * Set internally; available for inspection by tasks and build scripts.
     */
    var ciResolvedBaseRef: String = ""
        internal set

    /**
     * All monorepo projects with their metadata and change information,
     * computed against the CI baseline (last-successful-build tag or fallback).
     * Available after configuration phase completes.
     */
    var ciMonorepoProjects: MonorepoProjects = MonorepoProjects(emptyList())
        internal set

    /**
     * Set of all affected project paths computed against the CI baseline.
     * Used by `buildChangedProjectsAndCreateReleaseBranches`.
     * Available after configuration phase completes.
     */
    var ciAllAffectedProjects: Set<String> = emptySet()
        internal set

    /**
     * Guards against concurrent metadata computation under --parallel builds.
     * The first thread to win compareAndSet(false, true) performs the computation;
     * all others see true and skip it.
     */
    internal val computationGuard = AtomicBoolean(false)

    /**
     * Flag indicating whether metadata has been computed in the configuration phase.
     * Marked volatile to ensure the write is visible to all threads once set.
     * Used to ensure metadata is available before task execution.
     */
    @Volatile
    internal var metadataComputed: Boolean = false
}
