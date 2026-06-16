# Aureus

[![Release](https://img.shields.io/github/v/release/Luxorium/Aureus?sort=semver&display_name=tag&style=flat-square&label=release&color=blue&cacheSeconds=3600)](https://github.com/Luxorium/Aureus/releases/latest)
[![Build](https://github.com/Luxorium/Aureus/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/Luxorium/Aureus/actions/workflows/build.yml)
[![License](https://img.shields.io/github/license/Luxorium/Aureus?style=flat-square&label=license&color=green)](https://github.com/Luxorium/Aureus/blob/main/LICENSE)
[![Java](https://img.shields.io/badge/java-25%2B-orange?style=flat-square)](https://adoptium.net/)

Aureus is a Folia-native economy core for Paper/Folia 26.1.2 servers. It
provides player accounts, balances, payments, admin economy controls, balance
leaderboards, and a durable transaction ledger.

## Highlights

- Player accounts with configurable starting balances
- Payments, admin give/take/set/reset controls, and balance top
- Full SQLite transaction ledger with WAL mode
- Minor-unit currency math to avoid floating point balance drift
- Public Java API under `dev.luxorium.aureus.api`
- No hard dependency on Vault or any other Luxorium plugin

## Compatibility

| Requirement | Version |
|-------------|---------|
| Java | 25 or newer |
| Server | Paper or Folia 26.1.2 |
| Aureus | 0.1.0 |

## Installation

1. Download `Aureus-0.1.0.jar` from the latest release.
2. Place the jar in your server's `plugins/` directory.
3. Restart the server.
4. Edit `plugins/Aureus/config.yml` as needed.
5. Run `/aureus reload` after configuration changes.

## Build From Source

```bash
./gradlew clean build
```

The compiled plugin is written to:

```text
build/libs/Aureus-0.1.0.jar
```

## Commands And Permissions

| Command | Permission |
|---------|------------|
| `/balance`, `/bal`, `/money` | `aureus.command.balance` |
| `/pay <player> <amount>` | `aureus.command.pay` |
| `/baltop` | `aureus.command.baltop` |
| `/transactions [player]` | `aureus.command.transactions.self` |
| `/eco <give|take|set|reset> <player> [amount]` | `aureus.admin.eco` |
| `/aureus reload` | `aureus.admin.reload` |

## Configuration

`config.yml` controls the display format, starting balance, min/max payment
limits, balance top cache duration, transaction page size, and SQLite file name.

Money is stored internally as `long` minor units. With the default
`decimal-places: 2`, `1.00` is stored as `100`.

## Data Storage

Runtime data is stored in SQLite under the server's plugin data directory:

| Data | Path |
|------|------|
| Database | `plugins/Aureus/aureus.db` |

## Folia Notes

Aureus avoids legacy Bukkit scheduler APIs. SQLite work runs off player, world,
and entity threads, while command handlers resolve Bukkit state before
delegating durable work to the async economy service.

## License

Aureus is licensed under the [MIT License](LICENSE).
