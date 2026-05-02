package com.example.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class SchedulerUtils {

    private static Boolean isFolia;

    public static boolean isFolia() {
        if (isFolia == null) {
            try {
                Class.forName("io.papermc.paper.threadedregionsapi.RegionScrollingConfigurations");
                isFolia = true;
            } catch (ClassNotFoundException e) {
                isFolia = false;
            }
        }
        return isFolia;
    }

    public static Object runTask(Plugin plugin, Runnable runnable) {
        if (isFolia()) {
            try {
                Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                return scheduler.getClass().getMethod("execute", Plugin.class, Runnable.class).invoke(scheduler, plugin, runnable);
            } catch (Exception e) {
                return Bukkit.getScheduler().runTask(plugin, runnable);
            }
        } else {
            return Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static Object runTaskLater(Plugin plugin, Runnable runnable, long delayTicks) {
        if (isFolia()) {
            try {
                Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                Method runDelayed = scheduler.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class);
                return runDelayed.invoke(scheduler, plugin, (Consumer<Object>) (task) -> runnable.run(), delayTicks);
            } catch (Exception e) {
                return Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
            }
        } else {
            return Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
        }
    }

    public static Object runTaskTimer(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia()) {
            try {
                Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                Method runAtFixedRate = scheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
                return runAtFixedRate.invoke(scheduler, plugin, (Consumer<Object>) (task) -> runnable.run(), Math.max(1, delayTicks), periodTicks);
            } catch (Exception e) {
                return Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
            }
        } else {
            return Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
        }
    }

    public static Object runTaskAsync(Plugin plugin, Runnable runnable) {
        if (isFolia()) {
            try {
                Object scheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                Method runNow = scheduler.getClass().getMethod("runNow", Plugin.class, Consumer.class);
                return runNow.invoke(scheduler, plugin, (Consumer<Object>) (task) -> runnable.run());
            } catch (Exception e) {
                return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
            }
        } else {
            return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    public static void cancelTask(Object task) {
        if (task == null) return;
        try {
            if (isFolia()) {
                task.getClass().getMethod("cancel").invoke(task);
            } else {
                if (task instanceof org.bukkit.scheduler.BukkitTask) {
                    ((org.bukkit.scheduler.BukkitTask) task).cancel();
                }
            }
        } catch (Exception ignored) {}
    }

    public static void teleport(Entity entity, Location location) {
        try {
            if (isFolia()) {
                entity.getClass().getMethod("teleportAsync", Location.class).invoke(entity, location);
            } else {
                entity.teleport(location);
            }
        } catch (Exception e) {
            entity.teleport(location);
        }
    }
}
