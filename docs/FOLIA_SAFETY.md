# Folia Safety

Aureus is Folia-native.

## Rules

- Do not add `BukkitScheduler`, `BukkitRunnable`, or `runTask*` usage.
- Do not perform blocking SQLite work on region or entity threads.
- Resolve Bukkit player state before delegating durable work to async services.
- Return results to players through safe command or scheduler contexts.
- Keep economy API methods asynchronous when they may touch storage.

## Review Checklist

- Storage changes use async execution.
- Command changes do not assume a single global main thread.
- No cross-thread world, entity, player, or inventory access is introduced.
