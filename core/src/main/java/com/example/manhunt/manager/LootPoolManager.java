package com.example.manhunt.manager;

import com.example.manhunt.api.MPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class LootPoolManager {

    private final Random random = new Random();
    private final Map<String, List<LootEntry>> lootPools = new ConcurrentHashMap<>();
    private volatile boolean enabled = false;
    private volatile boolean levelingEnabled = false;
    private final java.util.concurrent.atomic.AtomicInteger globalProgress = new java.util.concurrent.atomic.AtomicInteger(0);

    public LootPoolManager(MPlatform platform) {
        loadDefaults();
    }

    private void loadDefaults() {
        // Example defaults
        addLoot("PIG", "DIAMOND", 0.05);
        addLoot("PIG", "IRON_INGOT", 0.2);
        addLoot("GRASS_BLOCK", "APPLE", 0.1);
        addLoot("DIRT", "GOLD_INGOT", 0.01);
    }

    public void addLoot(String source, String material, double chance) {
        lootPools.computeIfAbsent(source.toUpperCase(), k -> new java.util.concurrent.CopyOnWriteArrayList<>())
                .add(new LootEntry(material, chance));
    }

    public List<String> getLoot(String source) {
        if (!enabled) return null;
        
        List<LootEntry> entries = lootPools.get(source.toUpperCase());
        if (entries == null) return null;

        int currentLevel = getLevel();
        List<String> results = new ArrayList<>();
        for (LootEntry entry : entries) {
            double chance = entry.getChance();
            if (levelingEnabled) {
                // Increase chance by 5% per level, up to 2x original chance
                chance = Math.min(chance * 2, chance * (1 + (currentLevel * 0.05)));
            }

            if (random.nextDouble() < chance) {
                results.add(entry.getMaterial());
            }
        }
        return results.isEmpty() ? null : results;
    }

    public void incrementProgress() {
        if (levelingEnabled) globalProgress.incrementAndGet();
    }

    public int getLevel() {
        // Level increases every 100 global actions
        return globalProgress.get() / 100;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isLevelingEnabled() { return levelingEnabled; }
    public void setLevelingEnabled(boolean levelingEnabled) { this.levelingEnabled = levelingEnabled; }
    public void resetProgress() { globalProgress.set(0); }

    private static class LootEntry {
        private final String material;
        private final double chance;

        LootEntry(String material, double chance) {
            this.material = material;
            this.chance = chance;
        }

        public String getMaterial() { return material; }
        public double getChance() { return chance; }
    }
}
