# Aureus

[![Release](https://img.shields.io/github/v/release/Luxorium/Aureus?sort=semver&display_name=tag&style=flat-square&label=release&color=blue&cacheSeconds=3600)](https://github.com/Luxorium/Aureus/releases/latest)
[![Build](https://github.com/Luxorium/Aureus/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/Luxorium/Aureus/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/Luxorium/Aureus?style=flat-square&label=license&color=green)](https://github.com/Luxorium/Aureus/blob/main/LICENSE)
[![Java](https://img.shields.io/badge/java-25%2B-orange?style=flat-square)](https://adoptium.net/)
[![Paper/Folia](https://img.shields.io/badge/Paper%2FFolia-26.1.2-blueviolet?style=flat-square)](https://papermc.io/)

Aureus is a Folia-native economy core for Paper/Folia 26.1.2 Minecraft servers.

## Ecosystem Role

Aureus provides the economy foundation for the Luxorium plugin ecosystem. It can
run by itself, and Mercatus or Munera can optionally integrate with it for shop
payments and contract rewards. Civitas remains separate as the essentials,
administration, and quality-of-life command plugin.

## Highlights

- Player accounts with configurable starting balances.
- Payments, balance checks, balance top, and transaction history.
- Admin economy controls for give, take, set, and reset actions.
- SQLite storage with WAL mode and bundled SQLite JDBC.
- Minor-unit currency math to avoid floating point balance drift.
- Public Java API under `dev.luxorium.aureus.api`.

## Compatibility

| Requirement | Version |
| ----------- | ------- |
| Java | 25 or newer |
| Server | Paper/Folia 26.1.2 |
| Aureus | 0.1.1 |
| Optional integrations | None |

## Installation

1. Download `Aureus-0.1.1.jar` from the latest release.
2. Place the jar in your server's `plugins/` directory.
3. Restart the server.
4. Edit `plugins/Aureus/config.yml` as needed.
5. Run `/aureus reload` after configuration changes.

## Build From Source

```bash
./gradlew clean test build
```

The compiled plugin jar is written to:

```text
build/libs/Aureus-0.1.1.jar
```

## Commands and Permissions

| Command | Permission | Description |
| ------- | ---------- | ----------- |
| `/balance`, `/bal`, `/money` | `aureus.command.balance` | Show your balance. |
| `/pay <player> <amount>` | `aureus.command.pay` | Pay another player. |
| `/baltop` | `aureus.command.baltop` | Show the richest accounts. |
| `/transactions [player]` | `aureus.command.transactions.self` | Show transaction history. |
| `/eco <give|take|set|reset> <player> [amount]` | `aureus.admin.eco` | Manage balances. |
| `/aureus reload` | `aureus.admin.reload` | Reload configuration. |

## Configuration

`config.yml` controls currency display, starting balance, payment limits,
balance top cache duration, transaction page size, and the SQLite database file.
See [Configuration](docs/CONFIGURATION.md) for the full reference.

## Data Storage

| Data | Path |
| ---- | ---- |
| Configuration | `plugins/Aureus/config.yml` |
| SQLite database | `plugins/Aureus/aureus.db` |

## Folia Safety

Aureus is Folia-native. It must avoid legacy `BukkitScheduler`,
`BukkitRunnable`, and `runTask*` APIs, and it must avoid unsafe cross-thread
world, entity, or player access. SQLite work runs away from region threads.

## Documentation

- [Installation](docs/INSTALLATION.md)
- [Commands](docs/COMMANDS.md)
- [Configuration](docs/CONFIGURATION.md)
- [Folia Safety](docs/FOLIA_SAFETY.md)
- [Ecosystem](docs/ECOSYSTEM.md)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [SECURITY.md](SECURITY.md).

## License

Aureus is licensed under the [MIT License](LICENSE).
