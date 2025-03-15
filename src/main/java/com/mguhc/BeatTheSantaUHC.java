package com.mguhc;

import java.util.*;

import com.mguhc.listeners.ConfigListener;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.mguhc.listeners.CoffreListener;
import com.mguhc.listeners.VillagerListener;
import com.mguhc.listeners.scenario.CadeauScenario;
import com.mguhc.listeners.scenario.CutCleanScenario;
import com.mguhc.listeners.scenario.DayCycleScenario;
import com.mguhc.listeners.scenario.HastyBoysScenario;
import com.mguhc.listeners.scenario.NoStoneVariantScenario;

public class BeatTheSantaUHC extends JavaPlugin implements Listener {
    
    private static BeatTheSantaUHC instance;
    private int timepassed = 0;
    private int dayNumber = 0;
    private Player santa;
    public HashMap<UUID, Integer> playerKills = new HashMap<>();
    private boolean meetupEnabled = false; // Variable pour suivre si le mode meetup est activé
    private String currentPhase = "Waiting";
    private Player selectedSanta;

    public void onEnable() {
        instance = this;
        for (World world : Bukkit.getWorlds()) {
            world.setGameRuleValue("announceAdvancements", "false");
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setDifficulty(Difficulty.NORMAL);
        }
        getServer().getPluginManager().registerEvents(new ConfigListener(), this);
        getServer().getPluginManager().registerEvents(new VillagerListener(), this);
        getServer().getPluginManager().registerEvents(new CoffreListener(this), this);
        
        // Scenario
        getServer().getPluginManager().registerEvents(new CutCleanScenario(), this);
        getServer().getPluginManager().registerEvents(new HastyBoysScenario(), this);
        getServer().getPluginManager().registerEvents(new NoStoneVariantScenario(this), this);
        getServer().getPluginManager().registerEvents(new CadeauScenario(this), this);
    }

    public void setSanta(Player target, Player player) {
        if (target != null) {
            selectedSanta = target;
            player.sendMessage(ChatColor.GREEN + "Le Père Noël a été sélectionné !");
        }
    }

    public void enableMeetup(Player player) {
        meetupEnabled = !meetupEnabled; // Active ou désactive le mode meetup
        String status = meetupEnabled ? "activé" : "désactivé";
        player.sendMessage(ChatColor.YELLOW + "Le mode meetup est maintenant " + status + " !");
    }

    public void startGame(Player player) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        // Convertir la collection en liste
        List<Player> playerList = new ArrayList<>(players);

        // Vérifier si la liste des joueurs n'est pas vide
        if (!playerList.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(playerList.size() + 1); // Choisit un index aléatoire
            if (selectedSanta == null) {
                santa = playerList.get(randomIndex); // Attribue le rôle de Santa à un joueur aléatoire
            }
            else {
                santa = selectedSanta;
            }
        }

        currentPhase = "Playing";
        Bukkit.getWorld("world").setTime(0);
        new DayCycleScenario(this);
        dayNumber = 0;

        new BukkitRunnable() {
            @Override
            public void run() {
                timepassed++;
            }
        }.runTaskTimer(this, 0L, 20L); // 20L = 1 seconde

        // Attribuer les capacités au Père Noël
        clearAll(player);
        giveSantaAbilities(santa);
        santa.setPlayerListName(ChatColor.RED + santa.getName() + " Père Noël");

        // Attribuer les effets et le nom dans le tab aux Lutins
        for (Player p : playerList) {
            if (!p.equals(santa)) {
                clearAll(p);
                teleportPlayerToRandomLocation(p);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        giveElfAbilities(p);
                        p.setPlayerListName(ChatColor.GREEN + p.getName() + " Lutin");
                    }
                }.runTaskLater(this, 20*20);
            }
        }
        santa.teleport(new Location(santa.getWorld(), -41, 83, 71));
    }

    public static void clearAll(Player player) {
        player.getInventory().clear();
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            player.removePotionEffect(potionEffect.getType());
        }
        player.setMaxHealth(20);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().setArmorContents(null);
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

    private void giveMeetupGear(Player player) {
        // Casque en fer P3
        ItemStack ironHelmet = new ItemStack(Material.DIAMOND_HELMET);
        ItemMeta helmetMeta = ironHelmet.getItemMeta();
        helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true); // Protection 3
        ironHelmet.setItemMeta(helmetMeta);
        player.getInventory().addItem(ironHelmet);

        // Plastron en diams P2
        ItemStack diamondChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta chestplateMeta = diamondChestplate.getItemMeta();
        chestplateMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true); // Protection 2
        diamondChestplate.setItemMeta(chestplateMeta);
        player.getInventory().addItem(diamondChestplate);

        // Pantalon en fer P3
        ItemStack ironLeggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemMeta leggingsMeta = ironLeggings.getItemMeta();
        leggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true); // Protection 3
        ironLeggings.setItemMeta(leggingsMeta);
        player.getInventory().addItem(ironLeggings);

        // Bottes en diams P2
        ItemStack diamondBoots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemMeta bootsMeta = diamondBoots.getItemMeta();
        bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true); // Protection 2
        diamondBoots.setItemMeta(bootsMeta);
        player.getInventory().addItem(diamondBoots);

        // Épée en diams T3
        ItemStack diamondSword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta swordMeta = diamondSword.getItemMeta();
        swordMeta.addEnchant(Enchantment.DAMAGE_ALL, 3, true); // Tranchant 3
        diamondSword.setItemMeta(swordMeta);
        player.getInventory().addItem(diamondSword);

        // 12 Golden Apples
        player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 12));

        // 64 Steak
        player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64));

        // Un seau d'eau
        player.getInventory().addItem(new ItemStack(Material.WATER_BUCKET, 1));

        // Pioche en diams Efficacité 3
        ItemStack diamondPickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta pickaxeMeta = diamondPickaxe.getItemMeta();
        pickaxeMeta.addEnchant(Enchantment.DIG_SPEED, 3, true); // Efficacité 3
        diamondPickaxe.setItemMeta(pickaxeMeta);
        player.getInventory().addItem(diamondPickaxe);

        player.getInventory().addItem(new ItemStack(Material.WOOD, 1028));

        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.addEnchant(Enchantment.ARROW_DAMAGE, 3, true);
        bow.setItemMeta(bowMeta);
        player.getInventory().addItem(bow);
        player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
    }

    private void giveSantaGear(Player santa) {;
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
        // Pioche en diams Efficacité 3
        ItemStack diamondPickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta pickaxeMeta = diamondPickaxe.getItemMeta();
        pickaxeMeta.addEnchant(Enchantment.DIG_SPEED, 3, true); // Efficacité 3
        diamondPickaxe.setItemMeta(pickaxeMeta);
        elf.getInventory().addItem(diamondPickaxe);

        elf.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64)); // 64 steaks
        elf.getInventory().addItem(new ItemStack(Material.BOOK, 3)); // 3 livres
        elf.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 10)); // 10 pommes rouges
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

    public boolean isMeetupEnabled() {
        return meetupEnabled;
    }
}