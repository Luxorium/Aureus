# Changelog

All notable changes to Aureus will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Changed

- Updated SQLite JDBC to 3.53.2.0.
- Updated the JUnit BOM to 6.1.0.
- Made CI run `./gradlew clean test build` explicitly.
- Documented the full default configuration key set.

## [0.1.1] - 2026-06-19

### Changed

- Build with Java 25-compatible bytecode on any Java 25 or newer compiler.
- Refreshed release documentation for the current Luxorium plugin ecosystem.

### Verified

- Gradle tests and build pass on Java 26 while targeting Java 25 bytecode.

### Changed

- Standardized repository documentation, GitHub metadata, and CI configuration.
- Standardized on `plugin.yml` as the single Paper/Folia plugin descriptor.

## [0.1.0] - 2026-06-16

### Added

- Initial Folia-native Aureus foundation.
- SQLite account and transaction storage with WAL mode.
- Player balances, payments, admin economy commands, transaction history, and cached balance top.
- Minor-unit money parsing and formatting.
- Public `EconomyService` API for future Luxorium plugins.
- Unit tests for money handling, balance mutation, transfers, insufficient funds, and transaction logging.
