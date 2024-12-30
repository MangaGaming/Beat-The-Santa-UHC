package com.mguhc.listeners.scenario;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class HastyBoysScenario implements Listener {

    @EventHandler
    public void onItemCraft(CraftItemEvent event) {
        ItemStack item = event.getCurrentItem();

        // Appliquer les enchantements si c'est un outil de type pioche, hache ou pelle
        if (item != null && (item.getType() == Material.STONE_PICKAXE
                || item.getType() == Material.IRON_PICKAXE || item.getType() == Material.DIAMOND_PICKAXE
                || item.getType() == Material.STONE_AXE
                || item.getType() == Material.IRON_AXE || item.getType() == Material.DIAMOND_AXE)) {
            item.addEnchantment(Enchantment.DIG_SPEED, 3); // Efficiency III
            item.addEnchantment(Enchantment.DURABILITY, 1); // Unbreaking I
        }
    }
}