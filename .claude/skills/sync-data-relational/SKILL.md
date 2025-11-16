---
name: sync-data-relational
description: Analyze spring-data-relational version changes and identify required updates for the current project. Use when upgrading spring-data-relational versions or syncing with upstream changes.
allowed-tools: [WebFetch, Grep, Read, Glob, Bash, TodoWrite]
---

# Sync Spring Data Relational Changes

This skill helps synchronize spring-data-relational version updates to the spring-jdbc-plus project.

## When to Use

- When a new spring-data-relational version is released
- When asked to "sync data-relational changes"
- When asked to "update to spring-data-relational X.X.X"
- When analyzing what needs to be updated for a version upgrade

## What This Skill Does

1. **Fetches Version Diff**: Retrieves changes between two spring-data-relational versions from GitHub
2. **Analyzes Changes**: Categorizes changes into:
   - API changes (new methods, deprecated methods, signature changes)
   - Dependency upgrades
   - Bug fixes
   - Documentation updates
3. **Identifies Impact**: Searches the current codebase for affected code
4. **Provides Action Items**: Lists specific files and changes needed

## Instructions

### Step 1: Determine Versions

1. Check `gradle.properties` to find current `springDataBomVersion`
2. Determine the spring-data-relational version from the BOM version
   - Spring Data BOM 2025.0.5 → spring-data-relational 3.5.5
   - Spring Data BOM 2025.0.6 → spring-data-relational 3.5.6
3. Ask user for target version if not specified

### Step 2: Fetch Changes from GitHub

Use WebFetch to get the comparison from:
`https://github.com/spring-projects/spring-data-relational/compare/{old-version}...{new-version}`

Example: `https://github.com/spring-projects/spring-data-relational/compare/3.5.5...3.5.6`

### Step 3: Analyze Changes

Categorize the changes:

**Critical Changes** (High Priority):
- API deprecations
- Breaking changes
- Signature changes in methods we override/use

**Important Changes** (Medium Priority):
- New features we might want to adopt
- Bug fixes that affect our code
- Dependency upgrades

**Informational** (Low Priority):
- Documentation updates
- Internal refactoring
- Minor bug fixes

### Step 4: Search Current Codebase

For each critical/important change, search the codebase:

1. Use Grep to find usages of:
   - Deprecated classes/methods
   - Changed API signatures
   - Affected dependencies

2. Focus on these directories:
   - `spring-data-jdbc-plus-support/`
   - `spring-data-jdbc-plus-sql/`
   - `spring-data-jdbc-plus-repository/`
   - `spring-boot-autoconfigure-data-jdbc-plus/`

### Step 5: Generate Report

Create a structured report with:

```markdown
## Spring Data Relational {old} → {new} Upgrade Analysis

### Summary
- Total commits: X
- Files changed: Y
- Critical changes: Z

### Critical Changes Requiring Action

#### 1. [Change Category - e.g., API Deprecation]
**Upstream Change**: Description of what changed in spring-data-relational
**Impact**: Files in our codebase that are affected
**Action Required**: What needs to be done

[List specific files with line numbers if found]

### Important Changes to Review

[Same format as above]

### Dependency Updates

[List dependency version changes]

### Low Priority / Informational

[Brief list of other changes]

### Next Steps

1. [ ] Review critical changes
2. [ ] Update affected code
3. [ ] Update gradle.properties if needed
4. [ ] Run tests
5. [ ] Update documentation
```

### Step 6: Provide Actionable Next Steps

- Create TODO items for each required change
- Suggest specific code modifications
- Highlight test cases that should be added/updated

## Examples

**User**: "Sync data-relational changes from 3.5.5 to 3.5.6"
**Skill**:
1. Fetches https://github.com/spring-projects/spring-data-relational/compare/3.5.5...3.5.6
2. Identifies: API deprecations, dependency upgrades
3. Searches codebase for affected code
4. Generates report with action items

**User**: "What needs to be updated for spring-data-relational 3.5.6?"
**Skill**:
1. Checks current version in gradle.properties
2. Compares with 3.5.6
3. Provides analysis and action items

## Important Notes

- Always use TodoWrite to track the analysis tasks
- Focus on changes that affect the core modules (support, sql, repository)
- Consider backward compatibility when suggesting changes
- Highlight security-related changes with high priority
- Note any changes to transitive dependencies that might affect our users

## Related Files

- `gradle.properties` - Contains springDataBomVersion
- `buildSrc/src/main/groovy/spring.jdbc.plus.spring-bom-conventions.gradle` - BOM configuration
- `spring-data-jdbc-plus-support/` - Core support module
- `spring-data-jdbc-plus-sql/` - SQL module
- `spring-data-jdbc-plus-repository/` - Repository module
