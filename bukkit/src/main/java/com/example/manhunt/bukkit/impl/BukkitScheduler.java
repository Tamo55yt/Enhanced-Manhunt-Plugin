package com.example.manhunt.bukkit.impl;

import com.example.manhunt.api.MScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class BukkitScheduler implements MScheduler {
    private final Plugin plugin;

    public BukkitScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    private static class BukkitTaskWrapper implements Task {
        private final BukkitTask bukkitTask;

        private BukkitTaskWrapper(BukkitTask bukkitTask) {
            this.bukkitTask = bukkitTask;
        }

        @Override
        public void cancel() {
            bukkitTask.cancel();
        }

        @Override
        public boolean isCancelled() {
            return !Bukkit.getScheduler().isQueued(bukkitTask.getTaskId()) && !Bukkit.getScheduler().isCurrentlyRunning(bukkitTask.getTaskId());
        }
    }

    @Override
    public @NotNull Task runTask(@NotNull Runnable runnable) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTask(plugin, runnable));
    }

    @Override
    public @NotNull Task runTaskLater(@NotNull Runnable runnable, long delayTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks));
    }

    @Override
    public @NotNull Task runTaskTimer(@NotNull Runnable runnable, long delayTicks, long periodTicks) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks));
    }

    @Override
    public @NotNull Task runTaskAsync(@NotNull Runnable runnable) {
        return new BukkitTaskWrapper(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }
}
