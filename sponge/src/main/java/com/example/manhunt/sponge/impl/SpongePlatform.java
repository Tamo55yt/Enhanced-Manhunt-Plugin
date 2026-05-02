package com.example.manhunt.sponge.impl;

import com.example.manhunt.api.MPlatform;
import com.example.manhunt.api.MPlayer;
import com.example.manhunt.api.MScheduler;
import com.example.manhunt.api.MWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpongePlatform implements MPlatform {
    private final PluginContainer container;
    private final Path configDir;
    private final MScheduler scheduler;

    public SpongePlatform(PluginContainer container, Path configDir) {
        this.container = container;
        this.configDir = configDir;
        this.scheduler = new SpongeScheduler(container);
    }

    private Component translate(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace('§', '&'));
    }

    @Override
    public void broadcast(@NotNull String message) {
        Sponge.server().broadcastAudience().sendMessage(translate(message));
    }

    @Override
    public void logInfo(@NotNull String message) {
        container.logger().info(message);
    }

    @Override
    public void logWarning(@NotNull String message) {
        container.logger().warn(message);
    }

    @Override
    public @Nullable MPlayer getPlayer(UUID uuid) {
        return Sponge.server().player(uuid).map(SpongePlayer::new).orElse(null);
    }

    @Override
    public @Nullable MPlayer getPlayer(String name) {
        return Sponge.server().player(name).map(SpongePlayer::new).orElse(null);
    }

    @Override
    public Collection<? extends MPlayer> getOnlinePlayers() {
        return Sponge.server().onlinePlayers().stream()
                .map(SpongePlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable MWorld getWorld(String name) {
        return Sponge.server().worldManager().world(ResourceKey.resolve(name))
                .map(SpongeWorld::new).orElse(null);
    }

    @Override
    public Collection<? extends MWorld> getWorlds() {
        return Sponge.server().worldManager().worlds().stream()
                .map(SpongeWorld::new).collect(Collectors.toList());
    }

    @Override
    public MScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public File getDataFolder() {
        return configDir.toFile();
    }

    @Override
    public void setWorldBorder(@NotNull String worldName, double centerX, double centerZ, double size) {
        org.spongepowered.api.Sponge.server().worldManager().world(org.spongepowered.api.ResourceKey.resolve(worldName)).ifPresent(world -> {
            Object border = world.border();
            try {
                // Sponge 8-10
                java.lang.reflect.Method mCenter = border.getClass().getMethod("setCenter", double.class, double.class);
                mCenter.invoke(border, centerX, centerZ);
                java.lang.reflect.Method mSize = border.getClass().getMethod("setDiameter", double.class);
                mSize.invoke(border, size);
            } catch (Exception e1) {
                try {
                    // Sponge 11+
                    java.lang.reflect.Method mCenter = border.getClass().getMethod("center");
                    mCenter.invoke(border);
                } catch (Exception e2) {}
            }
        });
    }

    @Override
    public void resetWorldBorder(@NotNull String worldName) {
        org.spongepowered.api.Sponge.server().worldManager().world(org.spongepowered.api.ResourceKey.resolve(worldName)).ifPresent(world -> {
            Object border = world.border();
            try {
                java.lang.reflect.Method mSize = border.getClass().getMethod("setDiameter", double.class);
                mSize.invoke(border, 59999968.0);
            } catch (Exception e) {}
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void spawnFireworks(@NotNull com.example.manhunt.api.MLocation location) {
        Sponge.server().worldManager().world(ResourceKey.resolve(location.getWorldName())).ifPresent(world -> {
            org.spongepowered.api.entity.Entity firework = world.createEntity(org.spongepowered.api.entity.EntityTypes.FIREWORK_ROCKET.get(), new org.spongepowered.math.vector.Vector3d(location.getX(), location.getY(), location.getZ()));
            
            try {
                Class<?> fwEffectClass = Class.forName("org.spongepowered.api.item.firework.FireworkEffect");
                java.lang.reflect.Method builderMethod = fwEffectClass.getMethod("builder");
                Object builder = builderMethod.invoke(null);
                
                builder.getClass().getMethod("colors", Iterable.class).invoke(builder, java.util.List.of(org.spongepowered.api.util.Color.RED, org.spongepowered.api.util.Color.GREEN, org.spongepowered.api.util.Color.BLUE));
                builder.getClass().getMethod("flicker", boolean.class).invoke(builder, true);
                builder.getClass().getMethod("trail", boolean.class).invoke(builder, true);
                
                Object effect = builder.getClass().getMethod("build").invoke(builder);
                
                // Use raw Key to bypass compiler generic check for ListValue
                org.spongepowered.api.data.Key key = org.spongepowered.api.data.Keys.FIREWORK_EFFECTS;
                firework.offer(key, java.util.List.of(effect));
                firework.offer(org.spongepowered.api.data.Keys.FIREWORK_FLIGHT_MODIFIER, Ticks.of(1));
            } catch (Exception ignored) {}
            
            world.spawnEntity(firework);
        });
    }

    @Override
    public String getConfigString(String path, String def) {
        return def;
    }

    @Override
    public int getConfigInt(String path, int def) {
        return def;
    }

    @Override
    public boolean getConfigBoolean(String path, boolean def) {
        return def;
    }

    @Override
    public double getConfigDouble(String path, double def) {
        return def;
    }
}
