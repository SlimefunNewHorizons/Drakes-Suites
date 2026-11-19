# Carried entity snapshots — candidate fix (#884)

Status: **unvalidated candidate**, not deployed. No production inventories or worlds were modified.

EnderCarryOn previously placed the entire entity snapshot into a PDC string. NBT strings use a 65,535-byte modified UTF-8 limit; large villagers can exceed it. `CarriedEntityData` retains the original string representation when it fits and stores larger snapshots as UTF-8 byte arrays under the same key. Both representations remain readable. Malformed binary input fails without replacing existing data.

Release now keeps the carrier when parsing, spawning, or decoding fails, including a returned entity whose spawn was rejected. All six existing event handlers remain present. The same source change is mirrored in the standalone EnderCarryOn repository.

The added tests cover the exact UTF boundary for ASCII, null, BMP and supplementary characters, a synthetic 67,048-byte snapshot, legacy reads, invalid encodings, and retained carriers after restoration failures or an invalid returned entity. **None of these tests have run successfully in this session.**

Validation stopped after two environment failures (Fail-Stop 2x):

1. The offline Maven reactor cannot resolve Paper `26.2.build.111-stable`, Slimefun `RC-37`, and SQLite JDBC `3.46.1.0` from the current cache.
2. A focused compilation using the available Paper `26.2.build.133-stable` API lacks Adventure `ObjectContentsLike`; the available Adventure API is `4.26.1`. Compilation stopped before tests ran.

Before integration: restore the declared dependencies, run the full reactor and the regression tests, and exercise pickup, packet encoding, persistence and release of a large synthetic villager in isolated Paper 26.2 staging. Test cancellation of the actual spawn event as well. Identify and back up any existing oversized legacy carrier before considering a separate recovery; this patch does not modify such items automatically.

Rollback before deployment is simply abandoning the candidate branch. After producing binary carriers, reverting to the old reader alone is unsafe: those carriers need the dual-format reader to remain usable. A deployment must first establish a compatible rollback and verified backups.

The existing snapshot format is experimental; this change does not promise migration between Minecraft versions. See the [Paper EntitySnapshot API](https://jd.papermc.io/paper/1.21.11/org/bukkit/entity/EntitySnapshot.html).
