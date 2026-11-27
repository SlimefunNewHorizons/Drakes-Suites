# Towny Modality Plan (7th Modality — Vanilla+ Geopolitical)

Design and isolation checklist for adding **Towny** as a 7th modality on Dallas (`DrakesPurpur 26.2`), completely isolated from Slimefun and other modalities.

## 1. Capacity Baseline (Verified Nov 27, 2026)

- **RAM:** `12.1 GB` used of `32 GB` (`~20 GB` headroom).
- **Disk:** `41.8 GB` used (`disk: 0` limit).
- **TPS / MSPT:** `20.0 TPS`, `~27 ms` average MSPT (`50 ms` budget).
- **Guardrail:** Keep Towny **without Slimefun** so it adds minimal tick load, **pre-generate** the map with `Chunky`, and enforce a fixed `WorldBorder` (e.g., `5,000x5,000` or `8,000x8,000`) to prevent chunk-generation MSPT spikes.

## 2. Plugin Strategy

- Use upstream `TownyAdvanced/Towny` (open source).
- Only maintain a lightweight fork (`SlimefunNewHorizons/Towny-Drake`) if `Paper/Purpur 26.2` ABI/API adjustments are required.
- Restrict Towny strictly to `towny`, `towny_nether`, and `towny_the_end` (all other worlds disabled in Towny config).

## 3. Strict Isolation & Economy Checklist

1. **`InvSwitcher` (`worlds:`):** Add `towny` (Nether/End auto-included) for isolated inventory, enderchest, XP, health, food, and per-world Vault wallet. Towny town/nation banks use Towny's own DB backed by the `towny` Vault wallet.
2. **`DrakesCore` (`SlimefunWorldFilter` + `CommandModalityGate`):** Add `towny`, `towny_nether`, `towny_the_end` alongside `clasico*` to block `/sf` and disable Slimefun items/blocks.
3. **`ProtectionStones` & `BetterTeams`:** Disable both in `towny*` so Towny chunk/plot claims and towns/nations do not conflict.
4. **Economy Integrations:** Decide whether `Jobs`, `QuickShop-Hikari`, and `sBank` operate in `towny` or if Towny runs on its own town economy + player shops.
5. **Permissions (`LuckPerms`):** Grant `towny.command.*` scoped with `world=towny`.

## 4. In-Game Setup (Spawn & UX)

- Dedicated `towny` spawn protected via `WorldGuard` / Towny SafeZone (no PvP) with quick-start holograms/NPCs (`/t new`, `/t claim`, `/t deposit`, `/n new`, `/rtp`).
- Pre-generate `towny` with `Chunky` and configure `WildRTP` for `towny`.
- Register `towny` in `Odysseia` (`%odysseia_modalidad%`, `/towny` command in Lobby) and add a dedicated `towny` scoreboard/header section in `TAB/config.yml` (Town, Nation, Balance, Ping).
- Optional web map (`BlueMap` / `Squaremap`) for live town borders.
