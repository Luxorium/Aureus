## Summary

- 

## Testing

- [ ] `./gradlew clean build`
- [ ] Runtime tested on Paper/Folia 26.1.2, if behavior changed

## Documentation Checklist

- [ ] README updated, if user-facing behavior changed
- [ ] docs updated, if commands, config, storage, or setup changed
- [ ] CHANGELOG updated under `[Unreleased]`

## Plugin Descriptor Checklist

- [ ] `src/main/resources/plugin.yml` command metadata updated, if needed
- [ ] Permission nodes declared, if needed
- [ ] Version references remain consistent

## Folia Safety Checklist

- [ ] No legacy `BukkitScheduler`, `BukkitRunnable`, or `runTask*` usage added
- [ ] No unsafe cross-thread world, entity, player, or inventory access added
- [ ] Blocking I/O stays away from region/entity threads

## Breaking Changes Checklist

- [ ] No breaking config, command, permission, API, or storage changes
- [ ] Breaking changes are documented with migration notes, if present
