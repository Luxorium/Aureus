# Commands

| Command | Permission | Description |
| ------- | ---------- | ----------- |
| `/balance` | `aureus.command.balance` | Show your balance. |
| `/bal` | `aureus.command.balance` | Alias for `/balance`. |
| `/money` | `aureus.command.balance` | Alias for `/balance`. |
| `/pay <player> <amount>` | `aureus.command.pay` | Pay another player. |
| `/baltop` | `aureus.command.baltop` | Show the richest accounts. |
| `/transactions [player]` | `aureus.command.transactions.self` | Show transaction history. |
| `/eco give <player> <amount>` | `aureus.admin.eco` | Add money to an account. |
| `/eco take <player> <amount>` | `aureus.admin.eco` | Remove money from an account. |
| `/eco set <player> <amount>` | `aureus.admin.eco` | Set an account balance. |
| `/eco reset <player>` | `aureus.admin.eco` | Reset an account to the starting balance. |
| `/aureus reload` | `aureus.admin.reload` | Reload configuration. |

Players can view their own transaction history by default. Viewing another
player's transaction history requires `aureus.admin.transactions.other`.
