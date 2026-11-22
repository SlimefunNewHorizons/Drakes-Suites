package com.drakescraft.suites.server.compensation;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Holder dedicado para identificar el Cofre Virtual de Devoluciones (/reclamar)
 * y conservar cualquier overflow (>54 stacks) de forma atómica al cerrar.
 */
public final class CompensationChestHolder implements InventoryHolder {

    private final UUID ownerId;
    private final String ownerName;
    private final int initialDisplayedStacks;
    private final List<ItemStack> overflowStacks;
    private Inventory inventory;

    public CompensationChestHolder(
        @Nonnull UUID ownerId,
        @Nonnull String ownerName,
        int initialDisplayedStacks,
        @Nonnull List<ItemStack> overflowStacks
    ) {
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.initialDisplayedStacks = initialDisplayedStacks;
        this.overflowStacks = new ArrayList<>(overflowStacks);
    }

    public void setInventory(@Nonnull Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Nonnull
    public UUID getOwnerId() {
        return ownerId;
    }

    @Nonnull
    public String getOwnerName() {
        return ownerName;
    }

    public int getInitialDisplayedStacks() {
        return initialDisplayedStacks;
    }

    @Nonnull
    public List<ItemStack> getOverflowStacks() {
        return overflowStacks;
    }
}
