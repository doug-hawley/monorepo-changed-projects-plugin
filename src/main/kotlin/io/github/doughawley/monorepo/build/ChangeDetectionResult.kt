package io.github.doughawley.monorepo.build

import io.github.doughawley.monorepo.build.domain.MonorepoProjects

/**
 * Result of change detection for a given baseline ref.
 */
data class ChangeDetectionResult(
    val monorepoProjects: MonorepoProjects,
    val allAffectedProjects: Set<String>
)
