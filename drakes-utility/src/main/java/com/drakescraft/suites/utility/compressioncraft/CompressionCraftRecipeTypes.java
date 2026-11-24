package com.drakescraft.suites.utility.compressioncraft;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import com.drakescraft.suites.utility.DrakesUtilityPlugin;
import org.bukkit.NamespacedKey;

public class CompressionCraftRecipeTypes {

    private CompressionCraftRecipeTypes() {}

    public static final RecipeType ItemCompression = new RecipeType(new NamespacedKey(DrakesUtilityPlugin.getInstance(), "ITEMCOMPRESSION"),new SlimefunItemStack("ITEMCOMPRESSION",CompressionCraftItems.JC_COMPRESSOR_1,"&fItem Compression","","&fused in the","&fCompressor and Decompressor"));
}
