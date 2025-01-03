package com.mguhc.listeners.scenario;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import com.mguhc.BeatTheSantaUHC;

public class CadeauScenario implements Listener {

	private BeatTheSantaUHC beatTheSantaUHC;

	public CadeauScenario(BeatTheSantaUHC beatTheSantaUHC) {
		this.beatTheSantaUHC = beatTheSantaUHC;
	}
		@EventHandler
		private void OnCraft(CraftItemEvent event) {
			ItemStack item = event.getCurrentItem();
			if(item.getType() == Material.GOLD_BLOCK ||
				item.getType() == Material.DIAMOND_BLOCK ||
				item.getType() == Material.EMERALD_BLOCK ||
				item.getType() == Material.GOLD_INGOT ||
				item.getType() == Material.DIAMOND ||
				item.getType() == Material.EMERALD) {
				event.setCancelled(true);
			}
			if(item.getType() == Material.GOLDEN_APPLE && item.getDurability() == 1) {
				event.setCancelled(true);
			}
		}
	
	@EventHandler
	private void OnBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if(block.getType() == Material.GOLD_BLOCK || block.getType() == Material.EMERALD_BLOCK || block.getType() == Material.DIAMOND_BLOCK) {
			if(beatTheSantaUHC.getSanta() == player) {
				player.sendMessage(ChatColor.RED + "Vous ne pouvez pas cassé vos cadeau en tant que Santa");
				event.setCancelled(true);
			}
		}
	}
}