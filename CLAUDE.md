# CLAUDE.md

This repository is Riverside Software's fork of SonarSource's sonarlint-core.

- `upstream` = `git@github.com:SonarSource/sonarlint-core.git`
- `origin` = `git@github.com:Riverside-Software/sonarlint-core.git`

Each upstream release line gets its own `OpenEdge-<major>.<minor>` branch. That branch carries the Riverside changes and versions them as `<major>.<minor>.99001`.

## Merging a new upstream sonarlint-core tag

Example: upstream tag `12.0.2.87499`, previous branch `OpenEdge-11.10`.

1. Fetch the upstream tags:
   ```
   git fetch upstream --tags
   ```
2. Create the new branch from the previous OpenEdge branch, so it keeps all Riverside changes:
   ```
   git checkout OpenEdge-11.10
   git checkout -b OpenEdge-12.0
   ```
   If only the patch version changes (same `<major>.<minor>`), stay on the existing branch instead.
3. Merge the tag and keep git's default message (`Merge tag '<tag>' into OpenEdge-<major>.<minor>`):
   ```
   git merge <tag>
   ```
   Resolve any conflicts. Riverside changes must survive, and the upstream changes must be applied as well.
4. Bump the version in **every** `pom.xml`, including `its/**` and `buildSrc/**`. Replace the upstream `<x.y.z>-SNAPSHOT` version (parent and project versions) with `<major>.<minor>.99001`. For example, `12.0.2-SNAPSHOT` becomes `12.0.99001`. Then check that no `-SNAPSHOT` of the sonarlint-core version is left anywhere.
5. Commit the version bump on its own, with the message `Version <major>.<minor>.99001`.
6. Build to check that the merge compiles.
   ```
   mvn -Dmaven.test.skip=true -P dist-no-arch clean package
   ```
7. **Never push** unless explicitly asked.

