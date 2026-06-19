# Configuration

Aureus writes its default configuration to `plugins/Aureus/config.yml` on first
startup.

## Key Areas

| Area | Description |
| ---- | ----------- |
| Currency display | Singular name, plural name, symbol, decimal places, grouping, and separators. |
| Starting balance | Initial balance for newly created accounts. |
| Payment limits | Minimum and maximum payment amounts. |
| Balance top | Cache duration and list size behavior. |
| Transactions | Transaction page size for command output. |
| Storage | SQLite database file name under the plugin data folder. |

Money is stored internally as `long` minor units. With `decimal-places: 2`,
`1.00` is stored as `100`.

## Options

| Key | Default | Description |
| --- | ------- | ----------- |
| `currency.singular` | `Aureus` | Singular currency label. |
| `currency.plural` | `Aurei` | Plural currency label. |
| `currency.symbol` | `⛃` | Symbol used when symbol display is enabled. |
| `currency.decimal-places` | `2` | Number of decimal places parsed and displayed. |
| `currency.show-symbol` | `true` | Whether formatted balances include the currency symbol. |
| `currency.symbol-before-amount` | `true` | Whether the symbol appears before the amount. |
| `currency.thousands-separator` | `,` | Grouping separator for formatted balances. |
| `currency.decimal-separator` | `.` | Decimal separator for formatted balances. |
| `economy.starting-balance` | `100.00` | Balance assigned to new accounts. |
| `economy.minimum-payment` | `0.01` | Smallest allowed player-to-player payment. |
| `economy.maximum-payment` | `1000000.00` | Largest allowed player-to-player payment. |
| `economy.baltop-cache-seconds` | `60` | Cache duration for balance top output. |
| `economy.transactions-page-size` | `10` | Number of transactions shown per command page. |
| `storage.sqlite-file` | `aureus.db` | SQLite file under `plugins/Aureus/`. |

## Storage Dependency

Aureus bundles SQLite JDBC into the plugin jar intentionally. This keeps the
plugin self-contained and avoids requiring server owners to install a database
driver separately.
