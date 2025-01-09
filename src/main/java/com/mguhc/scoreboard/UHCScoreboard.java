package com.mguhc.scoreboard;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.mguhc.BeatTheSantaUHC;

public class UHCScoreboard {

    private final BeatTheSantaUHC plugin;
	@SuppressWarnings("unused")
	private Player player;

    public UHCScoreboard(BeatTheSantaUHC plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void createScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        UUID playerID = player.getUniqueId();

        // Créer l'objectif du scoreboard avec le titre Beat The Santa
        Objective objective = scoreboard.registerNewObjective("uhc", "dummy");
        objective.setDisplayName("§aBeat §fThe §cSanta");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // Créer les scores
        Score[] scores = new Score[11]; // Augmenter la taille du tableau pour inclure les Lutins et le Santa

        scores[0] = objective.getScore(ChatColor.YELLOW + "Jour : " + ChatColor.RED + getActualDayString());
        scores[0].setScore(14);
        
        scores[1] = objective.getScore(ChatColor.YELLOW + "Temps : " + ChatColor.RED + formatTime(plugin.getTimePassed()));
        scores[1].setScore(13);

        scores[2] = objective.getScore(ChatColor.GRAY + "______________");
        scores[2].setScore(12);

        scores[3] = objective.getScore(ChatColor.YELLOW + "Pvp : " + ChatColor.RED + getPVPString());
        scores[3].setScore(11);
        
        scores[4] = objective.getScore(ChatColor.YELLOW + "Bordure : " + ChatColor.RED + getBorderSizeString());
        scores[4].setScore(10);

        scores[5] = objective.getScore(ChatColor.GRAY + "_______________");
        scores[5].setScore(9);

        scores[6] = objective.getScore(ChatColor.YELLOW + "Joueurs : " + ChatColor.RED + getOnlinePlayersString());
        scores[6].setScore(8);
        
        scores[7] = objective.getScore(ChatColor.YELLOW + "Lutins : " + ChatColor.RED + getLutinString());
        scores[7].setScore(7); // Nouvelle ligne pour les Lutins

        scores[8] = objective.getScore(ChatColor.YELLOW + "Santa : " + ChatColor.RED + getSantaString());
        scores[8].setScore(6); // Nouvelle ligne pour le Santa
        
        scores[9] = objective.getScore(ChatColor.GRAY + "_________________");
        scores[9].setScore(5);

        scores[10] = objective.getScore(ChatColor.YELLOW + "Kills : " + ChatColor.RED + getKillsString(playerID));
        scores[10].setScore(4);

        // Appliquer le scoreboard initial au joueur
        player.setScoreboard(scoreboard);

        // Créer une tâche répétitive pour mettre à jour les scores
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel(); // Annuler la tâche si le joueur se déconnecte
                    return;
                }
                
                // Mettre à jour les scores dynamiques
                scoreboard.resetScores(scores[0].getEntry());
                scores[0] = objective.getScore(ChatColor.YELLOW + "Jour : " + ChatColor.RED + getActualDayString());
                scores[0].setScore(14);

                scoreboard.resetScores(scores[1].getEntry());
                scores[1] = objective.getScore(ChatColor.YELLOW + "Temps : " + ChatColor.RED + formatTime(plugin.getTimePassed()));
                scores[1].setScore(13);

                scoreboard.resetScores(scores[3].getEntry());
                scores[3] = objective.getScore(ChatColor.YELLOW + "Pvp : " + ChatColor.RED + getPVPString());
                scores[3].setScore(11);

                scoreboard.resetScores(scores[4].getEntry());
                scores[4] = objective.getScore(ChatColor.YELLOW + "Bordure : " + ChatColor.RED + getBorderSizeString());
                scores[4].setScore(10);

                scoreboard.resetScores(scores[6].getEntry());
                scores[6] = objective.getScore(ChatColor.YELLOW + "Joueurs : " + ChatColor.RED + getOnlinePlayersString());
                scores[6].setScore(8);
                
                scoreboard.resetScores(scores[7].getEntry());
                scores[7] = objective.getScore(ChatColor.YELLOW + "Lutins : " + ChatColor.RED + getLutinString());
                scores[7].setScore(7); // Mettre à jour le score des Lutins

                scoreboard.resetScores(scores[8].getEntry());
                scores[8] = objective.getScore(ChatColor.YELLOW + "Père Noël : " + ChatColor.RED + getSantaString());
                scores[8].setScore(6); // Mettre à jour le score du Santa
                
                scoreboard.resetScores(scores[9].getEntry());
                scores[9] = objective.getScore(ChatColor.YELLOW + "Kills : " + ChatColor.RED + getKillsString(playerID));
                scores[9].setScore(4);
            }
        }.runTaskTimer(plugin, 0, 20); // Met à jour toutes les secondes
    }
    
    private String getKillsString(UUID playerID) {
    	return String.valueOf(plugin.getKills(playerID));
	}

    private String getLutinString() {
        if (plugin.getSanta() == null) {
            return String.valueOf(Bukkit.getOnlinePlayers().size());
        }
        else if (plugin.getSanta().isDead()) {
            return String.valueOf(Bukkit.getOnlinePlayers().size());
        }
        else {
            return String.valueOf(Bukkit.getOnlinePlayers().size() - 1);
        }
	}

	private String getSantaString() {
		if(plugin.getSanta() == null) {
			return "Pas de père noël";
		}
    	if(!plugin.getSanta().isDead()) {
    		return "0";
    	}
    	else {
    		return "1";
    	}
	}

	private String getPVPString() {
        if (plugin.isMeetupEnabled()) {
            return "Activé";
        }
        else {
            if(plugin.getTimePassed() >= 30 * 60) {
                return "Activé";
            }
            else {
                return "Désactivé";
            }
        }
	}

	private String getActualDayString() {
		return String.valueOf(plugin.getDayNumber() / 3);
	}

	// Méthode pour obtenir la taille de la border en tant que chaîne
    private String getBorderSizeString() {
        double borderSize = Bukkit.getWorld("world").getWorldBorder().getSize();
        return String.valueOf((int) borderSize);
    }

    // Méthode pour obtenir le nombre de joueurs en ligne
    private String getOnlinePlayersString() {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        return String.valueOf(onlinePlayers);
    }
    
    private String formatTime(int time) {
        int minutes = time / 60;
        int seconds = time % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}