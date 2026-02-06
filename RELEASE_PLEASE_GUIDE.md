# Release Please Guide

This project uses [Release Please](https://github.com/googleapis/release-please) to automate releases and changelog management based on [Conventional Commits](https://www.conventionalcommits.org/).

## How It Works

1. **Commit with conventional commit messages** to your main branch
2. **Release Please automatically creates/updates a release PR** with:
   - Updated CHANGELOG.md
   - Version bumps in build.gradle.kts and gradle.properties
   - GitHub release notes
3. **Merge the release PR** to trigger:
   - GitHub release creation
   - Plugin publishing to Gradle Plugin Portal (if configured)

## Conventional Commit Format

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Commit Types

- **feat**: A new feature (bumps MINOR version)
- **fix**: A bug fix (bumps PATCH version)
- **docs**: Documentation only changes
- **style**: Code style changes (formatting, etc.)
- **refactor**: Code refactoring
- **perf**: Performance improvements
- **test**: Adding or updating tests
- **build**: Changes to build system or dependencies
- **ci**: Changes to CI configuration
- **chore**: Other changes that don't modify src or test files

### Breaking Changes

To trigger a MAJOR version bump, add `BREAKING CHANGE:` in the commit footer or use `!` after the type:

```
feat!: remove support for Java 11

BREAKING CHANGE: Java 11 is no longer supported. Minimum version is now Java 17.
```

## Examples

### Feature Addition
```bash
git commit -m "feat: add support for custom git branches"
```

### Bug Fix
```bash
git commit -m "fix: resolve Windows path separator issue in ProjectFileMapper"
```

### Breaking Change
```bash
git commit -m "feat!: migrate from JUnit to Kotest

BREAKING CHANGE: All tests now use Kotest framework. Projects depending on test utilities will need to update."
```

### Multiple Changes
```bash
git commit -m "chore: update dependencies

- Update Kotest from 5.8.0 to 5.9.1
- Update Gradle wrapper from 8.5 to 8.12"
```

## Release Workflow

### 1. Development
Make commits following conventional commit format:
```bash
git commit -m "feat: add new feature"
git push origin main
```

### 2. Release PR Created
Release Please automatically:
- Creates/updates a "chore(main): release X.Y.Z" PR
- Updates CHANGELOG.md with all changes since last release
- Bumps version in build.gradle.kts and gradle.properties

### 3. Review and Merge
Review the release PR and merge when ready:
- Check the changelog entries
- Verify version bump is correct
- Ensure CI passes

### 4. Release Published
After merging the release PR:
- GitHub release is automatically created
- Plugin is published to Gradle Plugin Portal (requires secrets)
- Release artifacts are attached

## Configuration Files

### `.github/workflows/release-please.yml`
GitHub Action workflow that runs Release Please

### `release-please-config.json`
Configuration for Release Please:
- Release type and versioning strategy
- Changelog sections
- Files to update with version numbers

### `.release-please-manifest.json`
Tracks the current version of the package

## Manual Version Override

To force a specific version bump, add to commit footer:

```bash
# Force minor bump
git commit -m "fix: minor bug fix

Release-As: 1.1.0"

# Force major bump
git commit -m "fix: breaking fix

Release-As: 2.0.0"
```

## Publishing Setup

To enable automatic publishing to Gradle Plugin Portal, add these secrets to your GitHub repository:

1. Go to repository Settings → Secrets and variables → Actions
2. Add secrets:
   - `GRADLE_PUBLISH_KEY`: Your Gradle Plugin Portal key
   - `GRADLE_PUBLISH_SECRET`: Your Gradle Plugin Portal secret

Get these credentials from: https://plugins.gradle.org/

## Troubleshooting

### Release Please PR Not Created
- Ensure commits follow conventional commit format
- Check GitHub Actions logs for errors
- Verify release-please.yml workflow has permissions

### Wrong Version Bump
- Use `Release-As:` footer to override
- Check commit types (feat vs fix vs chore)
- Remember: only feat and fix bump versions by default

### Publishing Fails
- Verify GRADLE_PUBLISH_KEY and GRADLE_PUBLISH_SECRET are set
- Check build.gradle.kts has publishing configuration
- Review GitHub Actions logs for detailed error

## Converting Existing CHANGELOG

Your current CHANGELOG.md is already in a compatible format. Release Please will:
- Preserve existing entries under ## [1.0.0] and earlier
- Add new entries under ## [Unreleased] → version when releasing
- Maintain the Keep a Changelog format

## Tips

1. **Write good commit messages**: They become your changelog
2. **Use scopes for clarity**: `feat(git): add staged file detection`
3. **Group related changes**: One feature = one commit when possible
4. **Breaking changes are serious**: Use `!` and `BREAKING CHANGE:` carefully
5. **Review release PRs**: They show exactly what's being released

## Resources

- [Release Please Documentation](https://github.com/googleapis/release-please)
- [Conventional Commits Specification](https://www.conventionalcommits.org/)
- [Keep a Changelog](https://keepachangelog.com/)
- [Semantic Versioning](https://semver.org/)
