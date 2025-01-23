package com.mguhc.listeners.scenario;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class CutCleanScenario implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material blockType = block.getType();

        // Vérifie si le joueur utilise une pioche
        if (event.getPlayer().getInventory().getItemInHand().getType().toString().endsWith("_PICKAXE")) {
        	
        	int xp = 2; // Quantité d'XP similaire à celle de la redstone
            
            if (blockType == Material.IRON_ORE) {
                // Annule l'événement original
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.IRON_INGOT, 1));
                block.getWorld().spawn(block.getLocation(), org.bukkit.entity.ExperienceOrb.class).setExperience(xp);
            }

            if (blockType == Material.GOLD_ORE) {
                // Annule l'événement original
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLD_INGOT, 1));
                block.getWorld().spawn(block.getLocation(), org.bukkit.entity.ExperienceOrb.class).setExperience(xp);
            }

            if (blockType == Material.DIAMOND) {
                // Annule l'événement original
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.DIAMOND, 1));
                block.getWorld().spawn(block.getLocation(), org.bukkit.entity.ExperienceOrb.class).setExperience(xp);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Nourriture automatiquement cuite
        if (event.getEntity() instanceof org.bukkit.entity.Cow) {
            event.getDrops().clear(); // Retire les drops d'origine
            event.getDrops().add(new ItemStack(Material.COOKED_BEEF, 2)); // Ajoute de la viande cuite
            event.getDrops().add(new ItemStack(Material.LEATHER, 1)); // Ajoute de la viande cuite
        }
    }
}

