package com.example.manhunt.sponge.impl;

import com.example.manhunt.api.MLocation;
import com.example.manhunt.api.MPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;

import java.time.Duration;
import java.util.UUID;

public class SpongePlayer implements MPlayer {
    private final ServerPlayer player;

    public SpongePlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public @NotNull String getName() {
        return player.name();
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return player.uniqueId();
    }

    private Component translate(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        player.sendMessage(translate(message));
    }

    @Override
    public void sendActionBar(@NotNull String message) {
        player.sendActionBar(translate(message));
    }

    @Override
    public void sendTitle(@NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
        Title t = Title.title(
            translate(title),
            translate(subtitle),
            Title.Times.times(Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L))
        );
        player.showTitle(t);
    }

    @Override
    public MLocation getLocation() {
        ServerLocation loc = player.serverLocation();
        return new MLocation(
            loc.world().key().toString(),
            loc.x(), loc.y(), loc.z(),
            0, 0
        );
    }

    @Override
    public void teleport(MLocation location) {
        ResourceKey key = ResourceKey.resolve(location.getWorldName());
        Sponge.server().worldManager().world(key).ifPresent(world -> {
            player.setLocation(ServerLocation.of(world, location.getX(), location.getY(), location.getZ()));
        });
    }

    @Override
    public void setCompassTarget(MLocation location) {
        // Handled via custom tracking in CompassManager/Scoreboard for Sponge
    }

    @Override
    public void updateInventoryCompass(MLocation target) {
        // Lodestone tracking not yet implemented for Sponge
    }

    @Override
    public double getHealth() {
        return player.health().get();
    }

    @Override
    public void setHealth(double health) {
        player.health().set(health);
    }

    @Override
    public void setMaxHealth(double health) {
        player.maxHealth().set(health);
    }

    @Override
    public void clearInventory() {
        player.inventory().clear();
    }

    @Override
    public void giveItem(@NotNull String material, int amount) {
        String key = material.contains(":") ? material.toLowerCase() : "minecraft:" + material.toLowerCase();
        org.spongepowered.api.item.ItemType type = org.spongepowered.api.Sponge.game().registry(org.spongepowered.api.registry.RegistryTypes.ITEM_TYPE)
                .value(ResourceKey.resolve(key));
        
        org.spongepowered.api.item.inventory.ItemStack stack = org.spongepowered.api.item.inventory.ItemStack.of(type, amount);
        player.inventory().offer(stack);
    }

    @Override
    public void addPotionEffect(@NotNull String effectType, int duration, int amplifier) {
        String key = effectType.contains(":") ? effectType.toLowerCase() : "minecraft:" + effectType.toLowerCase();
        org.spongepowered.api.effect.potion.PotionEffectType type = org.spongepowered.api.Sponge.game().registry(org.spongepowered.api.registry.RegistryTypes.POTION_EFFECT_TYPE)
                .value(ResourceKey.resolve(key));
        
        org.spongepowered.api.effect.potion.PotionEffect effect = org.spongepowered.api.effect.potion.PotionEffect.builder()
                .potionType(type)
                .duration(org.spongepowered.api.util.Ticks.of(duration))
                .amplifier(amplifier)
                .build();
        
        player.transform(org.spongepowered.api.data.Keys.POTION_EFFECTS, list -> {
            list.add(effect);
            return list;
        });
    }

    @Override
    public void setGameMode(String mode) {
        String key = mode.contains(":") ? mode.toLowerCase() : "minecraft:" + mode.toLowerCase();
        org.spongepowered.api.entity.living.player.gamemode.GameMode gm = org.spongepowered.api.Sponge.game().registry(org.spongepowered.api.registry.RegistryTypes.GAME_MODE)
                .value(ResourceKey.resolve(key));
        player.offer(org.spongepowered.api.data.Keys.GAME_MODE, gm);
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MPlayer) return ((MPlayer) obj).getUniqueId().equals(getUniqueId());
        return false;
    }
}
