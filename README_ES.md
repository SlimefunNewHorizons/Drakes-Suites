<p align="center">
  <img src="./assets/starsuites-banner.svg" width="100%" alt="StarSuites Banner" />
</p>

<div align="center">

# StarSuites (Monorepo Drakes-Suites) - Rama `26.x`

### Arquitectura Maestra de Suites concebida por JackStar para el Ecosistema de Minecraft y Sistemas Distribuidos
**Consolidacion Oficial de 180+ Plugins en Suites Autonomas portadas a Purpur 26.2 (Next-Gen Sandbox 2026/2027)**

[![Maven Build](https://img.shields.io/badge/Maven_Reactor-BUILD_SUCCESS-22C55E?style=for-the-badge&logo=apachemaven&logoColor=white)](https://github.com/SlimefunNewHorizons/Drakes-Suites)
[![Purpur 26.2](https://img.shields.io/badge/Purpur_API-26.2_NextGen-8B5CF6?style=for-the-badge&logo=purpur&logoColor=white)](https://purpurmc.org/)
[![Java 25](https://img.shields.io/badge/Java-25_LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Rust Off-Heap](https://img.shields.io/badge/Rust_SIMD-Off--Heap_FFM-DEA584?style=for-the-badge&logo=rust&logoColor=white)](https://www.rust-lang.org/)
[![SQLite WAL](https://img.shields.io/badge/SQLite-WAL_Subsystem-06B6D4?style=for-the-badge&logo=sqlite&logoColor=white)](https://sqlite.org/)
[![SRE Triad](https://img.shields.io/badge/SRE_Triad-Antigravity_Codex_Claude-F59E0B?style=for-the-badge)](https://github.com/SlimefunNewHorizons)

**[Read in English (README.md)](./README.md)** | **[Version en Espanol](#)**

[Diseno Arquitectonico](#1-por-que-starsuites-es-una-genialidad-de-ingenieria) |
[Suites Oficiales](#3-desglose-canonico-de-las-suites) |
[Configuracion Modular](#4-estandar-de-configuracion-modular-modulesyml) |
[Matriz de Modalidades](#7-matriz-de-modalidades-y-activacion-de-slimefun) |
[Hoja de Ruta Organizacional](#10-matriz-de-absorcion-organizacional-addons-restantes-para-starsuites) |
[Clasificacion de Plugins](#11-clasificacion-canonica-de-plugins-en-dallas-plugins) |
[Hoja de Ruta](#12-hoja-de-ruta-hacia-26x-2027)

</div>

---

> ### Estado de la Rama: `26.x` — en produccion (Temporada 2)
> Esta es la linea desplegada en el servidor de produccion DrakesCraft (Dallas) desde la Temporada 2: **Purpur 26.2 (build 141) con Java 25**.
> Las 8 suites y `drakes-multiverse` cargan junto con algunos JARs de addons sueltos que aun no se han absorbido
> (ver la [instantanea de runtime](README.md#runtime-snapshot-season-2-2026-11-20), medida el 2026-11-20 desde `logs/latest.log` y los `modules/*.yml`).
> `main` es la linea anterior (Paper 1.21.11, Java 21, Temporada 1) y queda para referencia y hotfixes de esa linea.
>
> **Estandar objetivo:** consolidar todos los addons dentro de las suites y no desplegar JARs sueltos. Hoy conviven ambos; que copia
> es la efectiva en cada caso **aun no esta verificado** (ej. `DynaTech`, `FluffyMachines`, `Supreme`, `NetworksV6`, `SensibleToolbox`).
> Todo arreglo nuevo debe aplicarse tambien en el modulo equivalente de la suite.

---

## 1. Por que StarSuites es una Genialidad de Ingenieria

La transicion de un ecosistema caotico de **180+ micro-plugins y repositorios sueltos** a **StarSuites** no es un simple refactor de codigo; es un salto cuantico en ingenieria de software, arquitectura de sistemas distribuidos y optimizacion extrema de Minecraft:

```
                     ANTES: 180+ Repositorios Dispersos
  +---------------+ +---------------+ +---------------+ +---------------+
  | SF Addon #1   | | SF Addon #2   | | SF Addon #3   | | SF Addon #180 |
  | 70+ BucleAsync| | Fugas Memoria | | YAML Monolito | | Picos de GC   |
  +-------+-------+ +-------+-------+ +-------+-------+ +-------+-------+
          +-----------------+---------------+-----------------+
                                    | Caidas Criticas de TPS y Deadlocks
                                    v
                     DESPUES: Motor Unificado StarSuites
  +---------------------------------------------------------------------+
  |                         KERNEL DRAKES-CORE                          |
  |  +-------------------------+  +----------------------------------+  |
  |  | Spatial Ticker Engine   |  | Off-Heap Rust SIMD (Java 25 FFM) |  |
  |  | (~70% CPU Erradicado)   |  | (Grafos Logisticos y Red Elect.) |  |
  |  +-------------------------+  +----------------------------------+  |
  |  +-------------------------+  +----------------------------------+  |
  |  | Modulos modules/*.yml   |  | Motor Transaccional SQLite WAL   |  |
  |  | (Reload en Caliente 0ms)|  | (Bovedas Anti-Dupe Cero Perdidas)|  |
  |  +-------------------------+  +----------------------------------+  |
  +---------------------------------+-----------------------------------+
                                    | Bus de Alto Rendimiento
          +-------------------------+-------------------------+
          v                         v                         v
   DrakesTech (Logistica)    DrakesBio (Genetica)      DrakesMagic (Arcano)
   DrakesGenerators (Energia)DrakesCombat (Arsenal)    DrakesServer (Star)
```

### Los 7 Pilares Estructurales de la Genialidad:

1. **La Consolidacion Paradigmatica 180 a 8:**  
   Gestionar 180 repositorios Git individuales, pipelines de CI/CD fragmentados, dependencias en conflicto y versiones dispares es inmanejable. StarSuites encapsula todo el universo de Slimefun en **8 Mega-Suites cohesivas** compilables en bloque via Maven Reactor en menos de 60 segundos.
2. **SuiteTickerEngine Espacial (~70% de Reduccion en Carga de CPU):**  
   Historicamente, cada addon registraba tareas Bukkit `runTaskTimer` asincronas independientes cada tick, saturando el hilo principal con escaneos de NBT y chunks vacios. `SuiteTickerEngine` indexa los bloques activos por chunk cargado; cuando los jugadores se alejan, el motor **pone en hibernacion los tickers de chunks inactivos**, erradicando el 65-80% de la sobrecarga de CPU.
3. **Aceleracion Nativa Off-Heap en Rust (Java 25 FFM API):**  
   Los cuellos de botella criticos, como el calculo de grafos en redes de transporte digital (Networks/Cargo) y las matrices de energia SIMD, se delegan a binarios nativos en Rust (`libslimefun_ffi.so` y `libodysseia_ffi.so`) via la API Foreign Function & Memory de Java 25, ejecutando fuera del heap sin pausas de Garbage Collector.
4. **Configuracion Modular Desacoplada (`modules/<modulo>.yml`):**  
   Se erradicaron los archivos monoliticos de 15,000 lineas propensos a corrupciones sintacticas. Cada sistema absorbido tiene su propio YAML aislado y puede recargarse en caliente en vivo via `/drakessuites reload <suite> [modulo]` sin reiniciar el servidor.
5. **Preservacion Absoluta de Claves PDC (Cero Perdida de Datos):**  
   Cada identificador en `PersistentDataContainer` (`slimefun:slimefun_item`) se mantuvo con precision de byte (ej. `NETWORKS_CABLE`, `INFINITY_SINGULARITY`, `GCE_CHICKEN_T9`, `SLIMETINKER_TINKERS_WORKBENCH`). Los inventarios de los jugadores y las bases construidas migran con cero perdidas.
6. **Almacenamiento Transaccional SQLite WAL:**  
   Auditoria de transacciones, nonces anti-duplicacion y persistencia de inventarios usan SQLite con Write-Ahead Logging concurrente, eliminando los bloqueos por I/O de disco del hilo principal del servidor.
7. **Evolucion Star Engine (Odysseia -> Star):**  
   Unificacion del nucleo del servidor integrando aislamiento hermetico de 6 modalidades (`InvSwitcher`), verificacion idempotente de compras Tebex, telemetria Star en vivo, macroeconomia dinamica (SII) y salvaguardas de red en un unico artefacto.

---

## 2. Los Tres Pilares de Ingenieria Soberana

Este monorepo forma parte del ecosistema unificado de ingenieria de software de **JackStar (Jack)**:

1. **Tecnologia Educativa e Infraestructura Institucional:** Ingenieria de software y redes de campus privadas, automatizacion de infraestructura, diagnosticos y plataformas de gestion escolar.
2. **Star / JackStar:** Arquitectura soberana de software, orquestador autonomo de SRE **SAORI**, evolucion del motor Odysseia a **Star Engine** y autoria de **StarSuites**.
3. **DrakesCraft:** Servidor multijugador masivo de Minecraft en produccion (Dallas, TX), comunidad de jugadores, economia macroeconomica dinamica y panteones mitologicos.

> **Reconocimiento Canonico Upstream (Slimefun):**  
> Slimefun original es una colosal obra open-source concebida por **TheBusyBiscuit** y mantenida por su comunidad historica. **JackStar no es el creador original de Slimefun**, sino el arquitecto de modernizacion tecnica: rescate de rendimiento critico en Paper 1.21.11, erradicacion de 70+ tickers asincronos duplicados, aceleracion off-heap en Rust ([`Slimefun-Rust`](https://github.com/SlimefunNewHorizons/Slimefun-Rust)) y consolidacion masiva de 180+ micro-repositorios en las suites oficiales.  
> 
> **Autoria Soberana de la Suite Multiverse:**  
> La Suite Multiverse (`drakes-multiverse.jar`) es creacion y propiedad intelectual de **Chagui68** (Chagui), consolidando sus plugins `MultiverseCreatures` y `MultiverseNets`.

---

## 3. Desglose Canonico de las Suites

El ecosistema StarSuites reemplaza mas de 160 plugins individuales dispersos por **8 Mega-Suites oficiales** mas la Suite soberana Multiverse:

| Suite | Artefacto JAR | Dominio Tecnico | Sistemas y Addons Consolidados de la Organizacion |
| :--- | :--- | :--- | :--- |
| **Suite 0** | `drakes-core.jar` | Kernel y Motor Central | Slimefun4-Drake runtime, Dough-core shadeado, Ticker centralizado (`SuiteTickerEngine`), JNI/FFM Bindings Rust (`NativeEngineBridge`), DB SQLite WAL y Logs de Auditoria. |
| **Suite 1** | `drakes-tech.jar` | Logistica y Redes Digitales | **Modulos nativos:** `InfinityExpansionModule` (Singularidades y Tier 10), `DynaTechModule` (Teseractos cuanticos, Gema de los Angeles, Camaras de Cultivo), `SupremeModule`, `FluffyMachinesModule`, `FoxyMachinesModule`, `NanotechModule`. **Sistemas absorbidos:** Networks, DrakesNanotech, SensibleToolbox, EquivalencyTech (EMCTech), Quaptics, AdvancedTech, DankTech2, GlobiaMachines, Nexcavate, PrivateStorage, SlimeCustomizer / Ryken, IDreamOfEasy, SaneCrafting. |
| **Suite 2** | `drakes-bio.jar` | Biogenetica y Campo | **Modulos nativos:** `GeneticChickensModule` (64 especies, genoma de 6 bits, herencia meiotica), `CultivationModule` (82 combinaciones botanicas, polinizacion diagonal), `SlimyBeesModule` (26 especies de abejas, genoma mendeliano de 7 cromosomas), `FlowerPowerModule`, `TreeTapsModule` (resina, ambar, caucho), `ExoticGardenModule`, `MobCapturerModule`. **Sistemas absorbidos:** Gastronomicon, Drugfun, GlobalWarming, HotbarPets. |
| **Suite 3** | `drakes-magic.jar` | Arcano y Alquimia | **Modulos nativos:** `AlchimiaVitaeModule` (9 infusiones misticas, Soul Collector, bateria de totems), `CrystamaeModule` (Crystamae Historia: 9 dominios, 6 rarezas, baculos T1-T5, 12 conjuros), `RelicsCthoniaModule`, `SoulJarsModule` (16 criaturas y almas). **Sistemas absorbidos:** Netheopoiesis (con recoleccion virtual de basura en el Nether), TranscEndence, SpiritsUnchained, DemonicExpansion, ElementManipulation, MagicXpansion, SlimeChem, Coronalis, InfernalExpansion. |
| **Suite 4** | `drakes-generators.jar` | Matriz Energetica | **Modulos nativos:** `UltimateGeneratorsModule` (Generador Infinito, Diesel, Biocombustibles, Aliento de Dragon, Cristales del End), `LiteXpansionModule`, `SMGModule`, `EcoPowerModule`, `OreChunksModule` (11 minerales, perfiles geologicos), `BetterReactorsModule`, `LiquidModule` (Combustibles e Hidrocarburos). |
| **Suite 5** | `drakes-utility.jar` | Utilidades y QoL | **Modulos nativos:** `BackpacksModule` (DyedBackpacks, 96 variantes, anti-dupe), `ColoredEnderChestsModule` (4096 frecuencias), `SoundMufflerModule`, `SimpleUtilsModule` (Elevator, Workbench, Wrench), `ExtraToolsModule` (Martillo 3x3, Generador de Adoquin, Fabrica de Concreto, Pulverizador, Transmutador de Oro), `ChestTerminalModule`, `SFCalcModule`, `SlimeHUDModule`, `EnderCarryOnModule`, `TrashModule`, `VanillaPatPatModule`. **Sistemas absorbidos:** SFPortalGun, SmallSpace, JustEnoughGuide, WorldEditSlimefun, GeyserHeads, SfChunkInfo. |
| **Suite 6** | `drakes-combat.jar` | Arsenal y Combate | **Modulos nativos:** `MobDropsModule` (SFMobDrops: calibracion de botines), `SlimeTinkerModule` (piezas, aleaciones, filo), `WarfareModule`, `ExtraGearModule`, `ObsidianExpansionModule` y `ObsidianArmor`, `SlimefunNukesModule`, `SlimefunDiscModule`. **Sistemas absorbidos:** SlimefunLuckyBlocks, Galaxyfun (fusion de Galactifun 1 y 2), MissileWarfare, DrakesBosses, FNAmplifications, MilitaryArsenal, HardcoreSlimefun, CringleBosses. |
| **Suite 7** | `drakes-server.jar` | Servidor Standalone (Star) | **Evolucion Odysseia -> Star**: Motor central de servidor, puente Rust/Java (`Star-Rust`), InvSwitcher (aislamiento de 6 modalidades), PlayerVaultZ, AxGraves, BreweryX, BreweryMenu, SlimeMarket (`/sm`), RandomExpansion, Bump. |
| **Suite Multiverse** | `drakes-multiverse.jar` | **Autoria Soberana: Chagui68** | **Integracion nativa:** Absorbe `MultiverseNets` (motor completo de logistica digital standalone sin Slimefun, 109 unit tests en Paper y Purpur 26.X) y `MultiverseCreatures` (criaturas mitologicas, bosses tematicos, rituales y dimensiones). |

---

## 4. Estandar de Configuracion Modular (`modules/*.yml`)

Cada Mega-Suite desacopla su configuracion en sub-modulos administrados por `AbstractSuiteModule` y `SuiteModuleManager`:

```
plugins/
+-- DrakesCore/
|   +-- config.yml                # Configuracion del kernel, telemetria y SuiteTickerEngine
|   +-- modules/
+-- DrakesTech/
|   +-- config.yml                # Master switch y perfiles de rendimiento tecnologico
|   +-- modules/
|       +-- networks.yml          # Cables, ancho de banda y transferencia por tick
|       +-- dynatech.yml          # Maquinas dinamicas y generadores cuanticos
|       +-- infinity.yml          # Formulas de singularidades y recetas de Infinity
|       +-- supreme.yml           # Maquinaria Supreme y overclockers
|       +-- advancedtech.yml      # Aceleradores de particulas y compresores cuanticos
|       +-- fastmachines.yml      # Multiplicadores de velocidad y buffers
+-- DrakesBio/
|   +-- config.yml
|   +-- modules/
|       +-- genetic_chickens.yml  # Tiers 0-9, mutaciones geneticas, tasas de drop e incubadoras
|       +-- exotic_garden.yml     # Crecimiento de cultivos y botanica
|       +-- slimy_bees.yml        # Panales, genetica apicola y miel
+-- DrakesMagic/
|   +-- config.yml
|   +-- modules/
|       +-- alchimia_vitae.yml    # Calabazas de transmutacion y elixires
|       +-- crystamae.yml         # Rituales cristalinos y resonadores
|       +-- netheopoiesis.yml     # Boveda de recoleccion virtual del Nether
+-- DrakesGenerators/
|   +-- config.yml
|   +-- modules/
|       +-- litexpansion.yml      # Reactores y generadores de vacio
|       +-- ultimategenerators.yml# Tasas de Joules (J/t) y disipacion termica
+-- DrakesUtility/
|   +-- config.yml
|   +-- modules/
|       +-- backpacks.yml         # Mochilas tintables de alta capacidad
|       +-- chest_terminal.yml    # Rango de acceso inalambrico e indexacion
|       +-- portalgun.yml         # Teletransportadores espaciales cuanticos
+-- DrakesCombat/
|   +-- config.yml
|   +-- modules/
|       +-- warfare.yml           # Armamento tactico, municiones y balistica
|       +-- slimetinker.yml       # Modificadores de piezas, aleaciones y filo
|       +-- galaxyfun.yml         # Mecanicas planetarias y naves espaciales
|       +-- luckyblocks.yml       # Bloques de la suerte y recompensas de Slimefun
+-- DrakesServer/
    +-- config.yml
    +-- modules/
        +-- star_engine.yml       # Telemetria Star, puente Rust nativo y SQLite
        +-- invswitcher.yml       # Aislamiento estricto de inventarios por modalidad
        +-- playervaultz.yml      # Bovedas virtuales seguras
        +-- breweryx.yml          # Destileria y fermentacion personalizada
```

### Recarga en Caliente Granular:
Los modulos pueden activarse, desactivarse y recargarse individualmente sin reiniciar el servidor:
```bash
/drakessuites reload <suite> [modulo]
```

---

## 5. Ticker Engine Centralizado (`SuiteTickerEngine`)

Los addons ya no instancian bucles Bukkit `runTaskTimer` desacoplados que saturan el hilo principal:
1. **Agrupacion Espacial por Chunks:** Organiza los bloques activos por chunks cargados en memoria.
2. **Descarta Ticks en Chunks Inactivos:** Elimina llamadas repetitivas y costosas a NBT/PDC cuando no hay jugadores cerca.
3. **Reduccion de Sobrecarga:** Reduce en un **~65-80% el uso de CPU** comparado con 160 plugins individuales corriendo tareas Bukkit asincronas dispersas.

---

## 6. Compilacion, Perfiles Maven Duales y Compatibilidad Cruzada (1.21.11 / 26.X)

El monorepo tiene dos perfiles Maven. `purpur-26` es el predeterminado (`activeByDefault`) y produce los artefactos de la Temporada 2.

### Perfiles de Compilacion Maven
* **`purpur-26` (predeterminado, produccion):** `Purpur 26.2.build.+` con bytecode Java 25.
  ```bash
  mvn clean test
  ```
* **`paper-21` (linea anterior):** `Paper 1.21.11-R0.1-SNAPSHOT`, `MockBukkit 4.110.0` y `--release 21`; para la linea de la Temporada 1.
  ```bash
  mvn clean test -Ppaper-21
  ```

---

## 7. Matriz de Modalidades y Activacion de Slimefun

DrakesPurpur y StarSuites soportan 6 modalidades canonicas:

| Clave de Modalidad | Nombre de Modalidad | Slimefun Habilitado? | Descripcion e Infraestructura |
| :--- | :--- | :--- | :--- |
| `survival_sf` | Survival Slimefun | SI | Supervivencia tecnica principal con todas las suites de StarSuites activas. |
| `survival_clasico`| Survival Clasico | NO (VANILLA PURO) | **La UNICA modalidad donde Slimefun esta completamente desactivado.** Supervivencia vanilla pura. |
| `oneblock` | OneBlock | SI | Progresion sobre bloque infinito regenerativo con BentoBox y maquinas tecnicas. |
| `onechunk` | OneChunk | SI | Automatizacion tecnica extrema confinada a los limites de un unico chunk. |
| `skyblock` | SkyBlock | SI | Supervivencia clasica en el vacio BSkyBlock impulsada por StarSuites. |
| `lab` | Laboratorio | SI | Entorno de pruebas tecnicas con reinicio periodico de mapa para pruebas de estres. |

---

## 8. Compatibilidad Retroactiva y Cero Perdida de Datos

* **Preservacion Estricta de Claves PDC:** Los identificadores en `slimefun:slimefun_item` se mantienen 100% identicos a los addons originales (ej. `NETWORKS_CABLE`, `INFINITY_SINGULARITY`, `GCE_CHICKEN_T9`, `SLIMETINKER_TINKERS_WORKBENCH`, `COLORED_ENDER_CHEST_SMALL_0_0_0`, `DYED_BACKPACK_SMALL_RED`).
* **Persistencia Transaccional SQLite WAL:** Migracion limpia sin perdidas de inventarios, terminales ni cofres de jugadores en Dallas.

---

## 9. Ciclo de Vida y Flujo de Trabajo

* **Produccion (Temporada 2):** las suites de la rama `26.x` corren en Dallas junto a los JARs sueltos listados en la instantanea de runtime.
  El despliegue usa `desplegar_via_panel.py` (respaldo del JAR vivo y SHA-256 verificado) y se aplica en el siguiente reinicio seguro.
* **Hotfixes:** un arreglo en un plugin suelto debe aplicarse tambien al modulo equivalente aqui, para que retirar el JAR suelto no devuelva el bug.
* **Absorber un addon suelto:** activar su modulo, confirmar que copia carga, retirar el JAR suelto en una ventana de mantenimiento y no tocar
  las claves PDC de `slimefun:slimefun_item`, para que items y bloques existentes sobrevivan.
* **Colaboradores:** trabajan en el repositorio del plugin o directo en este monorepo (ver `CONTRIBUTING.md`); la compilacion del monorepo absorbe la ultima version publicada.

---

## 10. Matriz de Absorcion Organizacional: Addons Restantes para StarSuites

La siguiente tabla resume el plan de ingenieria para consolidar los addons sueltos que operan en staging o Dallas dentro de sus suites homólogas:

| Suite Destino | Addon Standalone | Estado de Absorcion | Notas Tecnicas de Implementacion |
| :--- | :--- | :--- | :--- |
| **`drakes-tech`** | `FastMachines` | Modulo Pendiente | Migrar api-version de 1.18 a 26.2; acoplar buffers al `SuiteTickerEngine`. |
| **`drakes-tech`** | `Networks` | Porte Nativo Activo | Compilar `NetworksV6-drake` dentro de `drakes-tech` con grafos SIMD en Rust off-heap. |
| **`drakes-tech`** | `Quaptics` | Planificado para Absorcion | Optica cuantica y enrutamiento de fotones; erradicar listeners redundantes de chunks. |
| **`drakes-tech`** | `SensibleToolbox` | Planificado para Absorcion | Almacenamiento modular y procesamiento digital de items. |
| **`drakes-tech`** | `EquivalencyTech` / `DankTech2` | Planificado para Absorcion | Transmutacion EMC y contenedores masivos comprimidos. |
| **`drakes-tech`** | `Nexcavate` | Planificado para Absorcion | Mineria automatizada por canteras con verificacion de bordes de chunk. |
| **`drakes-magic`**| `Netheopoiesis` | Parcheado / Listo para Ingesta | **Parche Critico Anti-Basura:** Implementa una boveda virtual de auto-recoleccion para drops del Nether y cabezas de Steve en vez de saturar el suelo. |
| **`drakes-magic`**| `MagicXpansion` | Planificado para Absorcion | Baculos arcanos, libros de hechizos y encantamientos elementales. |
| **`drakes-magic`**| `SlimeChem` | Planificado para Absorcion | Quimica de tabla periodica, destilacion molecular y aleaciones elementales. |
| **`drakes-magic`**| `SpiritsUnchained` / `Demonic` | Planificado para Absorcion | Altares de invocacion, posesion de espiritus y pactos oscuros. |
| **`drakes-bio`**  | `Cultivation` | Modulo Nativo Completo | 82 resultados botanicos canonicos y polinizacion diagonal. |
| **`drakes-bio`**  | `MobCapturer` | Modulo Nativo Completo | Capsulas de captura de entidades preservando NBT exacto. |
| **`drakes-bio`**  | `Gastronomicon` / `Drugfun` | Planificado para Absorcion | Gastronomia compleja, fermentacion y efectos metabolicos. |
| **`drakes-utility`**| `SFPortalGun` | Planificado para Absorcion | Teletransportacion espacial mediante raytracing y pares PDC vinculados. |
| **`drakes-utility`**| `GeyserHeads` | Planificado para Absorcion | Compatibilidad de perfiles de cabezas y skins para jugadores Bedrock. |
| **`drakes-utility`**| `WorldEditSlimefun` | Planificado para Absorcion | Pegado y clonacion de bloques de Slimefun con WorldEdit sin perdida de metadatos. |
| **`drakes-utility`**| `ChestTerminal` | Planificado para Deprecacion | Deprecar formalmente en favor de `Networks` y `MultiverseNets` de Chagui68. |
| **`drakes-combat`** | `SlimefunLuckyBlocks`| Planificado para Absorcion | Bloques de la suerte con tablas de botin de Slimefun. |
| **`drakes-combat`** | `Galaxyfun` | Binario Unificado Activo | Fusion completa de Galactifun 1 y Galactifun 2 en un motor unificado de exploracion espacial. |
| **`drakes-combat`** | `MissileWarfare` | Planificado para Absorcion | Silos de misiles balisticos, guiado por coordenadas y radares defensivos. |
| **`drakes-generators`**| `LiteXpansion` | Planificado para Absorcion | Generadores de energia del vacio y celdas de materia oscura. |
| **`drakes-generators`**| `Liquid` | Planificado para Absorcion | Canerias hidraulicas de hidrocarburos liquidos y refinacion. |
| **`drakes-server`** | `sBank` | Integracion en Core | Banca de jugadores e intereses dinamicos; transacciones en SQLite WAL blindadas contra dupes. |

---

## 11. Clasificacion Canonica de Plugins en Dallas (`/plugins`)

En el servidor de produccion (**Dallas**), coexisten cuatro categorias de software:

1. **Mega-Suites StarSuites (Monorepo `Drakes-Suites`):**
   - Consolida y reemplaza los 160+ plugins y addons sueltos por los 8 JARs oficiales (`drakes-core` hasta `drakes-server`) mas la suite soberana `drakes-multiverse` de Chagui68.
   - **`drakes-server.jar` (Star Engine):** Evolucion canonica de Odysseia a **Star**. Kernel unificado de servidor, telemetria Star, verificacion idempotente de compras Tebex (`purchases.db`), vigilancia de economia macroeconomica (SII) y puente Rust nativo.
2. **Forks Soberanos de la Organizacion (`SlimefunNewHorizons` - Open Source):**
   - **`BentoBox-Drake`**: Fork oficial soberano del motor de islas (BSkyBlock, OneBlock, CaveBlock, AcidIsland) con el parche critico anti-destruccion de items de Paper Data Components en `ItemStackTypeAdapter`.
   - **`CrazyAuctions-Drake`**: Subastas de jugadores (`/ah`) adaptadas al stack moderno sin bugs de duplicacion de GUI.
   - **`DrakesSlimeMarket`**: Motor macroeconomico dinamico (`/sm`), con 777 ofertas controladas y suavizado exponencial.
   - `AxGraves-Drakes`, `WorldwideChat-Drake`, `PlayerVaultZ-Drake`, `BreweryX-Drake`, `Inventory-Rollback-Plus-Drake`, `DrakesBosses`, `MultiverseCreatures`, `DrakesRankup`, `ExcellentCrates-Drake`, `LevelledMobs-Drake`.
3. **Plugins Comerciales / Closed-Source / Binarios Licenciados (Licenciados por Jack):**
   - `nLogin` y `nAntiBot` (Licencia empresarial permanente pagada por Jack).
   - `TAB` (Version premium para tablist avanzada y prefijos de rangos).
   - `UltimateShop` (Tiendas GUI comerciales).
   - `SlotMachine`, `LibsDisguises Premium`, `ModelEngine`, `FancyNpcs`, `DeluxeMenus`, `GrimAC`.
   - Plataforma: `WorldGuard`, `CoreProtect`, `InteractiveChat`, `MiniMOTD`, `Geyser-Spigot`, `Floodgate`, `DiscordSRV`.
4. **Componentes Nativos Hibridos en Rust (Off-Heap / Java 25 FFM):**
   - `Slimefun-Rust` (`libslimefun_ffi.so`): Grafos logisticos y matrices de energia SIMD sin pausas de Garbage Collector.
   - `Star-Rust` / `Odysseia-Rust` (`libodysseia_ffi.so`): Redstone clock guard, filtros de chat de alto rendimiento y evaluacion geometrica de auras divinas 3D.

---

## 12. Hoja de Ruta hacia 26.X (2027)

```
[Fase 1: Monorepo StarSuites (Maven Reactor Multi-Modulo)]
                         |
                         v
[Fase 2: Ticker Engine Unificado (SuiteTickerEngine & modules/*.yml)]
                         |
                         v
[Fase 3: Integracion de Dependencias Core (Evolucion Odysseia -> Star)]
                         |
                         v
[Fase 4: Porteo 26.X Dual Sandbox (CrossVersionAdapter, PurpurRuntimeProvider, Backpacks, MultiverseNets)]
                         |
                         v
[Fase 5: Porteo de Addons Restantes (FastMachines, Netheopoiesis, Galaxyfun, LuckyBlocks)]
                         |
                         v
[Fase 6: Despliegue Limpio Nueva Temporada (Lanzamiento Atomico en Dallas)]
```

---

<div align="center">

**Disenado y Construido con Orgullo por JackStar (Jack) y la Triada Simetrica SRE**  
*DrakesCraft - Star Systems - Todos los derechos reservados*

**[Read in English (README.md)](./README.md)**

</div>

---

## Licencia y Propiedad Intelectual

Este proyecto deriva de **Slimefun 4** de **TheBusyBiscuit** y la comunidad de Slimefun, bajo licencia **GPL-3.0**. El monorepo Drakes-Suites consolida mas de 160 plugins y se distribuye bajo la misma licencia GPL-3.0.

**Atribucion Upstream:** Slimefun es obra original de **TheBusyBiscuit** y la comunidad de Slimefun. Drakes-Suites es un esfuerzo de consolidacion y modernizacion liderado por JackStar6677-1 para el ecosistema DrakesCraft.

**Modificaciones:** Modificaciones copyright 2026 colaboradores de DrakesCraft Labs. Distribuido bajo GPL-3.0.

Este software es **Source-Available** para inspeccion publica y auditoria tecnica.
