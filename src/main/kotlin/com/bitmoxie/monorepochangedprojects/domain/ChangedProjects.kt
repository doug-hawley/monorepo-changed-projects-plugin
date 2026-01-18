package com.bitmoxie.monorepochangedprojects.domain

/**
 * Represents a collection of changed projects with metadata.
 * Provides convenient methods to query and analyze changed projects.
 */
data class ChangedProjects(
    val projects: List<ProjectMetadata>
) {
    /**
     * Returns a list of project names that are affected by changes.
     * This includes projects with direct changes AND projects whose dependencies have changed.
     */
    fun getChangedProjects(): List<String> {
        return getAffectedProjects().map { it.name }
    }

    /**
     * Returns a list of fully qualified project paths that are affected by changes.
     * This includes projects with direct changes AND projects whose dependencies have changed.
     */
    fun getChangedProjectPaths(): List<String> {
        return getAffectedProjects().map { it.fullyQualifiedName }
    }

    /**
     * Returns the count of projects affected by changes (including dependency changes).
     */
    fun getChangedProjectCount(): Int {
        return getAffectedProjects().size
    }

    /**
     * Returns all projects affected by changes (either direct changes or dependency changes).
     */
    fun getAffectedProjects(): List<ProjectMetadata> {
        val projectsWithDirectChanges = projects.filter { it.hasChanges() }
        val affected = mutableSetOf<ProjectMetadata>()

        // Add all projects with direct changes
        affected.addAll(projectsWithDirectChanges)

        // Add all projects that depend on changed projects
        projectsWithDirectChanges.forEach { changedProject ->
            val dependents = getProjectsDependingOn(changedProject.fullyQualifiedName)
            affected.addAll(dependents)
        }

        return affected.toList()
    }

    /**
     * Returns all projects, including those without changes.
     */
    fun getAllProjects(): List<String> {
        return projects.map { it.name }
    }

    /**
     * Returns all project paths, including those without changes.
     */
    fun getAllProjectPaths(): List<String> {
        return projects.map { it.fullyQualifiedName }
    }

    /**
     * Finds a project by its name.
     */
    fun findProjectByName(name: String): ProjectMetadata? {
        return projects.firstOrNull { it.name == name || it.fullyQualifiedName == name }
    }

    /**
     * Returns projects that have direct changes (not just dependency changes).
     */
    fun getProjectsWithDirectChanges(): List<ProjectMetadata> {
        return projects.filter { it.hasChanges() }
    }

    /**
     * Returns affected projects (including dependency changes) whose fully qualified name starts with the given prefix.
     * Useful for filtering projects by directory structure (e.g., ":apps", ":libs").
     *
     * @param prefix The prefix to match against fully qualified project names
     * @return List of affected projects matching the prefix
     */
    fun getChangedProjectsWithPrefix(prefix: String): List<ProjectMetadata> {
        return getAffectedProjects().filter { project ->
            project.fullyQualifiedName.startsWith(prefix)
        }
    }

    /**
     * Returns affected project names (not paths) for projects with the given prefix.
     * Includes projects with direct changes and those affected by dependency changes.
     *
     * @param prefix The prefix to match against fully qualified project names
     * @return List of project names (simple names) matching the prefix
     */
    fun getChangedProjectNamesWithPrefix(prefix: String): List<String> {
        return getChangedProjectsWithPrefix(prefix).map { it.name }
    }

    /**
     * Returns affected project paths for projects with the given prefix.
     * Includes projects with direct changes and those affected by dependency changes.
     *
     * @param prefix The prefix to match against fully qualified project names
     * @return List of fully qualified paths matching the prefix
     */
    fun getChangedProjectPathsWithPrefix(prefix: String): List<String> {
        return getChangedProjectsWithPrefix(prefix).map { it.fullyQualifiedName }
    }

    /**
     * Returns a map of project names to their changed file counts.
     */
    fun getChangedFileCountByProject(): Map<String, Int> {
        return projects
            .filter { it.hasChanges() }
            .associate { it.fullyQualifiedName to it.changedFiles.size }
    }

    /**
     * Returns all changed files across all projects.
     */
    fun getAllChangedFiles(): Set<String> {
        return projects.flatMap { it.changedFiles }.toSet()
    }

    /**
     * Returns total count of changed files across all projects.
     */
    fun getTotalChangedFilesCount(): Int {
        return projects.sumOf { it.changedFiles.size }
    }

    /**
     * Returns true if any project has changes.
     */
    fun hasAnyChanges(): Boolean {
        return projects.any { it.hasChanges() }
    }

    /**
     * Returns projects that depend on the given project (directly or transitively).
     */
    fun getProjectsDependingOn(projectName: String): List<ProjectMetadata> {
        // Build a map for quick lookups
        val projectMap = projects.associateBy { it.fullyQualifiedName }
        val altMap = projects.associateBy { it.name }

        // Find all projects that depend on the given project (transitively)
        val dependents = mutableListOf<ProjectMetadata>()

        projects.forEach { project ->
            if (hasDependencyTransitively(project, projectName, projectMap, altMap)) {
                dependents.add(project)
            }
        }

        return dependents
    }

    /**
     * Checks if a project depends on the given project transitively.
     */
    private fun hasDependencyTransitively(
        project: ProjectMetadata,
        targetName: String,
        projectMap: Map<String, ProjectMetadata>,
        altMap: Map<String, ProjectMetadata>,
        visited: MutableSet<String> = mutableSetOf()
    ): Boolean {
        // Avoid circular dependencies
        if (visited.contains(project.fullyQualifiedName)) {
            return false
        }
        visited.add(project.fullyQualifiedName)

        // Check direct dependencies
        if (project.hasDependency(targetName)) {
            return true
        }

        // Check transitive dependencies
        for (depName in project.dependencyNames) {
            val depProject = projectMap[depName] ?: altMap[depName]
            if (depProject != null && hasDependencyTransitively(depProject, targetName, projectMap, altMap, visited)) {
                return true
            }
        }

        return false
    }

    /**
     * Returns a summary of changes.
     */
    fun getSummary(): ChangeSummary {
        val projectsWithDirectChanges = getProjectsWithDirectChanges()
        val affectedProjects = getAffectedProjects()
        return ChangeSummary(
            totalProjects = projects.size,
            changedProjects = projectsWithDirectChanges.size,
            affectedProjects = affectedProjects.size,
            totalChangedFiles = getTotalChangedFilesCount(),
            projectNames = projectsWithDirectChanges.map { it.fullyQualifiedName },
            affectedProjectNames = affectedProjects.map { it.fullyQualifiedName }
        )
    }

    override fun toString(): String {
        return "ChangedProjects(total=${projects.size}, changed=${getChangedProjectCount()}, files=${getTotalChangedFilesCount()})"
    }

    /**
     * Summary of changes across all projects.
     */
    data class ChangeSummary(
        val totalProjects: Int,
        val changedProjects: Int,
        val affectedProjects: Int,
        val totalChangedFiles: Int,
        val projectNames: List<String>,
        val affectedProjectNames: List<String>
    ) {
        override fun toString(): String {
            return """
                |Change Summary:
                |  Total Projects: $totalProjects
                |  Changed Projects (direct): $changedProjects
                |  Affected Projects (including dependents): $affectedProjects
                |  Total Changed Files: $totalChangedFiles
                |  Direct Changes: ${projectNames.joinToString(", ")}
                |  All Affected: ${affectedProjectNames.joinToString(", ")}
            """.trimMargin()
        }
    }
}
