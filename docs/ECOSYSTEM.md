# Luxorium Ecosystem

Aureus is one plugin in the Luxorium plugin ecosystem:

| Plugin | Current Release | Role | Dependency Model |
| ------ | --------------- | ---- | ---------------- |
| Civitas | 1.0.1 | Essentials, administration, and quality-of-life commands | Standalone |
| Aureus | 0.1.1 | Economy core | Standalone |
| Mercatus | 0.1.2 | Player shops and trading | Standalone with optional Aureus integration |
| Munera | 0.1.2 | Contracts, jobs, quests, and buy orders | Standalone with optional Aureus integration |

Aureus should remain a stable economy foundation. Other Luxorium plugins may
integrate with its API, but Aureus must not hard-require those plugins.

## Descriptor Standard

Luxorium plugins use `src/main/resources/plugin.yml` as the single plugin
descriptor. Paper/Folia 26.1.2 supports this descriptor with `folia-supported:
true`, and using one descriptor avoids duplicated metadata drift.
