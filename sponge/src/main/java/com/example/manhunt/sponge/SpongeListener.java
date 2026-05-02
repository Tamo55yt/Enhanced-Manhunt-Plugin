package com.example.manhunt.sponge;

import com.example.manhunt.SpongeMain;
import com.example.manhunt.sponge.impl.SpongePlayer;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.advancement.AdvancementEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.time.Duration;

/**
 * SpongeListener - Orchestrates Manhunt game events for the Sponge platform.
 * Polished for better UI and visual consistency with Bukkit.
 */
public class SpongeListener {

    private final SpongeMain plugin;
    private final Random random = new Random();
    private static final List<ItemType> VALID_DROPS = new ArrayList<>();

    static {
        registerDefaultDrop(ItemTypes.DIAMOND);
        registerDefaultDrop(ItemTypes.IRON_INGOT);
        registerDefaultDrop(ItemTypes.GOLD_INGOT);
        registerDefaultDrop(ItemTypes.APPLE);
        registerDefaultDrop(ItemTypes.COOKED_BEEF);
    }

    private static void registerDefaultDrop(org.spongepowered.api.registry.DefaultedRegistryReference<ItemType> type) {
        try { VALID_DROPS.add(type.get()); } catch (Exception ignored) {}
    }

    public SpongeListener(SpongeMain plugin) {
        this.plugin = plugin;
    }

    private Component parse(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }

    @Listener
    public void onPlayerJoin(ServerSideConnectionEvent.Join event) {
        ServerPlayer player = event.player();
        if (plugin.getGameManager().isGameRunning() && plugin.getGameManager().isHunter(new SpongePlayer(player))) {
            ((com.example.manhunt.sponge.SpongeCompassManager) plugin.getCompassManager()).giveCompass(new SpongePlayer(player));
        }
    }

    @Listener(order = Order.POST)
    public void onCompassInteractPrimary(InteractItemEvent.Primary event) {
        handleCompassPing(event);
    }

    @Listener(order = Order.POST)
    public void onCompassInteractSecondary(InteractItemEvent.Secondary event) {
        handleCompassModeOrTeleport(event);
    }

    private void handleCompassPing(InteractItemEvent event) {
        Optional<ServerPlayer> optPlayer = event.cause().first(ServerPlayer.class);
        if (optPlayer.isEmpty() || event.itemStack().createStack().type() != ItemTypes.COMPASS.get()) return;

        ServerPlayer player = optPlayer.get();
        if (!plugin.getGameManager().isHunter(new SpongePlayer(player))) return;

        RayTrace.block()
                .world(player.world())
                .sourceEyePosition(player)
                .direction(player.direction())
                .limit(100)
                .select(RayTrace.nonAir())
                .execute()
                .ifPresent(result -> {
                    Vector3d pos = result.selectedObject().serverLocation().position();
                    spawnVisualPing(player, pos);
                    broadcastPingMessage(player, pos);
                    if (event instanceof Cancellable) ((Cancellable) event).setCancelled(true);
                });
    }

    private void spawnVisualPing(ServerPlayer player, Vector3d pos) {
        ParticleEffect effect = ParticleEffect.builder().type(ParticleTypes.FLAME.get()).quantity(1).offset(new Vector3d(0.1, 0.1, 0.1)).build();
        for (int i = 0; i < 10; i++) {
            final int delay = i;
            Sponge.server().scheduler().submit(Task.builder()
                .execute(() -> {
                    for (int y = 0; y < 256; y += 2) {
                        player.world().spawnParticles(effect, new Vector3d(pos.x(), y, pos.z()));
                    }
                })
                .delay(Ticks.of(delay * 20))
                .plugin(plugin.getContainer())
                .build());
        }
        player.playSound(Sound.sound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP.get(), Sound.Source.PLAYER, 1f, 1f), pos);
    }

    private void broadcastPingMessage(ServerPlayer player, Vector3d pos) {
        plugin.getMessageManager().broadcastRaw("&6[PING] &e" + player.name() + "&7: Buraya bakın! (" + (int)pos.x() + ", " + (int)pos.y() + ", " + (int)pos.z() + ")");
    }

    private void handleCompassModeOrTeleport(InteractItemEvent.Secondary event) {
        Optional<ServerPlayer> optPlayer = event.cause().first(ServerPlayer.class);
        if (optPlayer.isEmpty() || event.itemStack().createStack().type() != ItemTypes.COMPASS.get()) return;

        ServerPlayer player = optPlayer.get();
        SpongePlayer mPlayer = new SpongePlayer(player);

        boolean isSpectator = player.get(Keys.GAME_MODE).map(gm -> gm.equals(GameModes.SPECTATOR.get())).orElse(false);
        if (isSpectator && plugin.getGameManager().isHunter(mPlayer)) {
            new com.example.manhunt.sponge.menu.SpectatorTeleportMenu(plugin).open(player);
            if (event instanceof Cancellable) ((Cancellable) event).setCancelled(true);
            return;
        }

        if (plugin.getGameManager().isHunter(mPlayer)) {
            plugin.getCompassManager().cycleCompassMode(mPlayer);
            if (event instanceof Cancellable) ((Cancellable) event).setCancelled(true);
        }
    }

    @Listener
    public void onGunpowderInteract(InteractItemEvent.Secondary event) {
        Optional<ServerPlayer> optPlayer = event.cause().first(ServerPlayer.class);
        if (optPlayer.isEmpty() || event.itemStack().createStack().type() != ItemTypes.GUNPOWDER.get()) return;

        ServerPlayer player = optPlayer.get();
        if (!plugin.getGameManager().isSmokeBombEnabled() || !plugin.getGameManager().isRunner(new SpongePlayer(player))) return;

        long lastUse = plugin.getGameManager().getAbilityCooldowns().getOrDefault(player.uniqueId(), 0L);
        long now = System.currentTimeMillis();
        if (now - lastUse < 600000L) {
            long remaining = (600000L - (now - lastUse)) / 1000L;
            player.sendMessage(parse("&cSis bombası hazır değil! Kalan: " + remaining + "s"));
            return;
        }
        executeSmokeBomb(player, now);
    }

    private void executeSmokeBomb(ServerPlayer player, long timestamp) {
        plugin.getGameManager().getAbilityCooldowns().put(player.uniqueId(), timestamp);
        player.sendMessage(parse("&8Sis bombası kullanıldı!"));
        player.world().spawnParticles(ParticleEffect.builder().type(ParticleTypes.LARGE_SMOKE.get()).quantity(100).offset(new Vector3d(2, 2, 2)).build(), player.position());

        for (ServerPlayer p : Sponge.server().onlinePlayers()) {
            if (plugin.getGameManager().isHunter(new SpongePlayer(p)) && p.position().distance(player.position()) < 15) {
                new SpongePlayer(p).addPotionEffect("minecraft:blindness", 60, 1);
                new SpongePlayer(p).addPotionEffect("minecraft:slowness", 60, 2);
                p.sendMessage(parse("&cRunner sis bombası kullandı! Gözlerin yanıyor!"));
            }
        }
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.All event) {
        if (!plugin.getGameManager().isGameRunning()) return;
        plugin.getLootPoolManager().incrementProgress();
        event.transactions().forEach(transaction -> {
            String blockType = transaction.original().state().type().key(RegistryTypes.BLOCK_TYPE).toString();
            Optional<ServerLocation> optLoc = transaction.original().location();
            if (optLoc.isEmpty()) return;
            ServerLocation loc = optLoc.get();

            List<String> customLoot = plugin.getLootPoolManager().getLoot(blockType);
            if (customLoot != null) {
                transaction.setCustom(transaction.original().withState(BlockTypes.AIR.get().defaultState()));
                customLoot.forEach(mat -> spawnCustomItem(loc, mat));
            }

            if (plugin.getScenarioManager().getSelectedScenario().equals("RANDOM_DROPS") && !VALID_DROPS.isEmpty()) {
                transaction.setCustom(transaction.original().withState(BlockTypes.AIR.get().defaultState()));
                spawnEntityItem(loc, VALID_DROPS.get(random.nextInt(VALID_DROPS.size())));
            }
        });
    }

    private void spawnCustomItem(ServerLocation loc, String mat) {
        String key = mat.contains(":") ? mat.toLowerCase() : "minecraft:" + mat.toLowerCase();
        Sponge.game().registry(RegistryTypes.ITEM_TYPE).findValue(ResourceKey.resolve(key)).ifPresent(type -> spawnEntityItem(loc, type));
    }

    private void spawnEntityItem(ServerLocation loc, ItemType type) {
        Entity item = loc.world().createEntity(EntityTypes.ITEM.get(), loc.position());
        item.offer(Keys.ITEM_STACK_SNAPSHOT, ItemStack.of(type, 1).createSnapshot());
        loc.world().spawnEntity(item);
    }

    @Listener
    public void onAdvancement(AdvancementEvent.Grant event) {
        ServerPlayer player = event.player();
        SpongePlayer mPlayer = new SpongePlayer(player);
        String advName = event.advancement().toString();
        boolean isEarnerHunter = plugin.getGameManager().isHunter(mPlayer);
        boolean isAnnounceEnabled = plugin.getGameManager().isAdvancementAnnouncementsEnabled();

        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500));
        for (ServerPlayer p : Sponge.server().onlinePlayers()) {
            if (plugin.getGameManager().isHunter(new SpongePlayer(p))) {
                if (isEarnerHunter) {
                    p.showTitle(Title.title(parse("&6&l" + player.name()), parse("&f[" + advName + "] Başarısını Kazandı!"), times));
                    plugin.getMessageManager().sendMessage(new SpongePlayer(p), "advancement_hunter", player.name(), advName);
                } else if (isAnnounceEnabled) {
                    p.showTitle(Title.title(parse("&c&l" + player.name()), parse("&eRunner [" + advName + "] Aldı!"), times));
                }
            }
        }
    }

    @Listener
    public void onWorldChange(org.spongepowered.api.event.entity.ChangeEntityWorldEvent.Pre event) {
        if (!(event.entity() instanceof ServerPlayer player)) return;
        if (plugin.getGameManager().isRunner(new SpongePlayer(player))) {
            ServerLocation from = player.serverLocation();
            plugin.getCompassManager().setLastPortalLocation(new com.example.manhunt.api.MLocation(from.world().key().toString(), from.x(), from.y(), from.z(), 0, 0));
            String targetWorld = event.destinationWorld().key().value().contains("nether") ? "Nether" : "Dünya";
            
            Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500));
            Sponge.server().onlinePlayers().forEach(p -> p.showTitle(Title.title(parse("&c&l" + player.name()), parse("&e" + targetWorld + "'e Geçiş Yaptı!"), times)));

            Component coords = parse("&6[PORTAL] &eRunner " + targetWorld + "'e girdi! Koordinatlar: &f" + (int)from.x() + ", " + (int)from.y() + ", " + (int)from.z());
            Sponge.server().onlinePlayers().stream().filter(p -> plugin.getGameManager().isHunter(new SpongePlayer(p))).forEach(p -> p.sendMessage(coords));
            Sponge.server().scheduler().submit(Task.builder().execute(() -> plugin.getCompassManager().updateAllCompassTargets()).delay(Ticks.of(1)).plugin(plugin.getContainer()).build());
        }
    }

    @Listener
    public void onHunterMove(MoveEntityEvent event) {
        if (event.entity() instanceof ServerPlayer player && plugin.getGameManager().isGracePeriod() && plugin.getGameManager().isHunter(new SpongePlayer(player))) {
            if (!plugin.getPlatform().getConfigBoolean("grace-period.allow-movement", false) && event.originalPosition().distance(event.destinationPosition()) > 0.01) event.setCancelled(true);
        }
    }

    @Listener
    public void onHunterDamage(DamageEntityEvent event) {
        event.cause().first(ServerPlayer.class).ifPresent(damager -> {
            if (plugin.getGameManager().isGracePeriod() && plugin.getGameManager().isHunter(new SpongePlayer(damager))) event.setCancelled(true);
        });
    }

    @Listener
    public void onPlayerDeath(DestructEntityEvent.Death event) {
        if (event.entity() instanceof ServerPlayer player) plugin.getGameManager().handleDeath(player);
    }

    @Listener
    public void onPlayerRespawn(RespawnPlayerEvent event) {
        plugin.getGameManager().handleRespawn(event);
    }

    @Listener
    public void onChat(PlayerChatEvent event) {
        Optional<ServerPlayer> optPlayer = event.cause().first(ServerPlayer.class);
        if (optPlayer.isEmpty()) return;
        ServerPlayer player = optPlayer.get();
        if (com.example.manhunt.sponge.menu.SetupMenu.awaitingChatInput.containsKey(player.uniqueId())) {
            if (event instanceof Cancellable) ((Cancellable) event).setCancelled(true);
            String type = com.example.manhunt.sponge.menu.SetupMenu.awaitingChatInput.remove(player.uniqueId());
            // Safe plain text serialization
            String msg = LegacyComponentSerializer.legacySection().serialize(event.message());
            handleChatMenuInput(player, type, msg);
        }
    }

    private void handleChatMenuInput(ServerPlayer player, String type, String input) {
        try {
            if ("countdown".equals(type)) player.sendMessage(parse("&aGeri sayım süresi " + Integer.parseInt(input) + " dakika olarak ayarlandı."));
            Sponge.server().scheduler().submit(Task.builder().execute(() -> new com.example.manhunt.sponge.menu.SetupMenu(plugin).open(player)).plugin(plugin.getContainer()).build());
        } catch (NumberFormatException e) {
            player.sendMessage(parse("&cGeçersiz sayı! İşlem iptal edildi."));
        }
    }
}
