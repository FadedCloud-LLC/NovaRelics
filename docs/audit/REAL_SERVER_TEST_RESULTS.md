# Real Server Test Results

## Status: BLOCKED (no server has been started)

| Blocker | Detail | Resolution |
|---|---|---|
| Minecraft EULA | Starting any Spigot or Paper server requires `eula=true`, which means accepting Mojang's EULA. The audit must not accept that on the owner's behalf. | The owner confirms acceptance for disposable test servers. CI can then set it in the ephemeral workspace. |
| Spigot jars | Spigot cannot be downloaded; it is built with BuildTools (`java -jar BuildTools.jar --rev <ver>`), roughly 5–10 min and 1–2 GB of disk per version. | Nightly CI with an `actions/cache` keyed by version. Locally, only PR-smoke versions are built. |
| Docker | Not installed locally. | MySQL/Testcontainers tests run in CI only. |

## Proposed harness (to implement after approval)

Location: `src/integrationTest/` plus `scripts/server-harness/`. It runs only in an isolated temporary directory, never against a production world.

1. Resolve the server jar:
   - Paper: `https://fill.papermc.io/v3/projects/paper/versions/<v>/builds/latest`, then verify the published SHA-256.
   - Spigot: BuildTools output from the cache.
2. Pick the JDK from the manifest minimum (17 / 21 / 25) through `actions/setup-java` or local toolchains.
3. Create `server/` containing `eula.txt` (after approval), and a `server.properties` with `online-mode=false`, `level-type=flat`, `spawn-protection=0`, `view-distance=2`, `max-players=0`.
4. Copy in `NovaRelics.jar` and **`NovaRelicsTestHarness.jar`**. The harness is a small test-only plugin that, once the server is up:
   - runs scripted scenarios using fake-player-free APIs (command dispatch as console, `World#…` block setup, synthetic events through `PluginManager#callEvent`);
   - writes JSON results.
5. Start the server with `-Xmx1G -Dpaper.disableChannelLimit` and stream the log.
6. Wait for `Done (` with a timeout of 180 s.
7. Assert:
   - `NovaRelics` is enabled (`/plugins`, harness check).
   - The log contains no `ClassNotFoundException`, `NoClassDefFoundError`, `NoSuchMethodError`, `NoSuchFieldError`, `IncompatibleClassChangeError` or `UnsupportedClassVersionError`.
   - The log contains no NovaRelics stack traces.
8. Run the scenarios:
   - command registration and output;
   - item build plus PDC;
   - every ability's enum/registry resolution (NR-016);
   - skull texture applied (NR-015);
   - anvil pickup (NR-006);
   - keep-item on death (NR-007);
   - dupe loop (NR-001).
9. Run `stop`, and assert a clean disable with no pending NovaRelics tasks and `data.db` closed.
10. Collect logs and JSON into `build/server-results/<platform>-<version>/`, then delete the server directory.

Player-dependent behaviours (real clicks, armor equip packets) need a headless client, for example a Mineflayer bot on `online-mode=false`. These are proposed as an optional nightly job, not a release gate.

## Results table

Every row is NOT TESTED until the harness runs; see `MINECRAFT_VERSION_MATRIX.md`.
