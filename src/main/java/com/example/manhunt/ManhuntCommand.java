package com.example.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ManhuntCommand implements CommandExecutor {

    private final Main plugin;
    private final Random random = new Random();

    public ManhuntCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("only_player"));
            return true;
        }

        if (!player.hasPermission("manhunt.admin")) {
            plugin.getMessageManager().sendMessage(player, "no_permission");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setrunner":
                if (args.length < 2) {
                    plugin.getMessageManager().sendMessage(player, "usage_setrunner");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    plugin.getMessageManager().sendMessage(player, "player_not_found");
                    return true;
                }
                plugin.setRunnerUUID(target.getUniqueId());
                plugin.getMessageManager().broadcast("runner_selected", target.getName());
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (plugin.isHunter(online)) {
                        plugin.giveCompass(online);
                        plugin.getMessageManager().sendMessage(online, "you_are_hunter");
                    }
                }
                break;

            case "setkit":
                if (args.length < 2) {
                    plugin.getMessageManager().sendMessage(player, "usage_setkit");
                    return true;
                }
                String role = args[1].toLowerCase();
                if (!role.equals("runner") && !role.equals("hunter")) {
                    plugin.getMessageManager().sendMessage(player, "invalid_role");
                    return true;
                }
                plugin.getKitManager().saveKit(player, role);
                break;

            case "start":
                plugin.startGame(true);
                break;

            case "random":
                if (!hasEnoughPlayers(player)) return true;
                Player randomRunner = getRandomPlayer();
                if (randomRunner == null) {
                    player.sendMessage("§cFailed to select a random player.");
                    return true;
                }
                plugin.setRunnerUUID(randomRunner.getUniqueId());
                plugin.getMessageManager().broadcast("random_runner_selected", randomRunner.getName());
                plugin.startGame(true);
                break;

            case "hidden":
                if (!hasEnoughPlayers(player)) return true;
                Player hiddenRunner = getRandomPlayer();
                if (hiddenRunner == null) {
                    player.sendMessage("§cFailed to select a random player.");
                    return true;
                }
                plugin.setRunnerUUID(hiddenRunner.getUniqueId());

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.getUniqueId().equals(hiddenRunner.getUniqueId())) {
                        plugin.getMessageManager().sendTitle(online, "runner_winner_title", 10, 60, 10);
                        plugin.getMessageManager().sendMessage(online, "runner_identity_hidden");
                    } else {
                        plugin.getMessageManager().sendTitle(online, "hunter_winner_title", 10, 60, 10);
                        plugin.getMessageManager().sendMessage(online, "hunter_identity_hidden");
                    }
                }
                plugin.startGame(false);
                break;

            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private boolean hasEnoughPlayers(Player sender) {
        if (Bukkit.getOnlinePlayers().size() < 2) {
            plugin.getMessageManager().sendMessage(sender, "not_enough_players");
            return false;
        }
        return true;
    }

    private Player getRandomPlayer() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) return null;
        return players.get(random.nextInt(players.size()));
    }

    private void sendHelp(Player player) {
        plugin.getMessageManager().sendMessage(player, "help_header");
        plugin.getMessageManager().sendMessage(player, "help_setrunner");
        plugin.getMessageManager().sendMessage(player, "help_setkit");
        plugin.getMessageManager().sendMessage(player, "help_start");
        plugin.getMessageManager().sendMessage(player, "help_random");
        plugin.getMessageManager().sendMessage(player, "help_hidden");
    }
}