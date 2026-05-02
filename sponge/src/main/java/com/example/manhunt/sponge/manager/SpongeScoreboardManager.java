package com.example.manhunt.sponge.manager;

import com.example.manhunt.SpongeMain;
import com.example.manhunt.api.MPlayer;
import com.example.manhunt.sponge.impl.SpongePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.criteria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;

import java.util.ArrayList;
import java.util.List;

public class SpongeScoreboardManager {

    private final SpongeMain plugin;
    private ScheduledTask scoreboardTask;

    public SpongeScoreboardManager(SpongeMain plugin) {
        this.plugin = plugin;
    }

    public void startScoreboardTask() {
        if (scoreboardTask != null) scoreboardTask.cancel();
        scoreboardTask = Sponge.server().scheduler().submit(Task.builder()
                .execute(this::updateScoreboards)
                .interval(org.spongepowered.api.util.Ticks.of(20))
                .plugin(plugin.getContainer())
                .build());
    }

    public void stopTasks() {
        if (scoreboardTask != null) {
            scoreboardTask.cancel();
            scoreboardTask = null;
        }
    }

    private void updateScoreboards() {
        if (!plugin.getGameManager().isGameRunning()) return;

        long now = System.currentTimeMillis();
        String timeStr = formatTime(now - plugin.getGameManager().getGameStartTime());
        MPlayer runner = plugin.getGameManager().getFirstRunner();
        String runnerName = runner != null ? runner.getName() : "---";
        String runnerWorld = runner != null ? formatWorldName(runner.getLocation().getWorldName()) : "---";

        for (ServerPlayer player : Sponge.server().onlinePlayers()) {
            Scoreboard sb = player.scoreboard();
            Objective obj = sb.objective("mh_sb").orElseGet(() -> {
                Objective newObj = Objective.builder()
                        .name("mh_sb")
                        .criterion(Criteria.DUMMY)
                        .displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(plugin.getMessageManager().getRawMessage(new SpongePlayer(player), "scoreboard_title")))
                        .build();
                sb.addObjective(newObj);
                sb.updateDisplaySlot(newObj, DisplaySlots.SIDEBAR);
                return newObj;
            });

            List<Component> lines = new ArrayList<>();
            lines.add(Component.text("§1"));
            lines.add(translate(player, "scoreboard_time", timeStr));
            lines.add(Component.text("§2"));
            lines.add(translate(player, "scoreboard_runner", runnerName));
            lines.add(translate(player, "scoreboard_world", runnerWorld));

            if (plugin.getGameManager().isHunter(new SpongePlayer(player)) && runner != null && player.world().key().toString().equals(runner.getLocation().getWorldName())) {
                org.spongepowered.math.vector.Vector3d runnerPos = new org.spongepowered.math.vector.Vector3d(runner.getLocation().getX(), runner.getLocation().getY(), runner.getLocation().getZ());
                int dist = (int) player.position().distance(runnerPos);
                lines.add(translate(player, "scoreboard_distance", String.valueOf(dist)));
            }

            lines.add(Component.text("§3"));
            String roleKey = plugin.getGameManager().isRunner(new SpongePlayer(player)) ? "role_runner" : (plugin.getGameManager().getDeadHunters().contains(player.uniqueId()) ? "role_spectator" : "role_hunter");
            lines.add(translate(player, "scoreboard_role", plugin.getMessageManager().getRawMessage(new SpongePlayer(player), roleKey)));
            lines.add(Component.text("§4"));
            lines.add(Component.text("play.manhunt.net", NamedTextColor.YELLOW));

            obj.scores().clear();
            int scoreValue = lines.size();
            for (Component line : lines) {
                try {
                    java.lang.reflect.Method m = obj.getClass().getMethod("findOrCreateScore", Component.class);
                    ((org.spongepowered.api.scoreboard.Score) m.invoke(obj, line)).setScore(scoreValue--);
                } catch (Exception e1) {
                    try {
                        java.lang.reflect.Method m = obj.getClass().getMethod("findOrCreateScore", String.class);
                        String lineStr = LegacyComponentSerializer.legacySection().serialize(line);
                        ((org.spongepowered.api.scoreboard.Score) m.invoke(obj, lineStr)).setScore(scoreValue--);
                    } catch (Exception e2) {
                        plugin.getLogger().error("Could not update scoreboard score: " + e2.getMessage());
                    }
                }
            }
        }
        updateTabList();
    }

    private Component translate(ServerPlayer p, String key, String... args) {
        String msg = plugin.getMessageManager().getRawMessage(new SpongePlayer(p), key);
        for (String arg : args) {
            msg = msg.replace("%s", arg).replace("%d", arg);
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }

    private String formatTime(long ms) {
        long sec = (ms / 1000) % 60;
        long min = (ms / (1000 * 60));
        return String.format("%02d:%02d", min, sec);
    }

    private String formatWorldName(String name) {
        if (name.contains("nether")) return "§cNether";
        if (name.contains("end")) return "§dEnd";
        return "§aOverworld";
    }

    public void updateTabList() {
        Scoreboard sb = Sponge.server().serverScoreboard().orElse(null);
        if (sb == null) return;

        Team runnerTeam = sb.team("1_Runner").orElseGet(() -> {
            Team t = Team.builder().name("1_Runner").displayName(Component.text("Runner")).build();
            sb.registerTeam(t);
            return t;
        });
        Team hunterTeam = sb.team("2_Hunters").orElseGet(() -> {
            Team t = Team.builder().name("2_Hunters").displayName(Component.text("Hunters")).build();
            sb.registerTeam(t);
            return t;
        });

        for (ServerPlayer player : Sponge.server().onlinePlayers()) {
            if (plugin.getGameManager().isRunner(new SpongePlayer(player))) {
                runnerTeam.addMember(player.teamRepresentation());
            } else if (plugin.getGameManager().isHunter(new SpongePlayer(player))) {
                hunterTeam.addMember(player.teamRepresentation());
            } else {
                runnerTeam.removeMember(player.teamRepresentation());
                hunterTeam.removeMember(player.teamRepresentation());
            }
        }
    }
}
