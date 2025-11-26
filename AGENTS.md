# AGENTS

## sync-data-relational
- Skill doc: @.claude/skills/sync-data-relational/SKILL.md
- Role: Analyze spring-data-relational version changes and outline any required actions for this project.
- When to use: Whenever a spring-data-relational upgrade/sync is requested or we need to assess related impact.
- Procedure:
  1. Check BOM version (`gradle.properties`) and confirm target Spring Boot/Data alignment.
  2. Fetch GitHub compare diff via `curl -L https://github.com/spring-projects/spring-data-relational/compare/{old}...{new}.diff` (Agent Mode + network perm).
  3. Parse diff to list critical API changes (e.g., deprecations in `JdbcAggregateOperations`, repository adjustments) and dependency upgrades.
  4. Search local code for affected symbols (e.g., deprecated template APIs, specific driver versions) and note required actions.
  5. Draft report + TODOs covering impact, doc updates, and test guidance.
