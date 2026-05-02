package com.example.manhunt.manager;

import com.example.manhunt.Main;
import com.example.manhunt.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ScoreboardManager {

    private final Main plugin;
    private Object scoreboardTask;
    private final Map<String, String> worldNameCache = new HashMap<>();

    public ScoreboardManager(@NotNull Main plugin) {
        this.plugin = plugin;
    }

    public void setupGlobalScoreboard() {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        if (sb.getTeam("1_Runner") == null) sb.registerNewTeam("1_Runner");
        if (sb.getTeam("2_Hunters") == null) sb.registerNewTeam("2_Hunters");
    }

    public void startScoreboardTask() {
        if (scoreboardTask != null) SchedulerUtils.cancelTask(scoreboardTask);
        scoreboardTask = SchedulerUtils.runTaskTimer(plugin, () -> {
            if (!plugin.getGameManager().isGameRunning()) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Scoreboard sb = p.getScoreboard();
                    if (sb != null && sb.getObjective("mh_sb") != null) {
                        sb.getObjective("mh_sb").unregister();
                    }
                }
                return;
            }
            updateScoreboards();
        }, 20L, 20L);
    }

    public void stopTasks() {
        if (scoreboardTask != null) { SchedulerUtils.cancelTask(scoreboardTask); scoreboardTask = null; }
    }

    public void updateScoreboards() {
        long now = System.currentTimeMillis();
        String timeStr = formatTime(now - plugin.getGameManager().getGameStartTime());
        Player runner = plugin.getGameManager().getBukkitFirstRunner();
        String runnerName = runner != null ? runner.getName() : "---";
        String runnerWorld = runner != null ? getCachedWorldName(runner.getWorld().getName()) : "---";

        for (Player p : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = p.getScoreboard();
            if (sb == Bukkit.getScoreboardManager().getMainScoreboard() || sb.getObjective("mh_sb") == null) {
                sb = Bukkit.getScoreboardManager().getNewScoreboard();
                setupScoreboardObjective(p, sb);
            }
            org.bukkit.scoreboard.Objective obj = sb.getObjective("mh_sb");
            if (obj == null) continue;

            List<String> lines = new ArrayList<>(12);
            lines.add("§1");
            lines.add(plugin.getMessageManager().getRawMessage(p, "scoreboard_time").replace("%s", timeStr));
            lines.add("§2");
            lines.add(plugin.getMessageManager().getRawMessage(p, "scoreboard_runner").replace("%s", runnerName));
            lines.add(plugin.getMessageManager().getRawMessage(p, "scoreboard_world").replace("%s", runnerWorld));
            
            if (plugin.getGameManager().isHunter(p) && runner != null && p.getWorld().equals(runner.getWorld())) {
                int dist = (int) p.getLocation().distance(runner.getLocation());
                lines.add(plugin.getMessageManager().getRawMessage(p, "scoreboard_distance").replace("%d", String.valueOf(dist)));
            }
            
            lines.add("§3");
            String roleKey = plugin.getGameManager().isRunner(p) ? "role_runner" : (plugin.getGameManager().getDeadHunters().contains(p.getUniqueId()) ? "role_spectator" : "role_hunter");
            lines.add(plugin.getMessageManager().getRawMessage(p, "scoreboard_role").replace("%s", plugin.getMessageManager().getRawMessage(p, roleKey)));
            lines.add("§4");
            lines.add("§eplay.manhunt.net");

            Set<String> currentEntries = sb.getEntries();
            for (String entry : currentEntries) {
                if (!lines.contains(entry)) sb.resetScores(entry);
            }
            
            int score = lines.size();
            for (String line : lines) {
                if (!obj.getScore(line).isScoreSet()) {
                    obj.getScore(line).setScore(score);
                }
                score--;
            }
        }
    }

    private String getCachedWorldName(String name) {
        return worldNameCache.computeIfAbsent(name, this::formatWorldName);
    }

    @SuppressWarnings("deprecation")
    private void setupScoreboardObjective(Player p, Scoreboard sb) {
        String title = plugin.getMessageManager().getRawMessage(p, "scoreboard_title");
        org.bukkit.scoreboard.Objective obj = null;
        
        try {
            java.lang.reflect.Method registerMethod = Scoreboard.class.getMethod("registerNewObjective", String.class, String.class, String.class);
            obj = (org.bukkit.scoreboard.Objective) registerMethod.invoke(sb, "mh_sb", "dummy", title);
        } catch (Exception e) {
            try {
                obj = sb.registerNewObjective("mh_sb", "dummy");
                if (obj != null) obj.setDisplayName(title);
            } catch (Exception ignored) {}
        }
        
        if (obj != null) obj.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
        p.setScoreboard(sb);
    }

    private String formatTime(long ms) {
        long sec = (ms / 1000) % 60;
        long min = (ms / (1000 * 60));
        return String.format("%02d:%02d", min, sec);
    }

    private String formatWorldName(String name) {
        if (name.endsWith("_nether")) return "§cNether";
        if (name.endsWith("_the_end")) return "§dEnd";
        return "§aOverworld";
    }

    public void updateTabList() {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Team runnerTeam = sb.getTeam("1_Runner");
        Team hunterTeam = sb.getTeam("2_Hunters");
        if (runnerTeam == null || hunterTeam == null) { setupGlobalScoreboard(); return; }

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            String name = player.getName();
            String listName;
            
            if (plugin.getGameManager().isRunner(player)) {
                if (!runnerTeam.hasEntry(name)) runnerTeam.addEntry(name);
                listName = ChatColor.AQUA + "" + ChatColor.BOLD + "RUNNER " + ChatColor.WHITE + name;
            } else if (plugin.getGameManager().isHunter(player)) {
                if (!hunterTeam.hasEntry(name)) hunterTeam.addEntry(name);
                if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
                    listName = ChatColor.GRAY + "HUNTER " + ChatColor.WHITE + name + ChatColor.RED + " [İZLİYOR]";
                } else if (plugin.getGameManager().getDeadHunters().contains(uuid)) {
                    listName = ChatColor.GOLD + "HUNTER " + ChatColor.WHITE + name + ChatColor.RED + " [ÖLDÜ]";
                } else {
                    listName = ChatColor.GOLD + "HUNTER " + ChatColor.WHITE + name;
                }
            } else {
                if (runnerTeam.hasEntry(name)) runnerTeam.removeEntry(name);
                if (hunterTeam.hasEntry(name)) hunterTeam.removeEntry(name);
                listName = name;
            }
            if (!listName.equals(player.getPlayerListName())) player.setPlayerListName(listName);
        }
    }
}
