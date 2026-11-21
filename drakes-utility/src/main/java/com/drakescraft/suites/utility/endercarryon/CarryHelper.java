package com.drakescraft.suites.utility.endercarryon;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CarryHelper {

    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey("endercarryon", "speed_slowdown");
    private static final AttributeModifier ATTRIBUTE_MODIFIER = new AttributeModifier(
            MODIFIER_KEY,
            -0.5,
            AttributeModifier.Operation.ADD_SCALAR
    );

    private static final List<Material> ALLOWED_CARRY_BLOCKS = new ArrayList<>(Arrays.asList(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.ENDER_CHEST,
            Material.BARREL
    ));

    public static boolean isValidCarryAttempt(PlayerInteractEvent event, Plugin plugin) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return false;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return false;
        }
        Block target = event.getClickedBlock();
        Player player = event.getPlayer();

        return target != null &&
                isMainHandEmpty(player) &&
                player.isSneaking() &&
                isAllowedCarryTarget(target) &&
                isSafeContainerToCarry(target) &&
                !isSlimefunOrCustomTileBlock(target) &&
                canBreakBlock(player, target, plugin);
    }

    public static boolean isSafeContainerToCarry(Block block) {
        BlockState state = block.getState();
        if (state instanceof Container container) {
            if (!container.getInventory().getViewers().isEmpty()) {
                return false;
            }
            if (container.getInventory().getHolder() instanceof DoubleChest) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSlimefunOrCustomTileBlock(Block block) {
        BlockState state = block.getState();
        if (state instanceof TileState tileState && !tileState.getPersistentDataContainer().getKeys().isEmpty()) {
            return true;
        }
        try {
            Class<?> blockStorage = Class.forName("me.mrCookieSlime.Slimefun.api.BlockStorage");
            var hasBlockInfo = blockStorage.getMethod("hasBlockInfo", Location.class);
            Object result = hasBlockInfo.invoke(null, block.getLocation());
            if (Boolean.TRUE.equals(result)) {
                return true;
            }
        } catch (Throwable ignored) {
            // Slimefun not present in test or runtime
        }
        return false;
    }

    public static ItemStack getCarryBlock(Block block) {
        ItemStack item = new ItemStack(block.getType());
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof BlockStateMeta blockStateMeta) {
            int customModelData = CarryKeys.getCustomModelData(block.getType());
            blockStateMeta.setCustomModelData(customModelData);
            blockStateMeta.getPersistentDataContainer().set(CarryKeys.CARRY, PersistentDataType.BYTE, (byte) 1);
            blockStateMeta.setBlockState(block.getState());
            item.setItemMeta(blockStateMeta);
        } else if (itemMeta != null) {
            itemMeta.getPersistentDataContainer().set(CarryKeys.CARRY, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(itemMeta);
        }

        return setAttributeModifier(item, Attribute.MOVEMENT_SPEED, ATTRIBUTE_MODIFIER);
    }

    public static ItemStack getCarryEntityItem(Entity entity) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            String snapshotData = entity.createSnapshot().getAsString();

            itemMeta.setCustomModelData(CarryKeys.ENTITY_ID);
            itemMeta.getPersistentDataContainer().set(CarryKeys.CARRY, PersistentDataType.BYTE, (byte) 1);
            itemMeta.getPersistentDataContainer().set(CarryKeys.ENTITY_DATA, PersistentDataType.STRING, snapshotData);

            String name = entity.getCustomName() != null ? entity.getCustomName() : entity.getType().name();
            itemMeta.displayName(Component.text("Carrying: " + name).color(NamedTextColor.GOLD));
            item.setItemMeta(itemMeta);
        }

        return setAttributeModifier(item, Attribute.MOVEMENT_SPEED, ATTRIBUTE_MODIFIER);
    }

    public static boolean isAllowedEntity(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        if (entity instanceof Player) return false;
        
        if (entity instanceof Monster) {
            return entity.getType() == org.bukkit.entity.EntityType.ZOMBIE ||
                   entity.getType() == org.bukkit.entity.EntityType.ENDERMITE;
        }
        return true;
    }

    private static boolean isAllowedCarryTarget(Block block) {
        return ALLOWED_CARRY_BLOCKS.contains(block.getType());
    }

    private static boolean isMainHandEmpty(Player player) {
        return player.getInventory().getItemInMainHand().getType().isAir();
    }

    private static boolean canBreakBlock(Player player, Block target, Plugin plugin) {
        BlockBreakEvent breakEvent = new BlockBreakEvent(target, player);
        plugin.getServer().getPluginManager().callEvent(breakEvent);
        return !breakEvent.isCancelled();
    }

    private static ItemStack setAttributeModifier(ItemStack item, Attribute attribute, AttributeModifier modifier) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return item;

        Multimap<Attribute, AttributeModifier> itemAttributeModifiers = itemMeta.getAttributeModifiers();
        if (itemAttributeModifiers != null && itemAttributeModifiers.containsKey(attribute)) {
            ArrayListMultimap<Attribute, AttributeModifier> mutableModifiers = ArrayListMultimap.create(itemAttributeModifiers);
            mutableModifiers.put(attribute, modifier);
            itemMeta.setAttributeModifiers(mutableModifiers);
        } else {
            itemMeta.addAttributeModifier(attribute, modifier);
        }

        item.setItemMeta(itemMeta);
        return item;
    }

    private CarryHelper() {}
}
