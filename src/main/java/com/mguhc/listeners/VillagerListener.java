package com.mguhc.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class VillagerListener implements Listener {

    private List<Player> playersWithNoFall = new ArrayList<>();
    private static final String GOLD_EXCHANGE_TITLE = ChatColor.GOLD + "Échange avec bloc d'or";
    private static final String EMERALD_EXCHANGE_TITLE = ChatColor.GREEN + "Échange avec bloc d'émeraude";
    private static final String DIAMOND_EXCHANGE_TITLE = ChatColor.AQUA + "Échange avec bloc de diamant";

    public VillagerListener() {
        spawnCustomVillager();
    }

    private void spawnCustomVillager() {
        World world = Bukkit.getWorld("world");
        if (world != null) {
            Location location = new Location(world, 92, 68, 128);
            Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
            villager.setCustomName(ChatColor.GRAY + "Père Fouetard");
            villager.setCustomNameVisible(true);

            // Désactiver le mouvement en utilisant NMS pour Spigot 1.8.8
            try {
                net.minecraft.server.v1_8_R3.Entity nmsVillager = ((org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager) villager).getHandle();
                nmsVillager.getDataWatcher().watch(15, (byte) 1); // Indicateur pour désactiver l'IA
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Événement pour rendre le villageois invulnérable (si nécessaire)
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Villager) {
            Villager villager = (Villager) event.getEntity();
            if (villager.getCustomName() != null && villager.getCustomName().equals(ChatColor.GRAY + "Père Fouetard")) {
                event.setCancelled(true); // Annuler les dommages pour ce villageois
            }
        }
    }

    // Gérer l'interaction avec le villageois
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            if (villager.getCustomName() != null && villager.getCustomName().equals(ChatColor.GRAY + "Père Fouetard")) {
                event.setCancelled(true); // Annule l'interface d'échange par défaut du villageois
                Player player = event.getPlayer();
                ItemStack itemInHand = player.getItemInHand();

                if (itemInHand.getType() == Material.GOLD_BLOCK && itemInHand.getAmount() >= 25) {
                    player.getInventory().removeItem(new ItemStack(Material.GOLD_BLOCK, 25));
                    openGoldInventory(player);
                } else if (itemInHand.getType() == Material.EMERALD_BLOCK && itemInHand.getAmount() >= 25) {
                    player.getInventory().removeItem(new ItemStack(Material.EMERALD_BLOCK, 25));
                    openEmeraldInventory(player);
                } else if (itemInHand.getType() == Material.DIAMOND_BLOCK && itemInHand.getAmount() >= 25) {
                    player.getInventory().removeItem(new ItemStack(Material.DIAMOND_BLOCK, 25));
                    openDiamondInventory(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Il vous faut un bloc d'or, d'émeraude ou de diamant pour cet échange.");
                }
            }
        }
    }

    // Inventaire pour les blocs d'or
    private void openGoldInventory(Player player) {
        Inventory goldInventory = Bukkit.createInventory(null, 9, GOLD_EXCHANGE_TITLE);

        ItemStack goldenApples = new ItemStack(Material.GOLDEN_APPLE, 4);
        ItemStack regenPotion = new ItemStack(Material.POTION, 1, (short) 8229); // Potion de régénération II
        ItemStack hasteEffect = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta hasteMeta = hasteEffect.getItemMeta();
        if (hasteMeta != null) {
            hasteMeta.setDisplayName(ChatColor.YELLOW + "Effet Haste I");
            hasteEffect.setItemMeta(hasteMeta);
        }

        goldInventory.setItem(1, goldenApples);
        goldInventory.setItem(3, regenPotion);
        goldInventory.setItem(5, hasteEffect);

        player.openInventory(goldInventory);
    }

    // Inventaire pour les blocs d'émeraude
    private void openEmeraldInventory(Player player) {
        Inventory emeraldInventory = Bukkit.createInventory(null, 9, EMERALD_EXCHANGE_TITLE);

        ItemStack noFall = new ItemStack(Material.POTION);
        ItemMeta nfMeta = noFall.getItemMeta();
        if (nfMeta != null) {
            nfMeta.setDisplayName(ChatColor.GREEN + "No Fall Damage");
            noFall.setItemMeta(nfMeta);
        }

        ItemStack sharpBook = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta sharpMeta = sharpBook.getItemMeta();
        if (sharpMeta != null) {
            sharpMeta.setDisplayName(ChatColor.GREEN + "Epee Tranchant II");
            sharpBook.setItemMeta(sharpMeta);
        }

        ItemStack xpBottles = new ItemStack(Material.EXP_BOTTLE, 10);

        emeraldInventory.setItem(1, noFall);
        emeraldInventory.setItem(3, sharpBook);
        emeraldInventory.setItem(5, xpBottles);

        player.openInventory(emeraldInventory);
    }

    // Inventaire pour les blocs de diamant
    private void openDiamondInventory(Player player) {
        Inventory diamondInventory = Bukkit.createInventory(null, 9, DIAMOND_EXCHANGE_TITLE);

        ItemStack flameBook = new ItemStack(Material.BOW);
        ItemMeta flameMeta = flameBook.getItemMeta();
        if (flameMeta != null) {
            flameMeta.setDisplayName(ChatColor.AQUA + "Arc Flamme");
            flameBook.setItemMeta(flameMeta);
        }

        ItemStack fireAspectBook = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta fireMeta = fireAspectBook.getItemMeta();
        if (fireMeta != null) {
            fireMeta.setDisplayName(ChatColor.AQUA + "Epee Fire Aspect");
            fireAspectBook.setItemMeta(fireMeta);
        }

        ItemStack fireResEffect = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta fireResMeta = fireResEffect.getItemMeta();
        if (fireResMeta != null) {
            fireResMeta.setDisplayName(ChatColor.RED + "Effet de Résistance au Feu I");
            fireResEffect.setItemMeta(fireResMeta);
        }

        diamondInventory.setItem(1, flameBook);
        diamondInventory.setItem(3, fireAspectBook);
        diamondInventory.setItem(5, fireResEffect);

        player.openInventory(diamondInventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Vérifier si l'inventaire est l'un des inventaires d'échange
        if (event.getView().getTitle().equals(GOLD_EXCHANGE_TITLE)) {
            event.setCancelled(true); // Empêche le retrait des objets de l'inventaire
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if (clickedItem.getType() == Material.GOLDEN_APPLE) {
                player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 4));
            } else if (clickedItem.getType() == Material.POTION) {
                player.getInventory().addItem(new ItemStack(Material.POTION, 1, (short) 8229)); // Potion Régénération II
            } else if (clickedItem.getType() == Material.DIAMOND_PICKAXE) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0, true, false));
            }

            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Échange réussi !");
        } else if (event.getView().getTitle().equals(EMERALD_EXCHANGE_TITLE)) {
            event.setCancelled(true); // Empêche le retrait des objets de l'inventaire
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if (clickedItem.getType() == Material.POTION) {
                playersWithNoFall.add(player);
            } else if (clickedItem.getType() == Material.DIAMOND_SWORD) {
                // Créer une épée avec enchantement Tranchant II
                ItemStack sharpSword = new ItemStack(Material.DIAMOND_SWORD);
                sharpSword.addEnchantment(Enchantment.DAMAGE_ALL, 2); // Tranchant II
                player.getInventory().addItem(sharpSword);
            } else if (clickedItem.getType() == Material.EXP_BOTTLE) {
                player.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE, 10));
            }

            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Échange réussi !");
        } else if (event.getView().getTitle().equals(DIAMOND_EXCHANGE_TITLE)) {
            event.setCancelled(true); // Empêche le retrait des objets de l'inventaire
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            if (clickedItem.getType() == Material.BOW) {
                // Créer un arc avec enchantement Flamme
                ItemStack diamondFlameBow = new ItemStack(Material.BOW);
                diamondFlameBow.addEnchantment(Enchantment.ARROW_FIRE, 1); // Flamme
                player.getInventory().addItem(diamondFlameBow);
            } else if (clickedItem.getType() == Material.DIAMOND_SWORD) {
                // Créer une épée avec enchantement Tranchant II
                ItemStack diamondSharpSword = new ItemStack(Material.DIAMOND_SWORD);
                diamondSharpSword.addEnchantment(Enchantment.DAMAGE_ALL, 2); // Tranchant II
                player.getInventory().addItem(diamondSharpSword);
            } else if (clickedItem.getType() == Material.LAVA_BUCKET) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
            }

            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Échange réussi !");
        }
    }

    @EventHandler
    private void OnDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (playersWithNoFall.contains(player) &&
                    event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                event.setCancelled(true);
            }
        }
    }
}