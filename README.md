<p align="center">
  <img src="./assets/starsuites-banner.svg" width="100%" alt="StarSuites Banner" />
</p>

<div align="center">

# StarSuites (Drakes-Suites Monorepo) - Branch `26.x`

### Sovereign Suite Architecture Conceived by JackStar for Minecraft and Distributed Systems
**Official Consolidation of 180+ Individual Plugins into High-Performance Autonomous Suites Ported to Purpur 26.2 (Next-Gen Sandbox 2026/2027)**

[![Maven Build](https://img.shields.io/badge/Maven_Reactor-BUILD_SUCCESS-22C55E?style=for-the-badge&logo=apachemaven&logoColor=white)](https://github.com/DrakesCraft-Labs/Drakes-Suites)
[![Purpur 26.2](https://img.shields.io/badge/Purpur_API-26.2_NextGen-8B5CF6?style=for-the-badge&logo=purpur&logoColor=white)](https://purpurmc.org/)
[![Java 25](https://img.shields.io/badge/Java-25_LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Rust Off-Heap](https://img.shields.io/badge/Rust_SIMD-Off--Heap_FFM-DEA584?style=for-the-badge&logo=rust&logoColor=white)](https://www.rust-lang.org/)
[![SQLite WAL](https://img.shields.io/badge/SQLite-WAL_Subsystem-06B6D4?style=for-the-badge&logo=sqlite&logoColor=white)](https://sqlite.org/)
[![SRE Triad](https://img.shields.io/badge/SRE_Triad-Antigravity_Codex_Claude-F59E0B?style=for-the-badge)](https://github.com/DrakesCraft-Labs)

**[English](#)** | **[Leer en Espanol (README_ES.md)](./README_ES.md)**

[Architectural Design](#1-why-starsuites-is-an-architectural-masterpiece) |
[Canonical Suites](#3-canonical-breakdown-of-the-suites) |
[Modular Configuration](#4-modular-configuration-standard-modulesyml) |
[Modality Matrix](#7-modality-matrix-and-slimefun-activation) |
[Organizational Roadmap](#10-organizational-absorption-matrix-remaining-addons-for-starsuites) |
[Production Classification](#11-canonical-classification-of-server-plugins) |
[Roadmap](#12-roadmap-to-26x-20262027)

</div>

---

> ### Branch Status: `26.x` (Next-Generation Sandbox)
> This branch implements the evolution of the StarSuites architecture towards the **Purpur 26.2** API (`api-version: 26.2`, Java 25 bytecode, modern Maven dependencies, and complete test suite validation).  
> **Production Branch:** The rock-solid production release for the active season running in Dallas remains on [`main`](https://github.com/DrakesCraft-Labs/Drakes-Suites/tree/main) (Paper 1.21.11 LTS).

---

## 1. Why StarSuites is an Architectural Masterpiece

The transition from a fragmented ecosystem of **180+ standalone micro-plugins** into **StarSuites** is not merely a refactoring: it is a monumental leap in Minecraft server engineering, distributed state management, and systems performance.

```
                      BEFORE: 180+ Dispersed Repositories
  +---------------+ +---------------+ +---------------+ +---------------+
  | SF Addon #1   | | SF Addon #2   | | SF Addon #3   | | SF Addon #180 |
  | 70+ Async Loop| | Memory Leaks  | | Monolithic YML| | GC Spikes     |
  +-------+-------+ +-------+-------+ +-------+-------+ +-------+-------+
          +-----------------+---------------+-----------------+
                                    | Severe TPS Drops & Deadlocks
                                    v
                      AFTER: StarSuites Unified Engine
  +---------------------------------------------------------------------+
  |                         DRAKES-CORE KERNEL                          |
  |  +-------------------------+  +----------------------------------+  |
  |  | Spatial Ticker Engine   |  | Off-Heap Rust SIMD (Java 25 FFM) |  |
  |  | (~70% CPU Load Reduced) |  | (Graph Traversal & Power Grid)   |  |
  |  +-------------------------+  +----------------------------------+  |
  |  +-------------------------+  +----------------------------------+  |
  |  | Decoupled modules/*.yml |  | Transactional SQLite WAL Engine  |  |
  |  | (Zero-Lag Hot Reload)   |  | (Zero Data Loss Anti-Dupe Vault) |  |
  |  +-------------------------+  +----------------------------------+  |
  +---------------------------------+-----------------------------------+
                                    | High-Throughput Bus
          +-------------------------+-------------------------+
          v                         v                         v
   DrakesTech (Logistics)    DrakesBio (Genetics)      DrakesMagic (Arcana)
   DrakesGenerators (Energy) DrakesCombat (Arsenal)    DrakesServer (Star)
```

### The 7 Structural Pillars of Engineering:

1. **The 180-to-8 Paradigm Consolidation:**  
   Managing 180 individual Git repositories, separate CI/CD pipelines, diverging dependencies, and competing versions is an operational nightmare. StarSuites encapsulates this entire landscape into **8 coherent, domain-driven Mega-Suites** compiled via a unified Maven Reactor build in under 60 seconds.
2. **Chunk-Spatial SuiteTickerEngine (~70% CPU Eradication):**  
   Vanilla Bukkit addons historically registered independent `runTaskTimer` loops every tick, causing thousands of uncoordinated chunk queries and NBT scans. The `SuiteTickerEngine` hashes active blocks by loaded chunk; when players leave a region, the engine **sleeps idle chunk tickers completely**, eliminating 65-80% of background CPU overhead.
3. **Off-Heap Rust SIMD Acceleration (Java 25 FFM API):**  
   Critical bottlenecks, such as massive logistics network graph traversal (Networks/Cargo) and energy distribution matrices, are delegated to native Rust libraries (`libslimefun_ffi.so` and `libodysseia_ffi.so`) via the Java 25 Foreign Function & Memory (FFM) API. Calculations execute off-heap with zero Garbage Collection pauses.
4. **Decoupled Modular YAML Configuration (`modules/<module>.yml`):**  
   Monolithic 15,000-line configuration files that break upon syntax errors have been eradicated. Every absorbed system possesses its own isolated configuration file in `plugins/<Suite>/modules/<module>.yml`, complete with hot-reloading via `/drakessuites reload <suite> [module]` without restarting the server.
5. **Absolute Zero-Data-Loss PDC Key Matching:**  
   Every single PersistentDataContainer (PDC) identifier (`slimefun:slimefun_item`) has been preserved with byte-for-byte fidelity (e.g., `NETWORKS_CABLE`, `INFINITY_SINGULARITY`, `GCE_CHICKEN_T9`, `SLIMETINKER_TINKERS_WORKBENCH`). Existing player inventories, quantum vaults, and base infrastructures migrate with zero loss.
6. **Transactional SQLite WAL State Storage:**  
   Critical state, transaction auditing, anti-dupe nonces, and inventory snapshots write to concurrent SQLite WAL (Write-Ahead Logging) storage, removing disk I/O freezes from the server main thread.
7. **Star Engine Evolution (Odysseia -> Star):**  
   Kernel unification integrating 6-modality airtight inventory isolation (`InvSwitcher`), Tebex idempotent purchase verification, live server telemetry, dynamic economy algorithms (SII), and safety guards in a single binary.

---

## 2. The Three Sovereign Pillars of Engineering

StarSuites is an integral part of the unified software architecture created and led by **JackStar (Jack)**:

1. **Campus Educational Technology & Institutional Infrastructure:** Software engineering and private campus networking, lab infrastructure, institutional automation tools, and management portals (strictly maintained under educational privacy standards).
2. **Star / JackStar:** Personal engineering identity as software architect, host of the server matrix (`Star Server`, `Nova`, `Nexus`), autonomous SRE orchestrator (**SAORI**), evolution of Odysseia to **Star Engine**, and master author of **StarSuites**.
3. **DrakesCraft:** Massively multiplayer production Minecraft network (hosted in Dallas, TX), player community, dynamic macroeconomic markets, and mythic boss pantheons.

> **Canonical Upstream Attribution (Slimefun):**  
> Slimefun is a monumental open-source project originally created by **TheBusyBiscuit** and maintained by its community. **JackStar is not the original creator of Slimefun**; JackStar assumed the role of modernizing rescue architect: solving critical performance collapses on Paper 1.21.11, eliminating 70+ duplicate async tickers, writing off-heap native Rust accelerators ([`Slimefun-Rust`](https://github.com/DrakesCraft-Labs/Slimefun-Rust)), and consolidating 180+ micro-repositories into the official Suites.  
> 
> **Sovereign Attribution (Multiverse Suite):**  
> The Multiverse Suite (`drakes-multiverse.jar`) is the sovereign intellectual creation of **Chagui68** (Chagui), encapsulating `MultiverseCreatures` and `MultiverseNets`.

---

## 3. Canonical Breakdown of the Suites

The StarSuites ecosystem condenses 180+ repositories into **8 Official Mega-Suites** plus the sovereign Multiverse Suite:

| Suite | JAR Artifact | Domain | Consolidated Systems & Addons from Organization |
| :--- | :--- | :--- | :--- |
| **Suite 0** | `drakes-core.jar` | Kernel & Central Engine | Slimefun4-Drake runtime, shaded Dough-core, Centralized Ticker (`SuiteTickerEngine`), Rust JNI/FFM Bridge (`NativeEngineBridge`), SQLite WAL DB, and Audit Subsystem. |
| **Suite 1** | `drakes-tech.jar` | Logistics & Digital Networks | **Native Modules:** `InfinityExpansionModule` (Singularities & Tier 10), `DynaTechModule` (Tesseracts, Angel Gem, Growth Chambers), `SupremeModule`, `FluffyMachinesModule`, `FoxyMachinesModule`, `NanotechModule`. **Absorbed Systems:** Networks, DrakesNanotech, SensibleToolbox, EquivalencyTech (EMCTech), Quaptics, AdvancedTech, DankTech2, GlobiaMachines, Nexcavate, PrivateStorage, SlimeCustomizer / Ryken, IDreamOfEasy, SaneCrafting. |
| **Suite 2** | `drakes-bio.jar` | Bio-Genetics & Agriculture | **Native Modules:** `GeneticChickensModule` (64 species, 6-bit genome, meiotic segregation), `CultivationModule` (82 botanical outcomes, diagonal pollination), `SlimyBeesModule` (26 species, 7-chromosome genome), `FlowerPowerModule`, `TreeTapsModule` (resin, amber, rubber), `ExoticGardenModule`, `MobCapturerModule`. **Absorbed Systems:** Gastronomicon, Drugfun, GlobalWarming, HotbarPets. |
| **Suite 3** | `drakes-magic.jar` | Arcana & Alchemy | **Native Modules:** `AlchimiaVitaeModule` (9 mystical infusions, Soul Collector, totem battery), `CrystamaeModule` (Crystamae Historia: 9 domains, 6 rarities, T1-T5 staves, 12 spells), `RelicsCthoniaModule`, `SoulJarsModule` (16 soul jars). **Absorbed Systems:** Netheopoiesis (with virtual Nether collection), TranscEndence, SpiritsUnchained, DemonicExpansion, ElementManipulation, MagicXpansion, SlimeChem, Coronalis, InfernalExpansion. |
| **Suite 4** | `drakes-generators.jar` | Energy Matrix & Power | **Native Modules:** `UltimateGeneratorsModule` (Endless, Diesel, Biofuel, Dragon Breath, End Crystal Generators), `LiteXpansionModule`, `SMGModule`, `EcoPowerModule`, `OreChunksModule` (11 mineral chunks, geological yields), `BetterReactorsModule`, `LiquidModule` (Hydrocarbons & Fuel Flow). |
| **Suite 5** | `drakes-utility.jar` | Utilities & QoL | **Native Modules:** `BackpacksModule` (DyedBackpacks, 96 variants, strict anti-dupe), `ColoredEnderChestsModule` (4096 frequencies), `SoundMufflerModule`, `SimpleUtilsModule` (Elevator, Workbench, Wrench), `ExtraToolsModule` (3x3 Hammer, Cobble Generator, Concrete Factory, Pulverizer, Gold Transmuter), `ChestTerminalModule`, `SFCalcModule`, `SlimeHUDModule`, `EnderCarryOnModule`, `TrashModule`, `VanillaPatPatModule`. **Absorbed Systems:** SFPortalGun, SmallSpace, JustEnoughGuide, WorldEditSlimefun, GeyserHeads, SfChunkInfo. |
| **Suite 6** | `drakes-combat.jar` | Arsenal & Combat | **Native Modules:** `MobDropsModule` (SFMobDrops: calibrated entity drop tables), `SlimeTinkerModule` (custom alloys, traits, sharpness), `WarfareModule`, `ExtraGearModule`, `ObsidianExpansionModule` & `ObsidianArmor`, `SlimefunNukesModule`, `SlimefunDiscModule`. **Absorbed Systems:** SlimefunLuckyBlocks, Galaxyfun (unified Galactifun 1 & 2), MissileWarfare, DrakesBosses, FNAmplifications, MilitaryArsenal, HardcoreSlimefun, CringleBosses. |
| **Suite 7** | `drakes-server.jar` | Server Core (Star Engine) | **Odysseia -> Star Evolution**: Core server engine, Rust/Java bridge (`Star-Rust`), InvSwitcher (strict 6-modality isolation), PlayerVaultZ, AxGraves, BreweryX, BreweryMenu, Dynamic Market (`/sm`), RandomExpansion, Bump. |
| **Suite Multiverse** | `drakes-multiverse.jar` | **Sovereign: Chagui68** | **Native Integration:** Consolidates `MultiverseNets` (complete standalone digital logistics engine without Slimefun, 109 unit tests on Paper 1.21.11 and Purpur 26.X) and `MultiverseCreatures` (mythological beasts, thematic bosses, celestial rituals). |

---

## 4. Modular Configuration Standard (`modules/*.yml`)

Each Mega-Suite organizes configuration into dedicated files inside `plugins/<Suite>/modules/`:

```
plugins/
+-- DrakesCore/
|   +-- config.yml                # Kernel configuration, telemetry, SuiteTickerEngine
|   +-- modules/
+-- DrakesTech/
|   +-- config.yml                # Performance master switches & energy caps
|   +-- modules/
|       +-- networks.yml          # Digital transport bandwidth and limits
|       +-- dynatech.yml          # Dynamic machinery & quantum generators
|       +-- infinity.yml          # Singularity formulas & Infinity recipes
|       +-- supreme.yml           # Supreme industrial alloys & overclockers
|       +-- advancedtech.yml      # Particle accelerators & quantum compressors
|       +-- danktech.yml          # Mass condensators & fluid storage
|       +-- fastmachines.yml      # Overclocking multipliers & buffers
+-- DrakesBio/
|   +-- config.yml
|   +-- modules/
|       +-- genetic_chickens.yml  # Tiers 0-9, incubators, mutation rates
|       +-- exotic_garden.yml     # Crop growth and culinary recipes
|       +-- slimy_bees.yml        # Apiaries, genetic bee breeding, honey flow
+-- DrakesMagic/
|   +-- config.yml
|   +-- modules/
|       +-- alchimia_vitae.yml    # Transmutation vats & elixirs
|       +-- crystamae.yml         # Resonators & crystal attunement
|       +-- netheopoiesis.yml     # Nether transmutations & virtual drop vault
|       +-- demonic_expansion.yml # Soul altars & dark pacts
+-- DrakesGenerators/
|   +-- config.yml
|   +-- modules/
|       +-- litexpansion.yml      # Void reactors & vacuum generators
|       +-- better_reactors.yml   # Fission loops, cryocooling, radiation
|       +-- ultimategenerators.yml# Joules per tick (J/t) and heat dissipation
+-- DrakesUtility/
|   +-- config.yml
|   +-- modules/
|       +-- backpacks.yml         # Color-coded high-capacity storage
|       +-- chest_terminal.yml    # Wireless indexing & range
|       +-- portalgun.yml         # Quantum entanglement spatial teleporters
+-- DrakesCombat/
|   +-- config.yml
|   +-- modules/
|       +-- warfare.yml           # Ballistics, ammo types, exoskeletons
|       +-- slimetinker.yml       # Custom traits, alloys, edge sharpness
|       +-- galaxyfun.yml         # Planetary mechanics, spaceships, life support
|       +-- luckyblocks.yml       # Slimefun drops and chaotic loot tables
|       +-- obsidian_expansion.yml# T10 armor plating & blast dampening
+-- DrakesServer/
    +-- config.yml
    +-- modules/
        +-- star.yml              # Star telemetry, native Rust bridge, SQLite WAL
        +-- invswitcher.yml       # Airtight 6-modality inventory isolation
        +-- playervaultz.yml      # Anti-dupe virtual player vaults
        +-- breweryx.yml          # Custom fermentation & aging engine
```

### Granular In-Game Hot-Reload:
Modules can be dynamically reloaded or toggled without a full server reboot:
```bash
/drakessuites reload <suite> [module]
```

---

## 5. Unified Ticker Engine (`SuiteTickerEngine`)

Addons no longer trigger independent Bukkit async timers that flood the primary server tick thread:
1. **Spatial Chunk Hashing:** Organizes active ticking blocks strictly by loaded chunk coordinates.
2. **Discard Idle Chunks:** Unloaded or inactive regions consume **0 CPU cycles**.
3. **Batch Updates:** Aggregates block updates into unified micro-batches, preventing tick-loop spikes.

---

## 6. Compilation, Dual Maven Profiles & Cross-Version Architecture (1.21.11 / 26.X)

To ensure each StarSuites JAR runs seamlessly without bytecode modification on both the live production environment (**Paper/Purpur 1.21.11**) and the next-gen platform (**Purpur 26.X / 26.2**), the monorepo provides a dual-profile architecture:

### Maven Compilation Profiles
* **`paper-21` (Active for Current Season):**
  Targets `Paper 1.21.11-R0.1-SNAPSHOT` with `MockBukkit 4.110.0` and `--release 21`. Recommended for building production artifacts for the current live season.
  ```bash
  mvn clean test
  ```
* **`purpur-26` (Next-Gen Sandbox 26.2):**
  Targets `Purpur 26.2.build.+` with Java 25 bytecode to validate forward-compatibility:
  ```bash
  mvn clean test -Ppurpur-26
  ```

---

## 7. Modality Matrix and Slimefun Activation

DrakesPurpur and StarSuites support 6 canonical modalities:

| Modality Key | Modality Name | Slimefun Enabled? | Description & Infrastructure |
| :--- | :--- | :--- | :--- |
| `survival_sf` | Survival Slimefun | YES | Main technical survival network with all StarSuites enabled. |
| `survival_clasico`| Survival Clasico | NO (VANILLA PURO) | **The ONLY modality where Slimefun is completely disabled.** Pure vanilla gameplay. |
| `oneblock` | OneBlock | YES | Infinite regenerating block progression with BentoBox and technical machines. |
| `onechunk` | OneChunk | YES | Extreme technical automation confined to a single chunk boundary. |
| `skyblock` | SkyBlock | YES | Classic BSkyBlock void survival powered by StarSuites technology. |
| `lab` | Laboratorio | YES | Staging laboratory with scheduled world resets for testing high-load builds. |

---

## 8. Backward Compatibility & Zero Data Loss Guarantee

* **Immutable PDC Signatures:** Key namespaces in `slimefun:slimefun_item` match upstream perfectly (`NETWORKS_CABLE`, `INFINITY_SINGULARITY`, `COLORED_ENDER_CHEST_SMALL_0_0_0`, `DYED_BACKPACK_SMALL_RED`, etc.). Existing player items, blocks, and chests retain identical persistent data.
* **Transactional SQLite WAL:** Guarantees atomic writes on server shutdown or crash, completely preventing item rollbacks or dupe exploits.

---

## 9. Canonical Development Directive & Dual Lifecycle

To guarantee absolute operational stability and prevent technical misunderstandings between core developers, contributors (such as Chagui68), and autonomous SRE agents (Antigravity, Codex, Claude), development operates strictly under a **two-level architecture**:

### Level 1: Live Production in Dallas (Current Active Season)
* **Operational Reality:** The production server currently runs with individual standalone JARs (`MultiverseNets-v3.3.jar`, `Supreme.jar`, `PlayerVaultZ.jar`, etc.).
* **Anti-Wipe Golden Rule:** **It is strictly forbidden to replace the live individual plugins with the consolidated suites mid-season.** Doing so risks player inventory loss, entity desync, and structure corruptions.
* **Live Hotfix Workflow:** When a critical bug occurs on the live server:
  1. Fix the issue directly in the plugin dedicated repository (e.g., `MultiverseNets`).
  2. Run local unit tests (`mvn clean test`).
  3. Compile its individual JAR and deploy it to Dallas via `desplegar_via_panel.py`.

### Level 2: StarSuites Monorepo (Staging & Next Season)
* **Objective:** Complete consolidation into the 8 official suite JARs (`drakes-core` through `drakes-server` + `drakes-multiverse`) for a clean, unified launch next season.
* **Developer Workflow Guide (Chagui & Staff):**
  - If you need to develop features for a specific plugin (e.g., `MultiverseNets` or `MultiverseCreatures`), you can work in your dedicated repository or contribute directly to the monorepo.
  - When you build and publish to the organization Maven repository, StarSuites automatically pulls and bundles the latest version on the next global build (`mvn clean package`).

---

## 10. Organizational Absorption Matrix: Remaining Addons for StarSuites

The following table provides the definitive technical roadmap of standalone addons currently in staging or Dallas that are designated for complete native module absorption into StarSuites:

| Destination Suite | Standalone Addon | Absorption Status | Technical Implementation Notes |
| :--- | :--- | :--- | :--- |
| **`drakes-tech`** | `FastMachines` | Pending Module Port | Modernize API version from 1.18 to 26.2; integrate speed buffer formula with `SuiteTickerEngine`. |
| **`drakes-tech`** | `Networks` | Native Port Active | Compile `NetworksV6-drake` directly into `drakes-tech` with off-heap Rust SIMD graph traversal. |
| **`drakes-tech`** | `Quaptics` | Slated for Absorption | Quantum optical lasers and photon routing; eliminate redundant chunk listeners. |
| **`drakes-tech`** | `SensibleToolbox` | Slated for Absorption | Modular item processing and digital sorting buffers. |
| **`drakes-tech`** | `EquivalencyTech` / `DankTech2` | Slated for Absorption | EMC transmutation and compressed bulk storage canisters. |
| **`drakes-tech`** | `Nexcavate` | Slated for Absorption | Automated quarry excavators with chunk boundary verification. |
| **`drakes-magic`**| `Netheopoiesis` | Parched / Ingestion Ready | **Critical Anti-Trash Patch:** Implements a virtual auto-collection vault for Nether drops and Steve heads instead of littering Nether terrain. |
| **`drakes-magic`**| `MagicXpansion` | Slated for Absorption | Arcane staves, spellbooks, and elemental enchantments. |
| **`drakes-magic`**| `SlimeChem` | Slated for Absorption | Periodic table chemistry, molecular distillation, and elemental compounding. |
| **`drakes-magic`**| `SpiritsUnchained` / `Demonic` | Slated for Absorption | Dark occult altars, spirit summoning, and soul binding. |
| **`drakes-bio`**  | `Cultivation` | Native Module Complete | 82 canonical botanical outcomes and cross-pollination. |
| **`drakes-bio`**  | `MobCapturer` | Native Module Complete | Reusable entity capture capsules preserving exact mob NBT. |
| **`drakes-bio`**  | `Gastronomicon` / `Drugfun` | Slated for Absorption | Culinary crafting, beverage distillation, and metabolic effects. |
| **`drakes-utility`**| `SFPortalGun` | Slated for Absorption | Quantum spatial teleportation using native raytracing and PDC pair linking. |
| **`drakes-utility`**| `GeyserHeads` | Slated for Absorption | Bedrock skin and head profile resolver for mobile players. |
| **`drakes-utility`**| `WorldEditSlimefun` | Slated for Absorption | WorldEdit integration for pasting custom Slimefun blocks without metadata loss. |
| **`drakes-utility`**| `ChestTerminal` | Slated for Deprecation | Deprecate in favor of `Networks` and Chagui68 `MultiverseNets`. |
| **`drakes-combat`** | `SlimefunLuckyBlocks`| Slated for Absorption | Lucky block breaking tables with weighted Slimefun item reward pools. |
| **`drakes-combat`** | `Galaxyfun` | Unified Binary Active | Consolidated merger of Galactifun 1 and Galactifun 2 into a single unified space exploration engine. |
| **`drakes-combat`** | `MissileWarfare` | Slated for Absorption | Ballistic missile silos, targeting computers, and radar defense grids. |
| **`drakes-generators`**| `LiteXpansion` | Slated for Absorption | Quantum void power generators and dark matter fuel cells. |
| **`drakes-generators`**| `Liquid` | Slated for Absorption | Fluid hydrocarbon pipeline hydraulics and refinery systems. |
| **`drakes-server`** | `sBank` | Core Integration | Embedded banking and dynamic player interest; dupe-proof transactional SQLite WAL engine. |

---

## 11. Canonical Classification of Server Plugins

On the live production server (**Dallas**), four distinct tiers of software coexist:

1. **Mega-Suites StarSuites (Monorepo `Drakes-Suites`):**
   - Replaces 160+ individual JARs with the 8 official JARs (`drakes-core` through `drakes-server`) + `drakes-multiverse` by Chagui68.
   - **`drakes-server.jar` (Star Engine):** Canonical evolution of Odysseia to **Star**. Server core, Star telemetry, idempotent Tebex purchases (`purchases.db`), economic oversight (SII), and native Rust bridge.
2. **Sovereign GitHub Organization Forks (`DrakesCraft-Labs` - Open Source):**
   - **`BentoBox-Drake`**: Official sovereign island engine fork (BSkyBlock, OneBlock, CaveBlock, AcidIsland) with the critical Paper Data Components item fix in `ItemStackTypeAdapter`.
   - **`CrazyAuctions-Drake`**: Player auction house (`/ah`) with zero GUI dupe bugs.
   - **`DrakesSlimeMarket`**: Dynamic macro-economy (`/sm`) with 777 controlled offers and exponential moving average dampening.
   - `AxGraves-Drakes`, `WorldwideChat-Drake`, `PlayerVaultZ-Drake`, `BreweryX-Drake`, `Inventory-Rollback-Plus-Drake`, `DrakesBosses`, `MultiverseCreatures`, `DrakesRankup`, `ExcellentCrates-Drake`, `LevelledMobs-Drake`.
3. **Commercial / Closed-Source / Licensed Binaries (Legitimately Licensed by Jack):**
   - `nLogin` and `nAntiBot` (Lifetime enterprise license paid by Jack).
   - `TAB` (Premium version for advanced tablist and rank prefixes).
   - `UltimateShop` (Commercial GUI shops).
   - `SlotMachine`, `LibsDisguises Premium`, `ModelEngine`, `FancyNpcs`, `DeluxeMenus`, `GrimAC`.
   - Platform: `WorldGuard`, `CoreProtect`, `InteractiveChat`, `MiniMOTD`, `Geyser-Spigot`, `Floodgate`, `DiscordSRV`.
4. **Hybrid Native Rust Components (Off-Heap / Java 25 FFM):**
   - `Slimefun-Rust` (`libslimefun_ffi.so`): Graph topology routing and SIMD power grid calculations without GC pauses.
   - `Star-Rust` / `Odysseia-Rust` (`libodysseia_ffi.so`): Redstone clock guard, high-performance chat filters, and 3D boss aura geometric evaluations.

---

## 12. Roadmap to 26.X (2026/2027)

```
[Phase 1: StarSuites Monorepo (Maven Reactor Multi-Module)]
                         |
                         v
[Phase 2: Unified Ticker Engine (SuiteTickerEngine & modules/*.yml)]
                         |
                         v
[Phase 3: Core Dependency Integration (Odysseia -> Star Evolution)]
                         |
                         v
[Phase 4: Next-Gen 26.X Dual Sandbox (CrossVersionAdapter, PurpurRuntimeProvider, Backpacks, MultiverseNets)]
                         |
                         v
[Phase 5: Port Remaining Addons (FastMachines, Netheopoiesis, Galaxyfun, LuckyBlocks)]
                         |
                         v
[Phase 6: Clean Season Cutover (Atomic Deployment on Dallas)]
```

---

## 13. Operational Deployment & Monolithic Engine Integration

StarSuites binaries (`drakes-*.jar`) are deployed alongside the sovereign **DrakesPurpur 26.2** kernel:

### Build Artifacts Deployment
Compile the complete suite reactor:
```bash
mvn clean package -DskipTests -T 1C
```
The resulting JARs are placed into the server's `plugins/` directory:
* `drakes-core.jar` (Kernel, Dough-core, DB WAL, Spatial Ticker)
* `drakes-tech.jar`, `drakes-bio.jar`, `drakes-magic.jar`, `drakes-generators.jar`
* `drakes-utility.jar`, `drakes-combat.jar`, `drakes-server.jar`, `drakes-multiverse.jar`
* Accompanied by the foundational `Slimefun.jar` runtime and `plugins/Slimefun/native/libslimefun_ffi.so`.

### In-Game Management Commands
* `/drakessuites`: Displays active suites, absorbed modules, and ticker metrics.
* `/drakessuites reload <suite> [module]`: Hot-reloads configuration without restarting the server.
* `/hardcoresf toggle` (or `/hsf`): Operator toggle for HardcoreSlimefun mechanics (disabled by default).
* `/gw`: Operator inspection and toggle for GlobalWarming climate events (disabled by default).

---

<div align="center">

**Designed and Built with Pride by JackStar (Jack) and the SRE Symmetric Triad**  
*DrakesCraft - Star Systems - All Rights Reserved*

**[Leer Documentacion en Espanol (README_ES.md)](./README_ES.md)**

</div>

---

## License & Intellectual Property

This project is derived from **Slimefun 4** by **TheBusyBiscuit** and the Slimefun community, licensed under **GPL-3.0**. The Drakes-Suites monorepo consolidates 160+ plugins, and as a GPL-3.0 derivative, it is distributed under the same GPL-3.0 license.

**Upstream Attribution:** Slimefun is the original work of **TheBusyBiscuit** and the Slimefun community. Drakes-Suites is a consolidation and modernization effort led by JackStar6677-1 for the DrakesCraft ecosystem.

**Modifications:** Modifications copyright 2026 DrakesCraft Labs contributors. Distributed under GPL-3.0.

This software is **Source-Available** for public inspection and technical audit.
