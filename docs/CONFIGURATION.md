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

## Storage Dependency

Aureus bundles SQLite JDBC into the plugin jar intentionally. This keeps the
plugin self-contained and avoids requiring server owners to install a database
driver separately.
