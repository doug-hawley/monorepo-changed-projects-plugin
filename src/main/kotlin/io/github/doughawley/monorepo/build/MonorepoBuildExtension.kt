package io.github.doughawley.monorepo.build

import io.github.doughawley.monorepo.build.domain.MonorepoProjects
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Extension for configuring the monorepo-build plugin.
 */
open class MonorepoBuildExtension {
    /**
     * The tag name that the plugin reads from and writes to for tracking the
     * last successful build. Change detection compares HEAD against this tag.
     *
     * During baseline resolution the plugin fetches this tag from origin
     * (best-effort) to ensure the local copy is current, then checks whether
     * it exists. If it does not, the plugin falls back to
     * `origin/{primaryBranch}`. When neither ref is available, all projects
     * are treated as changed.
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
     * The ref that was actually used for change detection, or null when no baseline exists
     * (all projects treated as changed). Set internally after ref resolution.
     *
     * This may be the [lastSuccessfulBuildTag], `origin/{primaryBranch}` (fallback),
     * or null when neither ref is available.
     */
    var resolvedBaseRef: String? = null
        internal set

    /**
     * All monorepo projects with their metadata and change information.
     * Available after configuration phase completes.
     */
    var monorepoProjects: MonorepoProjects = MonorepoProjects(emptyList())
        internal set

    /**
     * Set of all affected project paths (including those affected by dependency changes).
     * Available after configuration phase completes.
     */
    var allAffectedProjects: Set<String> = emptySet()
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
