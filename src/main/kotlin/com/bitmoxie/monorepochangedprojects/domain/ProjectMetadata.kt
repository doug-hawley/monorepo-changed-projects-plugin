package com.bitmoxie.monorepochangedprojects.domain

data class ProjectMetadata(
    val name: String,
    val fullyQualifiedName: String,
    val dependencyNames: List<String> = emptyList(),
    val changedFiles: List<String> = emptyList()
) {
    /**
     * Returns true if this project depends on the given project name.
     */
    fun hasDependency(projectName: String): Boolean {
        return dependencyNames.contains(projectName)
    }

    /**
     * Returns true if this project has any changed files.
     */
    fun hasChanges(): Boolean = changedFiles.isNotEmpty()

    override fun toString(): String {
        return "ProjectMetadata(name='$name', fullyQualifiedName='$fullyQualifiedName', dependencies=${dependencyNames.size}, changedFiles=${changedFiles.size} files)"
    }
}
