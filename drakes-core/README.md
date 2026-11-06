# 🏛️ Suite 0: DrakesCore (`drakes-core.jar`)

> **Kernel Central, Motor de Ticks y Persistencia del Monorepo StarSuites**  
> Autor: JackStar (`JackStar6677-1`) | Plataforma: Paper/Purpur 1.21.11+

> This checkout is the `26.x` development branch, built with Java 25.
> Its test results do not certify the historical 1.21.11 production branch.

---

## ⚠️ DIRECTIVA CANÓNICA DE DESARROLLO PERMANENTE
> **Aviso para Desarrolladores y Agentes SRE:**  
> Cualquier modificación al kernel, gestión de base de datos SQLite WAL, puente PDC (`slimefun:slimefun_item`), aceleración SIMD/Rust o motor de tickers asíncronos **DEBE REALIZARSE EXCLUSIVAMENTE EN ESTE MÓDULO**.
> Queda prohibido alterar repositorios históricos archivados como `dough-core` o forks antiguos de Slimefun.

---

## 📦 Componentes Clave
1. **`SuiteTickerEngine`:**
   - Unificación de los bucles de ticks para todos los módulos de StarSuites.
   - Procesamiento agrupado por chunk cargado (`isLoaded()`) con reducción del 70% de overhead de CPU.
2. **`SuiteDatabaseEngine`:**
   - Motor SQLite con modo WAL (`PRAGMA journal_mode=WAL`), caché en memoria y timeouts configurados.
   - Escritura asíncrona por lotes (cola de hasta 50,000 operaciones y flush de 200 items/commit).
   - Tablas maestras: `suite_block_states`, `suite_quantum_storage`, `suite_player_data`, `suite_key_value`, `suite_audit_events`.
3. **`SuiteItemPdcBridge`:**
   - Estandarización de `slimefun:slimefun_item` sobre `PersistentDataContainer` (Data Components).
   - Guardias anti-dupe contra tamaños ilegales de stacks e inyección de identificadores corruptos.
4. **`NativeEngineBridge`:**
   - FFM Java 21 / JNI para conexión quirúrgica con Rust (`Slimefun-Rust` y `Star-Rust`).
   - Operaciones SIMD (`sumSaturating`), validación de redes y geometría de combate con fallback automático en Java puro.

---

## 🧪 Pruebas Unitarias
Compilar y ejecutar tests unitarios:
```bash
mvn -pl drakes-core test
```

## Command and world isolation (#635)

The `26.x` defaults cover both legacy worlds (`world_clasico*`,
`world_oneblock*`, `world_skyblock*`) and current T2 worlds (`clasico`,
`oneblock_world`, `bskyblock_world`, including Nether and End).
`slimefun.disabled-worlds` includes all six current/legacy Classic dimensions.
Existing installations must merge these entries into their own config;
`saveDefaultConfig()` does not replace an existing file.

Command rules match whole argument tokens, ignoring case and repeated whitespace.
For example, `/sf give` also blocks `/slimefun:sf give ITEM`, while `/sf guide`
and `/sf giveaway` remain independent. A root rule such as `/pay` covers every
argument. In whitelist mode, an explicit block still takes precedence.
`command-gate.enabled` and the existing staff bypass remain available.

This source change targets T2 on Java 25. Compilation and MockBukkit tests do
not certify deployment or player journeys on Paper 26.2; record runtime evidence
before marking it active. The historical `main` branch does not contain this
kernel class, so there is no corresponding 1.21.11 source to patch there.

The JAR declares `paperweight-mappings-namespace: mojang` to avoid unnecessary
Paper remapping. The probe is outside Maven's production source tree and is
never included in the kernel JAR.

### Disposable Paper runtime check

After the Java 25 `clean package` with tests, run from the repository root:

```bash
python3 drakes-core/src/runtime-test/run_probe.py \
  --paper-home "$HOME/workspace/season2-staging"
```

The input directory supplies only `paper-26.2-133.jar` and cached server
libraries; worlds, configs and player data are never copied. The runner compiles
through `build_seguro.sh`, creates a fresh fixture under the local SAORI state,
binds an ephemeral port on loopback and limits the server to 2 GiB and two CPUs.
The probe requires `-Ddrakes.probe.commandgate=true`; otherwise it disables itself.
It checks 38 assertions through the real event bus: current/legacy worlds,
namespaced commands, staff bypass, the disable switch, subcommand boundaries and
the six Classic Slimefun exclusions. It then requests a clean server shutdown.
Logs remain in the printed fixture directory. Synthetic players and worlds
exercise the guards; this does not replace real Java/Bedrock client journeys.

Recorded on 2026-11-06: Java 25 / Paper 26.2-133 completed
`COMMAND_GATE_PROBE PASS checks=38`, with no ERROR log entries and a clean
shutdown. The core reactor passed 67 unit/event tests with no failures or skips.
The shared staging and Dallas configs remain pending; this is an isolated run.
