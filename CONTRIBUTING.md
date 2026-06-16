# Contributing

This project is intentionally strict about Folia safety, documentation, and
small reviewable changes.

## Development Requirements

- Java 25 or newer
- Git
- A local Paper or Folia 26.1.2 test server for behavior testing

## Build

```bash
./gradlew clean build
```

The compiled jar is written to `build/libs/Aureus-0.1.0.jar`.

## Pull Request Checklist

- Keep changes focused on one behavior or documentation area.
- Update `README.md` or `CHANGELOG.md` when user-facing behavior changes.
- Add or update permissions in `plugin.yml` when adding commands.
- Run `./gradlew clean build` before opening a pull request.
- Test command behavior on Paper or Folia when changing runtime logic.

## Folia Safety

Aureus is Folia-native. Do not use legacy Bukkit scheduler APIs or direct
cross-thread world/entity access.

## Code Style

- Use clear names and keep command classes focused.
- Prefer existing services and helpers before adding new abstractions.
- Keep database work asynchronous.
- Keep comments short and only where they clarify non-obvious thread behavior.
