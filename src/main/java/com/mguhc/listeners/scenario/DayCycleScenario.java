package com.mguhc.listeners.scenario;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.mguhc.BeatTheSantaUHC;

public class DayCycleScenario implements Listener {

    private boolean isDay = true;
    private BeatTheSantaUHC beatthesantauhc;

    public DayCycleScenario(BeatTheSantaUHC beatthesantauhc) {
        this.beatthesantauhc = beatthesantauhc;
        startDayNightCycle();
    }

    private void startDayNightCycle() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (isDay) {
                        world.setTime(0); // Définit l'heure au début du jour
                        Bukkit.broadcastMessage(ChatColor.YELLOW + "Le jour commence !");
                        // Incrémente le nombre de jours uniquement lorsque le jour commence
                        beatthesantauhc.setDayNumber(beatthesantauhc.getDayNumber() + 1);
                    } else {
                        world.setTime(13000); // Définit l'heure au début de la nuit
                        Bukkit.broadcastMessage(ChatColor.BLUE + "La nuit tombe !");
                    }
                }
                isDay = !isDay; // Alterne entre le jour et la nuit pour le prochain cycle
            }
        }.runTaskTimer(beatthesantauhc, 0, 1800*20); // Exécute toutes les 30 minutes
    }
}