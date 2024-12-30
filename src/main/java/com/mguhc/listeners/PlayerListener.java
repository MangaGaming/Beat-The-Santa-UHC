package com.mguhc.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import com.mguhc.BeatTheSantaUHC;

public class PlayerListener implements Listener {
    
    private final BeatTheSantaUHC beatTheSantaUHC;
    private final Location chestLocation = new Location(Bukkit.getWorld("world"), -41, 111, -14);
    private Player currentPlayerCrocheting;
    private boolean santaChestExists = false;
	private boolean isChestLocked = false;

    public PlayerListener(BeatTheSantaUHC beatTheSantaUHC) {
        this.beatTheSantaUHC = beatTheSantaUHC;
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null); // Désactive le message de mort
        Player player = event.getEntity();
        Player killer = event.getEntity().getKiller();
        HashMap<UUID, Integer> playerKills = beatTheSantaUHC.getplayerKills();
        
        if (killer != null) {
            // Incrémente le nombre de kills du killer
            UUID killerId = killer.getUniqueId();
            playerKills.put(killerId, playerKills.getOrDefault(killerId, 0) + 1);
        }

        if (player == beatTheSantaUHC.getSanta()) {
            Bukkit.broadcastMessage("[Beat The Santa] " + ChatColor.RED + "Le Santa est mort, tué par " + killer.getName());
            
            // Placer un coffre aux coordonnées spécifiques
            chestLocation.getBlock().setType(Material.CHEST);
            santaChestExists = true;
            
            // Ajouter des items dans le coffre (optionnel)
            Chest chest = (Chest) chestLocation.getBlock().getState();
            chest.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 5));
        } else {
            Bukkit.broadcastMessage("[Beat The Santa] " + ChatColor.RED + "Le joueur " + player.getName() + " a été tué par " + (killer != null ? killer.getName() : "quelqu'un d'autre"));
        }
        
        // Vérifier si le joueur qui est mort était en train de crocheter le coffre
        if (player.equals(currentPlayerCrocheting)) {
            isChestLocked = false; // Annuler le crochetage
            currentPlayerCrocheting = null; // Réinitialiser le joueur en cours de crochetage
            player.sendMessage(ChatColor.RED + "Vous êtes mort, le crochetage du coffre a été annulé.");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        // Vérifier si le joueur clique droit sur le coffre
        if (santaChestExists && clickedBlock != null && clickedBlock.getLocation().equals(chestLocation) && clickedBlock.getType() == Material.CHEST) {
            event.setCancelled(true); // Empêche l'ouverture du coffre

            // Vérifier si le coffre est déjà en cours de crochetage
            if (!isChestLocked &&
            	player.getGameMode().equals(GameMode.SURVIVAL)) {
                player.sendMessage(ChatColor.GOLD + "Le coffre se crochette, veuillez attendre 1 minute !");
                isChestLocked = true; // Indique que le coffre est en cours de crochetage

                // Lancer une tâche pour gérer le crochetage du coffre
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendMessage(ChatColor.GREEN + "Le coffre a été crocheté ! Vous avez Gagné.");
                        Bukkit.broadcastMessage("Victoire de " + player.getName());
                        player.getWorld().getBlockAt(chestLocation).setType(Material.AIR);
                        isChestLocked = false; // Le coffre peut maintenant être ouvert
                        this.cancel(); // Annule la tâche
                    }
                }.runTaskLater(beatTheSantaUHC, 60 * 20); // 60 secondes en ticks (1 minute)
            } else {
                player.sendMessage(ChatColor.RED + "Le coffre est encore en cours de crochetage, veuillez patienter.");
            }
        }
    }
    
    @EventHandler
    private void OnJoin(PlayerJoinEvent event) {
    	Player player = event.getPlayer();
    	if(BeatTheSantaUHC.getInstance().getPhase().equals("Playing")) {
    		player.setGameMode(GameMode.SPECTATOR);
    	}
    	else if(BeatTheSantaUHC.getInstance().getPhase().equals("Waiting")) {
    		player.setMaxHealth(20);
            player.setHealth(20);
            player.setSaturation(20);
            player.getInventory().clear();
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().setArmorContents(null);
            player.getInventory().clear();
    	}
    }
    
    @EventHandler
    private void OnRespawn(PlayerRespawnEvent event) {
    	Player player = event.getPlayer();
    	if(beatTheSantaUHC.getPhase().equals("Playing")) {
    		player.setGameMode(GameMode.SPECTATOR);
    	}
    }
}