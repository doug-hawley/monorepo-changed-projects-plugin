# Changelog

## [2.0.0](https://github.com/doug-hawley/monorepo-gradle-plugins/compare/v1.1.0...v2.0.0) (2026-02-22)


### ⚠ BREAKING CHANGES

* task names changed from printChangedProjects / buildChangedProjects to printChangedProjectsFromBranch / buildChangedProjectsFromBranch
* plugin ID, DSL block name, extension class, and Kotlin package have all changed

### Code Refactoring

* rename tasks to printChangedProjectsFromBranch and buildChangedProjectsFromBranch ([b02be5e](https://github.com/doug-hawley/monorepo-gradle-plugins/commit/b02be5edef142ee430c9445073f90381d83512ff))
* restructure as monorepo-build-plugin subproject within monorepo-gradle-plugins ([ffc4a38](https://github.com/doug-hawley/monorepo-gradle-plugins/commit/ffc4a385605c56969418610135bc30a303a84b1f))
