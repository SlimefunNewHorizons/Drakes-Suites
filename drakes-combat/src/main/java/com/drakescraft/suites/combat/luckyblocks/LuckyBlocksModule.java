package com.drakescraft.suites.combat.luckyblocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import com.drakescraft.suites.combat.luckyblocks.surprises.CustomItemSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.LuckLevel;
import com.drakescraft.suites.combat.luckyblocks.surprises.Surprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.CakeSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.CookedFoodSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.DiamondBlockPillarSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.DiamondBlockSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.EmeraldBlockSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.GoldenAppleSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.IronBlockSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.LuckyAxeSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.LuckyBootsSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.LuckyChestplateSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.LuckyHelmetSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.LuckyLeggingsSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.LuckyPickaxeSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.LuckyPotionsSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.LuckySwordSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.TamedCatsSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.TamedDogsSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.UnluckyPotionsSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.ValuablesSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.lucky.XPRainSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.ChickenRainSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.CookieSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.DyeSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.FishSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.GrootSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.HaySurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.JebSheepSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.JerrySlimeSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.PotatOSSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.RainbowSheepSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.RawFoodSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.VillagersSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.neutral.WanderingTraderSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.pandora.IronGolemsSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.pandora.ReapersSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.AnvilRainSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.BryanZombieSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.ChargedCreeperSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.CobwebSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.EnclosedWaterSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.ExplosionSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.FakeDiamondBlock;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.FlyingCreeperSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.FlyingTNTSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.GiantSlimeSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.HighJumpSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.TNTRainSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.VoidHoleSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.WalshrusSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.WitchSurprise;
import com.drakescraft.suites.combat.luckyblocks.surprises.unlucky.ZombiePigmenSurprise;
import com.drakescraft.suites.core.module.AbstractSuiteModule;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.CommonPatterns;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

/**
 * Integrated SlimefunLuckyBlocks module in DrakesCombat.
 */
public class LuckyBlocksModule extends AbstractSuiteModule implements SlimefunAddon {

    private static final String TEXTURE = "b3b710b08b523bba7efba07c629ba0895ad61126d26c86beb3845603a97426c";

    private final List<Surprise> surprises = new LinkedList<>();
    private final BlockFace[] blockfaces = {
            BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST,
            BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST
    };
    private WorldGenerator worldGenerator;

    public LuckyBlocksModule(@Nonnull JavaPlugin plugin) {
        super(plugin, "luckyblocks", "LuckyBlocks SF Mechanics");
    }

    public static boolean isStandaloneSlimefunLuckyBlocksPresent() {
        return Bukkit.getPluginManager() != null
                && Bukkit.getPluginManager().getPlugin("SlimefunLuckyBlocks") != null;
    }

    @Override
    public void onEnable() {
        surprises.clear();
        registerDefaultSurprises();
        registerCustomSurprises();

        if (isStandaloneSlimefunLuckyBlocksPresent()) {
            logInfo("Standalone plugin SlimefunLuckyBlocks detected; yielding registration to dedicated addon.");
            return;
        }
        if (Bukkit.getPluginManager() == null || Bukkit.getPluginManager().getPlugin("Slimefun") == null) {
            logInfo("Slimefun not detected; Loaded " + surprises.size() + " LuckyBlocks surprises in standalone mode.");
            return;
        }

        setupSlimefunItems();
        this.worldGenerator = new WorldGenerator(this);
        Bukkit.getPluginManager().registerEvents(this.worldGenerator, getPlugin());

        logInfo("LuckyBlocks module enabled with " + surprises.size() + " surprises.");
    }

    private void setupSlimefunItems() {
        ItemGroup itemGroup = new ItemGroup(
                new NamespacedKey(getPlugin(), "lucky_blocks"),
                new CustomItemStack(PlayerHead.getItemStack(PlayerSkin.fromHashCode(TEXTURE)), "&rLucky Blocks")
        );

        SlimefunItemStack luckyBlock = new SlimefunItemStack("LUCKY_BLOCK", TEXTURE, "&fLucky Block", "&7Luck: &f0");
        SlimefunItemStack veryLuckyBlock = new SlimefunItemStack("LUCKY_BLOCK_LUCKY", TEXTURE, "&fVery lucky Block", "&7Luck: &a+80");
        SlimefunItemStack veryUnluckyBlock = new SlimefunItemStack("LUCKY_BLOCK_UNLUCKY", TEXTURE, "&fVery unlucky Block", "&7Luck: &c-80");
        SlimefunItemStack pandorasBox = new SlimefunItemStack("PANDORAS_BOX", "86c7dde512871bd607b77e6635ad39f44f2d5b4729e60273f1b14fba9a86a", "&5Pandora\"s Box", "&7Luck: &c&oERROR");

        // @formatter:off
        new LuckyBlock(itemGroup, luckyBlock, RecipeType.ENHANCED_CRAFTING_TABLE,
        new ItemStack[] { SlimefunItems.GOLD_12K, SlimefunItems.GOLD_12K, SlimefunItems.GOLD_12K, SlimefunItems.GOLD_12K, new ItemStack(Material.DISPENSER), SlimefunItems.GOLD_12K, SlimefunItems.GOLD_12K, SlimefunItems.GOLD_12K, SlimefunItems.GOLD_12K }).register(this, surprises, s -> s.getLuckLevel() != LuckLevel.PANDORA);

        new LuckyBlock(itemGroup, veryLuckyBlock, RecipeType.ENHANCED_CRAFTING_TABLE,
        new ItemStack[] { null, SlimefunItems.GOLD_12K, null, SlimefunItems.GOLD_12K, luckyBlock, SlimefunItems.GOLD_12K, null, SlimefunItems.GOLD_12K, null }).register(this, surprises, s -> s.getLuckLevel() == LuckLevel.LUCKY);

        new LuckyBlock(itemGroup, veryUnluckyBlock, RecipeType.ENHANCED_CRAFTING_TABLE,
        new ItemStack[] { null, new ItemStack(Material.SPIDER_EYE), null, new ItemStack(Material.SPIDER_EYE), luckyBlock, new ItemStack(Material.SPIDER_EYE), null, new ItemStack(Material.SPIDER_EYE), null }).register(this, surprises, s -> s.getLuckLevel() == LuckLevel.UNLUCKY);

        new LuckyBlock(itemGroup, pandorasBox, RecipeType.ENHANCED_CRAFTING_TABLE,
        new ItemStack[] { new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.LAPIS_BLOCK), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.LAPIS_BLOCK), luckyBlock, new ItemStack(Material.LAPIS_BLOCK), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.LAPIS_BLOCK), new ItemStack(Material.OAK_PLANKS) }).register(this, surprises, s -> s.getLuckLevel() == LuckLevel.PANDORA);
        // @formatter:on
    }

    @Override
    public void onDisable() {
        if (this.worldGenerator != null) {
            HandlerList.unregisterAll(this.worldGenerator);
            this.worldGenerator = null;
        }
        surprises.clear();
    }

    private void registerDefaultSurprises() {
        // Lucky Surprises
        registerSurprise(new CookedFoodSurprise());
        registerSurprise(new GoldenAppleSurprise());
        registerSurprise(new DiamondBlockSurprise());
        registerSurprise(new DiamondBlockPillarSurprise());
        registerSurprise(new EmeraldBlockSurprise());
        registerSurprise(new IronBlockSurprise());
        registerSurprise(new TamedDogsSurprise());
        registerSurprise(new TamedCatsSurprise());
        registerSurprise(new ValuablesSurprise());
        registerSurprise(new LuckySwordSurprise());
        registerSurprise(new LuckyPickaxeSurprise());
        registerSurprise(new LuckyAxeSurprise());
        registerSurprise(new XPRainSurprise());
        registerSurprise(new LuckyHelmetSurprise());
        registerSurprise(new LuckyChestplateSurprise());
        registerSurprise(new LuckyLeggingsSurprise());
        registerSurprise(new LuckyBootsSurprise());
        registerSurprise(new LuckyPotionsSurprise());
        registerSurprise(new UnluckyPotionsSurprise());
        registerSurprise(new CakeSurprise());

        // Neutral Surprises
        registerSurprise(new GrootSurprise());
        registerSurprise(new RawFoodSurprise());
        registerSurprise(new FishSurprise());
        registerSurprise(new WanderingTraderSurprise());
        registerSurprise(new RainbowSheepSurprise());
        registerSurprise(new ChickenRainSurprise());
        registerSurprise(new DyeSurprise());
        registerSurprise(new HaySurprise());
        registerSurprise(new CookieSurprise());
        registerSurprise(new JebSheepSurprise());
        registerSurprise(new VillagersSurprise());
        registerSurprise(new PotatOSSurprise());
        registerSurprise(new JerrySlimeSurprise());

        // Unlucky Surprises
        registerSurprise(new ChargedCreeperSurprise());
        registerSurprise(new WitchSurprise());
        registerSurprise(new ExplosionSurprise());
        registerSurprise(new VoidHoleSurprise());
        registerSurprise(new AnvilRainSurprise());
        registerSurprise(new EnclosedWaterSurprise());
        registerSurprise(new TNTRainSurprise());
        registerSurprise(new FlyingCreeperSurprise());
        registerSurprise(new FlyingTNTSurprise());
        registerSurprise(new FakeDiamondBlock());
        registerSurprise(new BryanZombieSurprise());
        registerSurprise(new WalshrusSurprise());
        registerSurprise(new HighJumpSurprise());
        registerSurprise(new CobwebSurprise());
        registerSurprise(new GiantSlimeSurprise());
        registerSurprise(new ZombiePigmenSurprise());

        // Pandora Box Surprises
        registerSurprise(new ReapersSurprise());
        registerSurprise(new IronGolemsSurprise());
    }

    private void registerCustomSurprises() {
        ConfigurationSection customSec = config.getConfigurationSection("custom");
        if (customSec == null) {
            return;
        }
        for (String name : customSec.getKeys(false)) {
            LuckLevel luckLevel = LuckLevel.NEUTRAL;
            List<ItemStack> items = new ArrayList<>();
            List<String> commands = new ArrayList<>();

            String luckLevelStr = config.getString("custom." + name + ".lucklevel");
            if (luckLevelStr != null) {
                try {
                    luckLevel = LuckLevel.valueOf(luckLevelStr.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ex) {
                    getPlugin().getLogger().log(Level.WARNING, "Couldn't load lucklevel of CustomItem Surprise \"{0}\", now using NEUTRAL (default)", name);
                }
            }

            List<String> cfgCommands = config.getStringList("custom." + name + ".commands");
            if (!cfgCommands.isEmpty()) {
                commands.addAll(cfgCommands);
            }

            ConfigurationSection itemsSec = config.getConfigurationSection("custom." + name + ".items");
            if (itemsSec != null) {
                for (String itemID : itemsSec.getKeys(false)) {
                    ItemStack item = null;
                    String itemPath = "custom." + name + ".items." + itemID;

                    String sfItemId = config.getString(itemPath + ".slimefun_item");
                    String matType = config.getString(itemPath + ".type");
                    if (sfItemId != null) {
                        String id = sfItemId.toUpperCase(Locale.ROOT);
                        SlimefunItem sfItem = SlimefunItem.getById(id);

                        if (sfItem != null) {
                            item = sfItem.getItem().clone();
                            if (config.getInt(itemPath + ".amount") > 1) {
                                item.setAmount(config.getInt(itemPath + ".amount"));
                            }
                        } else {
                            getPlugin().getLogger().log(Level.WARNING, "Could not load SlimefunItem \"{0}\" to custom surprise \"{1}\"", new Object[] { id, name });
                        }
                    } else if (matType != null && Material.getMaterial(matType) != null) {
                        item = new ItemStack(Material.getMaterial(matType));
                        ItemMeta itemMeta = item.getItemMeta();

                        if (config.getInt(itemPath + ".amount") > 1) {
                            item.setAmount(config.getInt(itemPath + ".amount"));
                        }

                        if (itemMeta != null) {
                            String displayName = config.getString(itemPath + ".displayname");
                            if (displayName != null) {
                                itemMeta.setDisplayName(ChatColors.color(displayName));
                            }

                            List<String> cfgLore = config.getStringList(itemPath + ".lore");
                            if (!cfgLore.isEmpty()) {
                                List<String> lore = new ArrayList<>();
                                for (String l : cfgLore) {
                                    lore.add(ChatColors.color(l));
                                }
                                itemMeta.setLore(lore);
                            }

                            List<String> cfgEnchants = config.getStringList(itemPath + ".enchants");
                            if (!cfgEnchants.isEmpty()) {
                                for (String ench : cfgEnchants) {
                                    String[] split = ench.split(":");
                                    String enchName = split[0];
                                    Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchName.toLowerCase(Locale.ROOT)));
                                    int level = 1;

                                    if (enchantment != null) {
                                        if (split.length == 2) {
                                            if (!CommonPatterns.NUMERIC.matcher(split[1]).matches()) {
                                                continue;
                                            }
                                            level = Integer.parseInt(split[1]);
                                        }
                                        itemMeta.addEnchant(enchantment, level, true);
                                    }
                                }
                            }
                            item.setItemMeta(itemMeta);
                        }
                    }

                    if (item != null) {
                        items.add(item);
                    }
                }
            }
            if (!items.isEmpty() || !commands.isEmpty()) {
                registerSurprise(new CustomItemSurprise(name, items, commands, luckLevel));
            }
        }
    }

    public static ItemStack createPotion(Color color, PotionEffect effect, boolean lucky) {
        ItemStack potion = new ItemStack(lucky ? Material.POTION : Material.SPLASH_POTION);
        PotionMeta pm = (PotionMeta) potion.getItemMeta();
        if (pm != null) {
            pm.setDisplayName(ChatColors.color((lucky ? "&6Lucky" : "&cUnlucky") + " potion"));
            pm.setColor(color);
            pm.addCustomEffect(effect, false);
            potion.setItemMeta(pm);
        }
        return potion;
    }

    public void registerSurprise(Surprise surprise) {
        if (surprise instanceof CustomItemSurprise) {
            if (config.getBoolean("custom." + surprise.getName() + ".enabled", false)) {
                surprises.add(surprise);
            }
            return;
        }

        if (config.contains("events." + surprise.getName())) {
            if (config.getBoolean("events." + surprise.getName(), true)) {
                surprises.add(surprise);
            }
        } else {
            surprises.add(surprise);
        }
    }

    public void spawnLuckyBlock(Block b) {
        BlockData data = Material.PLAYER_HEAD.createBlockData(bd -> {
            if (bd instanceof Rotatable skull) {
                BlockFace rotation = blockfaces[ThreadLocalRandom.current().nextInt(blockfaces.length)];
                skull.setRotation(rotation);
            }
        });

        b.setBlockData(data);
        PlayerHead.setSkin(b, PlayerSkin.fromHashCode(TEXTURE), true);
        BlockStorage.store(b, "LUCKY_BLOCK");

        if (config.getBoolean("debug", false)) {
            getPlugin().getLogger().log(Level.INFO, "spawned lucky block at {0} {1} {2} - {3}",
                    new Object[] { b.getX(), b.getY(), b.getZ(), b.getWorld().getName() });
        }
    }

    public List<Surprise> getSurprises() {
        return Collections.unmodifiableList(surprises);
    }

    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return getPlugin();
    }

    @Nullable
    @Override
    public String getBugTrackerURL() {
        return "https://github.com/SlimefunNewHorizons/Drakes-Suites/issues";
    }
}
