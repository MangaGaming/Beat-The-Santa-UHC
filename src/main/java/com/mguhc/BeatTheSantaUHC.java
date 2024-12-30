package com.mguhc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.mguhc.listeners.PlayerListener;
import com.mguhc.listeners.VillagerListener;
import com.mguhc.listeners.scenario.CadeauScenario;
import com.mguhc.listeners.scenario.CutCleanScenario;
import com.mguhc.listeners.scenario.DayCycleScenario;
import com.mguhc.listeners.scenario.DoubleOreScenario;
import com.mguhc.listeners.scenario.HastyBoysScenario;
import com.mguhc.listeners.scenario.NoStoneVariantScenario;
import com.mguhc.scoreboard.UHCScoreboard;

public class BeatTheSantaUHC extends JavaPlugin implements Listener {
    
    private static BeatTheSantaUHC instance;
    private List<Player> players = new ArrayList<>();
    private int timepassed = 0;
    private int dayNumber = 0;
    private Player santa;
    public HashMap<UUID, Integer> playerKills = new HashMap<>();
    private boolean meetupEnabled = false; // Variable pour suivre si le mode meetup est activé
    private String currentPhase = "Waiting";

    @Override
    public void onEnable() {
        instance = this;
        for (World world : Bukkit.getWorlds()) {
            world.setGameRuleValue("announceAdvancements", "false");
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setDifficulty(Difficulty.NORMAL);
        }
        
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Beat the Santa UHC plugin activé !");
        getServer().getPluginManager().registerEvents(new VillagerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Scenario
        getServer().getPluginManager().registerEvents(new CutCleanScenario(), this);
        getServer().getPluginManager().registerEvents(new HastyBoysScenario(), this);
        getServer().getPluginManager().registerEvents(new NoStoneVariantScenario(this), this);
        getServer().getPluginManager().registerEvents(new DoubleOreScenario(), this);
        getServer().getPluginManager().registerEvents(new CadeauScenario(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Beat the Santa UHC plugin désactivé !");
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if(event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            
            if(event.getRegainReason() == RegainReason.SATIATED || event.getRegainReason() == RegainReason.REGEN) {
                if(getSanta() != null &&
                	getSanta().equals(p)) {
                    return;
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        players.add(player);
        
        player.sendMessage(ChatColor.GREEN + "Vous avez rejoint Beat the Santa UHC !");
        player.setPlayerListName(ChatColor.GREEN + player.getName() + " Lutin");
        
        // ScoreBoard
        UHCScoreboard uhcScoreboard = new UHCScoreboard(this, player);
        uhcScoreboard.createScoreboard(player);
        
        // Retirer tous les effets de potion
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        // Vider l'inventaire du joueur
        player.getInventory().clear();

        // Rétablir la santé maximale et la saturation
        player.setMaxHealth(20);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20); // Saturation maximale
        player.setSaturation(20f); // Saturation max
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args[0].equalsIgnoreCase("/start")) {
        	currentPhase = "Playing";
            event.setCancelled(true); // Annule la commande par défaut
            new DayCycleScenario(this);
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    timepassed++;
                }
            }.runTaskTimer(this, 0L, 20L); // 20L = 1 seconde

            // Vérifier si "Sacha_legrandil" est présent dans les joueurs
            for (Player player : players) {
                if (player.getName().equals("Sacha_legrandgil")) {
                    santa = player;
                    break;
                }
            }

            // Si "Sacha_legrandil" n'est pas présent, afficher un message d'erreur
            if (santa == null) {
                event.getPlayer().sendMessage(ChatColor.RED + "Le joueur 'Sacha_legrandgil' doit être présent pour commencer !");
                return;
            }

            // Attribuer les capacités au Père Noël
            giveSantaAbilities(santa);
            santa.setPlayerListName(ChatColor.RED + santa.getName() + " Père Noël");

            // Attribuer les effets et le nom dans le tab aux Lutins
            for (Player player : players) {
                if (!player.equals(santa)) {
                    teleportPlayerToRandomLocation(player);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.setHealth(player.getMaxHealth());
                            giveElfAbilities(player);
                            player.setPlayerListName(ChatColor.GREEN + player.getName() + " Lutin");
                        }
                    }.runTaskLater(this, 20*20);
                }
            }
            // TP le Père Noël à (0, 100, 0)
            santa.teleport(new Location(santa.getWorld(), -41, 83, 71));
            
        } else if (args[0].equalsIgnoreCase("/meetup")) {
            meetupEnabled = !meetupEnabled; // Active ou désactive le mode meetup
            String status = meetupEnabled ? "activé" : "désactivé";
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Le mode meetup est maintenant " + status + " !");
        }
    }

    private void teleportPlayerToRandomLocation(Player player) {
        Random random = new Random();
        int x = random.nextInt(201) + 400; // Coordonnée X aléatoire entre 400 et 600
        int z = random.nextInt(201) + 400; // Coordonnée Z aléatoire entre 400 et 600
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 255, 10*20));
        player.teleport(new Location(player.getWorld(), x, 100, z));
    }

    private void giveSantaAbilities(Player santa) {
        // Attribuer 2 barres de cœurs
        santa.setMaxHealth(40);
        santa.setHealth(40); 
        // Attribuer les effets
        santa.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
        santa.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));
        santa.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
        santa.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0));
        santa.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
        // Attribuer le stuff de départ
        if (meetupEnabled) {
            giveMeetupGear(santa);
        } else {
            giveSantaGear(santa);
        }
    }

    private void giveElfAbilities(Player elf) {
        Random random = new Random();

        // Chance de recevoir 20 cœurs (40 points de vie)
        if (random.nextInt(100) < 15) { // 15% de chances d'obtenir 20 cœurs
            elf.setMaxHealth(40.0); // 20 cœurs
            elf.setHealth(40.0); // Remplit les 20 cœurs
        } else {
            // Sinon, attribuer un effet aléatoire
            PotionEffectType[] effects = {
                PotionEffectType.DAMAGE_RESISTANCE,
                PotionEffectType.INCREASE_DAMAGE ,
                PotionEffectType.SPEED,
            };
            PotionEffectType effect = effects[random.nextInt(effects.length)];
            elf.addPotionEffect(new PotionEffect(effect, Integer.MAX_VALUE, 0));
        }

        // Attribuer le stuff de départ
        if (meetupEnabled) {
            giveMeetupGear(elf);
        } else {
            giveElfGear(elf);
        }
    }

    private void giveMeetupGear(Player elf) {
        // Casque en fer P3
        ItemStack ironHelmet = new ItemStack(Material.DIAMOND_HELMET);
        ItemMeta helmetMeta = ironHelmet.getItemMeta();
        helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true); // Protection 3
        ironHelmet.setItemMeta(helmetMeta);
        elf.getInventory().addItem(ironHelmet);

        // Plastron en diams P2
        ItemStack diamondChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta chestplateMeta = diamondChestplate.getItemMeta();
        chestplateMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true); // Protection 2
        diamondChestplate.setItemMeta(chestplateMeta);
        elf.getInventory().addItem(diamondChestplate);

        // Pantalon en fer P3
        ItemStack ironLeggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemMeta leggingsMeta = ironLeggings.getItemMeta();
        leggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true); // Protection 3
        ironLeggings.setItemMeta(leggingsMeta);
        elf.getInventory().addItem(ironLeggings);

        // Bottes en diams P2
        ItemStack diamondBoots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemMeta bootsMeta = diamondBoots.getItemMeta();
        bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true); // Protection 2
        diamondBoots.setItemMeta(bootsMeta);
        elf.getInventory().addItem(diamondBoots);

        // Épée en diams T3
        ItemStack diamondSword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta swordMeta = diamondSword.getItemMeta();
        swordMeta.addEnchant(Enchantment.DAMAGE_ALL, 3, true); // Tranchant 3
        diamondSword.setItemMeta(swordMeta);
        elf.getInventory().addItem(diamondSword);

        // 12 Golden Apples
        elf.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 12));

        // 64 Steak
        elf.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64));

        // Un seau d'eau
        elf.getInventory().addItem(new ItemStack(Material.WATER_BUCKET, 1));

        // Pioche en diams Efficacité 3
        ItemStack diamondPickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta pickaxeMeta = diamondPickaxe.getItemMeta();
        pickaxeMeta.addEnchant(Enchantment.DIG_SPEED, 3, true); // Efficacité 3
        diamondPickaxe.setItemMeta(pickaxeMeta);
        elf.getInventory().addItem(diamondPickaxe);
    }

    private void giveSantaGear(Player santa) {
        // Stuff de départ pour le Père Noël
        santa.getInventory().addItem(new ItemStack(Material.IRON_HELMET)); // Casque en fer
        santa.getInventory().addItem(new ItemStack(Material.DIAMOND_CHESTPLATE)); // Plastron en diamants
        santa.getInventory().addItem(new ItemStack(Material.IRON_LEGGINGS)); // Pantalon en fer
        santa.getInventory().addItem(new ItemStack(Material.IRON_BOOTS)); // Bottes en fer
        
        // Pioche en diams Efficacité 3
        ItemStack diamondPickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta pickaxeMeta = diamondPickaxe.getItemMeta();
        pickaxeMeta.addEnchant(Enchantment.DIG_SPEED, 3, true); // Efficacité 3
        diamondPickaxe.setItemMeta(pickaxeMeta);
        santa.getInventory().addItem(diamondPickaxe);
        
        santa.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 10)); // 10 pommes rouges
        santa.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64)); // 64 steaks
        santa.getInventory().addItem(new ItemStack(Material.BOOK, 7)); // 7 livres
    }

    private void giveElfGear(Player elf) {
        // Stuff de départ pour les Lutins
        elf.getInventory().addItem(new ItemStack(Material.IRON_HELMET)); // Casque en fer
        elf.getInventory().addItem(new ItemStack(Material.IRON_CHESTPLATE)); // Plastron en fer
        elf.getInventory().addItem(new ItemStack(Material.IRON_LEGGINGS)); // Pantalon en fer
        elf.getInventory().addItem(new ItemStack(Material.IRON_BOOTS)); // Bottes en fer
        elf.getInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE, 1));
        elf.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64)); // 64 steaks
        elf.getInventory().addItem(new ItemStack(Material.BOOK, 3)); // 3 livres
        elf.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 10)); // 10 pommes rouges
    }
    
    @EventHandler
    private void OnConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Vérifier si l'item consommé est une Golden Apple et si le joueur a une santé maximale
        if (item.getType().equals(Material.GOLDEN_APPLE) && player.getMaxHealth() >= 40) {
        	if(!getSanta().equals(player)) {
                event.setCancelled(true); // Annuler la consommation de la Golden Apple

                // Ajouter des effets de potion
            	player.removePotionEffect(PotionEffectType.ABSORPTION);
            	player.removePotionEffect(PotionEffectType.REGENERATION);
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2 * 60 * 20, 1)); // 2 minutes d'absorption
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 9 * 20, 1)); // 9 secondes de régénération
                player.setSaturation(player.getSaturation() + 4);

                // Enlever une Golden Apple de l'inventaire du joueur
                ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE, 1);
                player.getInventory().removeItem(goldenApple); // Enlever une Golden Apple
        	}
        }
    }

    public int getTimePassed() {
        return timepassed;
    }

    public Player getSanta() {
        return santa;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int n) {
        dayNumber = n;
    }

    public HashMap<UUID, Integer> getplayerKills() {
        return playerKills;
    }

    public int getKills(UUID playerId) {
        return playerKills.getOrDefault(playerId, 0);
    }
    
    public String getPhase() {
    	return currentPhase;
    }

    public static BeatTheSantaUHC getInstance() {
        return instance;
    }
}