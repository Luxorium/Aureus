# Contributing

Aureus is part of the Luxorium plugin ecosystem. Keep changes focused,
documented, and safe for Folia.

## Development Requirements

- Java 25 or newer.
- Git.
- A local Paper/Folia 26.1.2 test server for runtime testing.

## Build

```bash
./gradlew clean build
```

The compiled jar is written to `build/libs/Aureus-0.1.0.jar`.

## Pull Request Checklist

- Keep changes focused on one behavior or documentation area.
- Update `README.md`, `docs`, or `CHANGELOG.md` when user-facing behavior changes.
- Add or update commands and permissions in `plugin.yml` when needed.
- Run `./gradlew clean build` before opening a pull request.
- Test runtime behavior on Paper/Folia 26.1.2 when command or listener behavior changes.

## Folia Safety

Do not add legacy `BukkitScheduler`, `BukkitRunnable`, or `runTask*` usage. Do
not add unsafe cross-thread world, entity, player, or inventory access. See
[Folia Safety](docs/FOLIA_SAFETY.md).

## Code Style

- Use clear names and keep command classes focused.
- Prefer existing services and helpers before adding abstractions.
- Keep blocking storage work away from region and entity threads.
- Keep comments short and focused on non-obvious threading or persistence behavior.
