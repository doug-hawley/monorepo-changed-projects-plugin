#!/bin/bash
# Conventional commit validation helper
# Usage: ./validate-commit.sh "your commit message"

set -e

COMMIT_MSG="${1:-}"

if [ -z "$COMMIT_MSG" ]; then
    echo "Usage: $0 \"commit message\""
    echo ""
    echo "Examples:"
    echo "  $0 \"feat: add new feature\""
    echo "  $0 \"fix: resolve bug\""
    echo "  $0 \"docs: update README\""
    exit 1
fi

# Conventional commit pattern
PATTERN="^(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\(.+\))?!?: .+"

if echo "$COMMIT_MSG" | grep -qE "$PATTERN"; then
    echo "✅ Valid conventional commit message"
    echo ""
    echo "Message: $COMMIT_MSG"
    echo ""

    # Extract type
    TYPE=$(echo "$COMMIT_MSG" | sed -E 's/^([a-z]+).*/\1/')

    # Check for breaking change
    if echo "$COMMIT_MSG" | grep -q "!:"; then
        echo "⚠️  Breaking change detected (MAJOR version bump)"
    elif [ "$TYPE" = "feat" ]; then
        echo "📦 Feature addition (MINOR version bump)"
    elif [ "$TYPE" = "fix" ]; then
        echo "🐛 Bug fix (PATCH version bump)"
    else
        echo "📝 Other change (no version bump by default)"
    fi

    exit 0
else
    echo "❌ Invalid conventional commit message"
    echo ""
    echo "Expected format: <type>[optional scope]: <description>"
    echo ""
    echo "Valid types:"
    echo "  feat     - New feature (MINOR bump)"
    echo "  fix      - Bug fix (PATCH bump)"
    echo "  docs     - Documentation only"
    echo "  style    - Code style (formatting, etc.)"
    echo "  refactor - Code refactoring"
    echo "  perf     - Performance improvement"
    echo "  test     - Adding/updating tests"
    echo "  build    - Build system changes"
    echo "  ci       - CI configuration changes"
    echo "  chore    - Other changes"
    echo "  revert   - Revert a previous commit"
    echo ""
    echo "Examples:"
    echo "  feat: add support for custom git branches"
    echo "  fix: resolve Windows path issue"
    echo "  feat(git)!: breaking API change"
    echo "  docs: update README with examples"
    echo ""
    exit 1
fi
