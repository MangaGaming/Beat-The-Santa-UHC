package com.mguhc.listeners.scenario;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class NoStoneVariantScenario implements Listener {

    @SuppressWarnings("unused")
	private JavaPlugin plugin;

    public NoStoneVariantScenario(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
	@EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Vérifie si le bloc est de la pierre avec les data values spécifiques
        Material blockType = event.getBlock().getType();
        byte data = event.getBlock().getData(); // Récupère la data value du bloc

        if (blockType == Material.STONE && (data == 1 || data == 3 || data == 5)) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            // Ajouter la cobblestone
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.COBBLESTONE));
        }
    }
}
