# Installation

## Requirements

- Java 25 or newer.
- Paper or Folia 26.1.2.
- `Aureus-0.1.1.jar`.

## Steps

1. Download `Aureus-0.1.1.jar` from the latest release.
2. Stop the server.
3. Place the jar in the server's `plugins/` directory.
4. Start the server once to generate `plugins/Aureus/config.yml`.
5. Review the configuration.
6. Run `/aureus reload` after future configuration changes.

## Upgrading

Back up `plugins/Aureus/` before replacing jars. Keep the SQLite database file
with the server data unless release notes say a migration is required.
