package com.github.k4runio.simpleBanHammer;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class SimpleBanHammer extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage("[SimpleBanHammer] §aSimpleBanHammer plugin loaded successfully!");
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getDataFolder().mkdirs();
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                assert in != null;
                try (Reader ignored = new InputStreamReader(in)) {
                    Path path = configFile.toPath();
                    Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("[SimpleBanHammer] §cSimpleBanHammer plugin disabled!");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null || event.getEntity().getKiller().getInventory().getItemInMainHand().getType() != Material.MACE) {
            return;
        }

        ItemStack item = event.getEntity().getKiller().getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§c§lBan Hammer")) {
            if (event.getEntity() instanceof org.bukkit.entity.Player killedPlayer) {
                org.bukkit.entity.Player killerPlayer = event.getEntity().getKiller();

                String ipAddress = Objects.requireNonNull(killedPlayer.getAddress()).getAddress().getHostAddress();
                getServer().getBanList(org.bukkit.BanList.Type.IP).addBan(ipAddress, getConfig().getString("ban-reason"), null, null);

                killedPlayer.sendMessage(Objects.requireNonNull(getConfig().getString("ban-message")));
                killedPlayer.kickPlayer(getConfig().getString("ban-message"));

                getServer().broadcastMessage(Objects.requireNonNull(getConfig().getString("broadcast-message")).replace("{player_name}", killedPlayer.getName()));
            }
        }
    }
    public static ItemStack createBanHammer() {
        ItemStack banHammer = new ItemStack(Material.MACE);
        ItemMeta meta = banHammer.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c§lBan Hammer");
            meta.addEnchant(Enchantment.SHARPNESS, 255, true);
            banHammer.setItemMeta(meta);
        }
        return banHammer;
    }

    @Override
    public boolean onCommand(org.bukkit.command.@NotNull CommandSender sender, org.bukkit.command.Command command, @NotNull String label, String @NotNull [] args) {
        if (command.getName().equalsIgnoreCase("banhammer")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("hammer")) {
                if (sender instanceof org.bukkit.entity.Player player) {
                    player.getInventory().addItem(createBanHammer());
                    player.sendMessage("§cYou have been given the Ban Hammer!");
                } else {
                    sender.sendMessage("Only players can use this command.");
                }
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("simplebanhammer.reload")) {
                    reloadPlugin();
                } else {
                    sender.sendMessage("§cYou don't have permission to reload the plugin.");
                }
                return true;
            }

            sender.sendMessage("§c§l/banhammer hammer - Get your Ban Hammer");
            sender.sendMessage("§c§l/banhammer reload - Reload plugin");
            return false;
        }
        return false;
    }

    private void reloadPlugin() {
        reloadConfig();
        getServer().broadcastMessage("§cSimpleBanHammer plugin has been reloaded!");
    }
}
