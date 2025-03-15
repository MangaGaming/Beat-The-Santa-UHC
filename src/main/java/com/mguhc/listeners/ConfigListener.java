package com.mguhc.listeners;

import com.mguhc.BeatTheSantaUHC;
import com.mguhc.scoreboard.UHCScoreboard;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfigListener implements Listener {
    private BeatTheSantaUHC beatTheSanta = BeatTheSantaUHC.getInstance();
    private Map<Player, String> playerInputState = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.sendMessage(ChatColor.GREEN + "Vous avez rejoint Beat the Santa UHC !");
        player.setPlayerListName(ChatColor.GREEN + player.getName() + " Lutin");

        // ScoreBoard
        UHCScoreboard uhcScoreboard = new UHCScoreboard(BeatTheSantaUHC.getInstance(), player);
        uhcScoreboard.createScoreboard(player);

        if (beatTheSanta.getPhase().equals("Waiting")) {
            BeatTheSantaUHC.clearAll(player);
            player.teleport(new Location(player.getWorld(), 0, 80, -6));
            if (player.isOp()) {
                player.getInventory().setItem(4, getConfigItem());
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 255));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 255));
        }
    }

    @EventHandler
    private void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (beatTheSanta.getPhase().equals("Playing")) {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.equals(getConfigItem())) {
            openConfigInventory(player);
        }
    }

    private void openConfigInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 18, "§9§lConfiguration");

        inventory.setItem(4, getStartItem());
        inventory.setItem(12, getMeetupItem());
        inventory.setItem(14, getSantaItem());

        player.openInventory(inventory);
    }

    @EventHandler
    private void OnConfigInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item != null && item.equals(getStartItem())) {
            beatTheSanta.startGame(player);
            player.closeInventory();
        }
        if (item != null && item.equals(getMeetupItem())) {
            beatTheSanta.enableMeetup(player);
            player.closeInventory();
        }
        if (item != null && item.equals(getSantaItem())) {
            playerInputState.put(player, "bts.setsanta");
            player.closeInventory();
            player.sendMessage("§aÉcrivez le nom du joueur que vous voulez mettre Père Noël");
        }
    }

    @EventHandler
    private void OnChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        String inputState = playerInputState.get(player);
        if (inputState != null && inputState.equals("bts.setsanta")) {
            Player target = Bukkit.getPlayer(message);
            if (target != null) {
                beatTheSanta.setSanta(target, player);
            }
            playerInputState.remove(player);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if(event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();

            if(event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
                if(beatTheSanta.getSanta() != null &&
                        beatTheSanta.getSanta().equals(p)) {
                    return;
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    private void OnConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Vérifier si l'item consommé est une Golden Apple et si le joueur a une santé maximale
        if (item.getType().equals(Material.GOLDEN_APPLE) && player.getMaxHealth() >= 40) {
            if(!beatTheSanta.getSanta().equals(player)) {
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

    private ItemStack getConfigItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("§6§lConfig");
        item.setItemMeta(itemMeta);
        return item;
    }

    public static ItemStack getStartItem() {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName("§a§lLancer"); // Définir le nom de l'item

            // Définir la texture de la tête
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFlOTc0YTI2MDhiZDZlZTU3ZjMzNDg1NjQ1ZGQ5MjJkMTZiNGEzOTc0NGViYWI0NzUzZjRkZWI0ZWY3ODIifX19"));

            // Utiliser la réflexion pour définir le profil
            try {
                Field field = itemMeta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(itemMeta, profile);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            item.setItemMeta(itemMeta); // Appliquer les modifications à l'item
        }
        return item; // Retourner l'item
    }

    public static ItemStack getMeetupItem() {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName("§7§lMeetup"); // Définir le nom de l'item

            // Définir la texture de la tête
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzBjZjc0ZTI2MzhiYTVhZDMyMjM3YTM3YjFkNzZhYTEyM2QxODU0NmU3ZWI5YTZiOTk2MWU0YmYxYzNhOTE5In19fQ"));

            // Utiliser la réflexion pour définir le profil
            try {
                Field field = itemMeta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(itemMeta, profile);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            item.setItemMeta(itemMeta); // Appliquer les modifications à l'item
        }
        return item; // Retourner l'item
    }

    public static ItemStack getSantaItem() {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName("§c§lDéfinir le Père Noël"); // Définir le nom de l'item

            // Définir la texture de la tête
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UxZjVjMDM1MDEwMGQ1NWY5NWQxNzNhZTliODQ4ODJhNTAyNmMwOTVkODhjY2E1ZjliOGU4OTM1NjJhMDZjZiJ9fX0"));

            // Utiliser la réflexion pour définir le profil
            try {
                Field field = itemMeta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(itemMeta, profile);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            item.setItemMeta(itemMeta); // Appliquer les modifications à l'item
        }
        return item; // Retourner l'item
    }

}
