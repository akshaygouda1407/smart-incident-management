# Diagrams

This folder contains editable diagram sources for the Smart Incident & Issue Management Platform.

## Files
- `usecase.puml` - UML use case diagram (PlantUML)
- `activity-issue-lifecycle.puml` - UML activity diagram for the issue lifecycle + SLA (PlantUML)
- `class-diagram.puml` - High-level class diagram (controllers/services/repos/entities) (PlantUML)
- `er-diagram.mmd` - ER diagram (Mermaid)

## How to render
- PlantUML: use a PlantUML plugin (IntelliJ/VS Code) or a PlantUML server/CLI.
- Mermaid: GitHub preview supports Mermaid in Markdown; for `.mmd`, open with a Mermaid preview extension.

Note: Actual DB column names may differ slightly depending on Hibernate naming strategy, but table names and relations match the JPA entities.
