package com.example.manhunt;

import com.example.manhunt.menu.MainMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ManhuntCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public ManhuntCommand(@NotNull Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Sadece oyuncular kullanabilir!");
            return true;
        }

        Player player = (Player) sender;

        // Hunter Chat (Takım Sohbeti)
        if (label.equalsIgnoreCase("hc")) {
            if (plugin.getGameManager().isHunter(player)) {
                if (args.length == 0) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /hc <mesaj>");
                    return true;
                }
                String msg = String.join(" ", args);
                String formatted = ChatColor.GOLD + "[AVCI-CHAT] " + ChatColor.YELLOW + player.getName() + ": " + ChatColor.WHITE + msg;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getGameManager().isHunter(p)) p.sendMessage(formatted);
                }
            } else {
                player.sendMessage(ChatColor.RED + "Sadece avcılar takım sohbetini kullanabilir!");
            }
            return true;
        }

        // Manhunt Track (Runner takibi)
        if (args.length > 0 && args[0].equalsIgnoreCase("track")) {
            if (plugin.getGameManager().isRunner(player)) {
                if (args.length < 2) {
                    plugin.getMessageManager().sendMessage(player, "usage_track");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !plugin.getGameManager().isHunter(target)) {
                    plugin.getMessageManager().sendMessage(player, "hunter_not_found");
                    return true;
                }
                plugin.getCompassManager().setRunnerTrackTarget(target.getUniqueId());
                plugin.getMessageManager().sendMessage(player, "tracking_hunter", target.getName());
                return true;
            } else {
                plugin.getMessageManager().sendMessage(player, "only_runner_track");
                return true;
            }
        }

        // Manhunt Donate (Avcılar arası eşya yardımı)
        if (args.length > 0 && args[0].equalsIgnoreCase("donate")) {
            if (plugin.getGameManager().isHunter(player)) {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /manhunt donate <oyuncu>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !plugin.getGameManager().isHunter(target)) {
                    player.sendMessage(ChatColor.RED + "Eşyayı sadece başka bir avcıya gönderebilirsin!");
                    return true;
                }
                if (target.equals(player)) {
                    player.sendMessage(ChatColor.RED + "Kendine eşya gönderemezsin!");
                    return true;
                }
                
                org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
                if (item == null || item.getType() == org.bukkit.Material.AIR) {
                    player.sendMessage(ChatColor.RED + "Elinde bir eşya tutmalısın!");
                    return true;
                }

                player.getInventory().setItemInMainHand(null);
                target.getInventory().addItem(item).values().forEach(remaining -> target.getWorld().dropItem(target.getLocation(), remaining));
                
                player.sendMessage(ChatColor.GREEN + target.getName() + " adlı oyuncuya eşya gönderildi.");
                target.sendMessage(ChatColor.GOLD + player.getName() + " sana bir eşya gönderdi!");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Bu komutu sadece avcılar kullanabilir!");
                return true;
            }
        }

        if (!player.hasPermission("manhunt.admin")) {
            plugin.getMessageManager().sendMessage(player, "no_permission");
            return true;
        }

        // GUI Menüsünü Aç
        if (args.length == 0) {
            new MainMenu(plugin).open(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help": sendHelp(player); break;
            case "start": {
                if (checkGameRunning(player)) return true;
                plugin.getGameManager().startCountdown();
                break;
            }
            case "surround": {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Kullanım: /manhunt surround <runner>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !plugin.getGameManager().isRunner(target)) {
                    player.sendMessage(ChatColor.RED + "Runner bulunamadı!");
                    return true;
                }
                plugin.getGameManager().surroundRunner(new com.example.manhunt.bukkit.impl.BukkitPlayer(target));
                player.sendMessage(ChatColor.GREEN + "Runner kuşatıldı!");
                break;
            }
            case "sethealth": {
                if (args.length < 3) {
                    plugin.getMessageManager().sendMessage(player, "usage_sethealth");
                    return true;
                }
                String role = args[1].toLowerCase();
                if (!role.equals("runner") && !role.equals("hunter")) {
                    plugin.getMessageManager().sendMessage(player, "usage_sethealth");
                    return true;
                }
                try {
                    int hearts = Integer.parseInt(args[2]);
                    if (hearts < 1 || hearts > 100) {
                        player.sendMessage(ChatColor.RED + "Kalp sayısı 1-100 arasında olmalıdır.");
                        return true;
                    }
                    plugin.getConfig().set("kits." + role + ".max-health", (double) (hearts * 2));
                    plugin.saveConfig();
                    plugin.getMessageManager().sendMessage(player, "health_set", role.toUpperCase(), hearts);
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().sendMessage(player, "usage_sethealth");
                }
                break;
            }
            case "togglecleanup": {
                boolean current = plugin.getConfig().getBoolean("item-cleanup.enabled", true);
                plugin.getConfig().set("item-cleanup.enabled", !current);
                plugin.saveConfig();
                String status = !current ? ChatColor.GREEN + "AKTİF" : ChatColor.RED + "DEVRE DIŞI";
                plugin.getMessageManager().sendMessage(player, "cleanup_toggled", status);
                break;
            }
            case "togglegrace": {
                boolean current = plugin.getConfig().getBoolean("grace-period.enabled", true);
                plugin.getConfig().set("grace-period.enabled", !current);
                plugin.saveConfig();
                String status = !current ? ChatColor.GREEN + "AKTİF" : ChatColor.RED + "DEVRE DIŞI";
                player.sendMessage(ChatColor.GOLD + "[Manhunt] " + ChatColor.YELLOW + "Doğuş Koruması: " + status);
                break;
            }
            case "settings": {
                new MainMenu(plugin).open(player);
                break;
            }
            case "setrespawn": {
                if (args.length < 3) {
                    plugin.getMessageManager().sendMessage(player, "usage_setrespawn");
                    return true;
                }
                try {
                    int min = Integer.parseInt(args[1]);
                    int max = Integer.parseInt(args[2]);
                    if (min < 0 || max < min) {
                        player.sendMessage(ChatColor.RED + "Geçersiz mesafe! Min 0'dan büyük, Max ise Min'den büyük olmalıdır.");
                        return true;
                    }
                    plugin.getConfig().set("hunter.respawn-distance.min", min);
                    plugin.getConfig().set("hunter.respawn-distance.max", max);
                    plugin.saveConfig();
                    plugin.getMessageManager().sendMessage(player, "respawn_set", min, max);
                } catch (NumberFormatException e) {
                    plugin.getMessageManager().sendMessage(player, "usage_setrespawn");
                }
                break;
            }
            case "stop": {
                org.bukkit.Location loc = player.getLocation();
                plugin.getGameManager().endGame(new com.example.manhunt.api.MLocation(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()), true);
                plugin.getMessageManager().broadcastRaw("&6[Manhunt] &cOyun bir yetkili tarafından durduruldu.");
                break;
            }
            default: sendHelp(player); break;
        }
        return true;
    }

    private boolean checkGameRunning(@NotNull Player player) {
        if (plugin.getGameManager().isGameRunning()) {
            plugin.getMessageManager().sendMessage(player, "game_already_running");
            return true;
        }
        return false;
    }

    private void sendHelp(@NotNull Player player) {
        plugin.getMessageManager().sendMessage(player, "help_header");
        player.sendMessage(ChatColor.YELLOW + "/manhunt " + ChatColor.GRAY + "- Ana yönetim menüsünü açar.");
        player.sendMessage(ChatColor.YELLOW + "/hc <mesaj> " + ChatColor.GRAY + "- Avcılar arası takım sohbeti.");
        plugin.getMessageManager().sendMessage(player, "help_track");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "track", "surround", "donate", "sethealth", "togglecleanup", "togglegrace", "setrespawn").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("sethealth")) {
            return Arrays.asList("runner", "hunter").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
