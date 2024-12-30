package com.mguhc.listeners.scenario;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class DoubleOreScenario implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material material = block.getType();
        
        ItemStack drop = null;
        int xp = 1; // Quantité d'XP similaire à celle de la redstone

        switch (material) {
            case GOLD_ORE:
                drop = new ItemStack(Material.GOLD_INGOT, 2); // 2 lingots d'or
                break;
            case DIAMOND_ORE:
                drop = new ItemStack(Material.DIAMOND, 2); // 2 diamants
                break;
            default:
                return; // Ne rien faire pour les autres blocs
        }

        // Annule les drops normaux et ajoute les drops personnalisés
        event.setCancelled(true);
        block.setType(Material.AIR); // Supprime le bloc cassé

        // Drop les items et l'expérience
        block.getWorld().dropItemNaturally(block.getLocation(), drop);
        block.getWorld().spawn(block.getLocation(), org.bukkit.entity.ExperienceOrb.class).setExperience(xp);
    }
}
