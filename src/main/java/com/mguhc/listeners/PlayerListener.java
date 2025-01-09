package com.mguhc.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.*;
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
    private final Location chestLocation = new Location(Bukkit.getWorld("world"), 0, 108, -43);
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
        for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.WITHER_DEATH, 1.0F, 1.0F);
        }
        Player killer = event.getEntity().getKiller();
        HashMap<UUID, Integer> playerKills = beatTheSantaUHC.getplayerKills();

        if (killer != null) {
            // Incrémente le nombre de kills du killer
            UUID killerId = killer.getUniqueId();
            playerKills.put(killerId, playerKills.getOrDefault(killerId, 0) + 1);
        }

        if (player.equals(beatTheSantaUHC.getSanta())) {
            if (killer != null) {
                Bukkit.broadcastMessage("[Beat The Santa] " + ChatColor.RED + "Le Santa est mort, tué par " + killer.getName());
            }

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
        if (clickedBlock != null && santaChestExists && clickedBlock.getLocation().equals(chestLocation) && clickedBlock.getType() == Material.CHEST) {
            event.setCancelled(true); // Empêche l'ouverture du coffre

            // Vérifier si le coffre est déjà en cours de crochetage
            if (!isChestLocked && player.getGameMode().equals(GameMode.SURVIVAL)) {
                player.sendMessage(ChatColor.GOLD + "Le coffre se crochette, veuillez attendre 1 minute !");
                isChestLocked = true; // Indique que le coffre est en cours de crochetage
                currentPlayerCrocheting = player; // Enregistrer le joueur qui crochette
                final int[] timer = {0};

                // Lancer une tâche pour gérer le crochetage du coffre
                new BukkitRunnable() {
                    @Override
                    public void run() {

                        // Vérifier si le joueur est toujours dans la zone définie
                        if (isPlayerInCrochetageZone(player)) {
                            if(timer[0] == 60) {
                                this.cancel();
                                Bukkit.broadcastMessage(ChatColor.GREEN + player.getName() + " a gagné");
                                player.getWorld().getBlockAt(chestLocation).setType(Material.AIR);
                            }
                        } else {
                            // Annuler le crochetage
                            isChestLocked = false; // Réinitialiser l'état de crochetage
                            currentPlayerCrocheting = null; // Réinitialiser le joueur en cours de crochetage
                            player.sendMessage(ChatColor.RED + "Vous avez annulé le crochetage du coffre en sortant de la zone !");
                            Bukkit.broadcastMessage(ChatColor.RED + player.getName() + " a annulé le crochetage du coffre en sortant de la zone ! Il est resté" + timer[0] + "secondes");
                            this.cancel(); // Annule la tâche
                        }
                        timer[0]++;
                    }
                }.runTaskTimer(beatTheSantaUHC, 0, 5); // Vérifie chaque seconde
            }
        }
    }

    // Méthode pour vérifier si le joueur est dans la zone de crochetage
    private boolean isPlayerInCrochetageZone(Player player) {
        Location loc = player.getLocation();
        double x = loc.getX();
        double z = loc.getZ();

        // Vérifier si le joueur est dans la zone carrée définie par les coins
        return (x >= -1 && x <= 1) && (z >= -44 && z <= -42);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (BeatTheSantaUHC.getInstance().getPhase().equals("Waiting")) {
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
    private void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (beatTheSantaUHC.getPhase().equals("Playing")) {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }
}