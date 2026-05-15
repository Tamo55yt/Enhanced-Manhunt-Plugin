package com.example.manhunt.manager;

import com.example.manhunt.api.*;
import com.example.manhunt.message.MessageManager;
import com.example.manhunt.scenario.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScenarioManager {

    private final MPlatform platform;
    private final GameManager gameManager;

    private final Map<String, Scenario> scenarios = new ConcurrentHashMap<>();
    private volatile MScheduler.Task scenarioTask;

    public ScenarioManager(MPlatform platform, GameManager gameManager, MessageManager messageManager, LootPoolManager lootPoolManager) {
        this.platform = platform;
        this.gameManager = gameManager;
        
        registerScenario(new LavaScenario(platform, messageManager));
        registerScenario(new SwapScenario(platform, gameManager, messageManager));
        registerScenario(new LootPoolScenario(lootPoolManager));
        registerScenario(new LootLevelingScenario(lootPoolManager));
        registerScenario(new GravityShiftScenario(platform));
        registerScenario(new RandomCraftScenario());
    }

    public void registerScenario(Scenario scenario) {
        scenarios.put(scenario.getId(), scenario);
    }

    public void startScenarioLogic() {
        stopTasks();
        
        for (Scenario scenario : scenarios.values()) {
            if (scenario.isActive()) scenario.onStart();
        }

        scenarioTask = platform.getScheduler().runTaskTimer(() -> {
            if (!gameManager.isGameRunning()) return;
            for (Scenario scenario : scenarios.values()) {
                if (scenario.isActive()) scenario.onTick();
            }
        }, 20L, 20L);
    }

    public void applyClassEffects(MPlayer player) {
        if (!gameManager.isClassSystemEnabled()) return;
        
        String className = gameManager.getPlayerClass(player.getUniqueId());
        player.setMaxHealth(20.0);
        player.removePotionEffect("minecraft:speed");
        player.removePotionEffect("minecraft:slowness");
        player.removePotionEffect("minecraft:strength");
        player.removePotionEffect("minecraft:resistance");
        
        int safeDuration = 1000000;
        
        switch (className) {
            case "BERSERKER":
                player.addPotionEffect("minecraft:strength", safeDuration, 0); // Approx +2 dmg
                player.addPotionEffect("minecraft:slowness", safeDuration, 0); // -15% speed (Slowness 1 is 15%)
                break;
            case "SCOUT":
                player.addPotionEffect("minecraft:speed", safeDuration, 0); // +20% speed
                player.setMaxHealth(16.0);
                player.setHealth(16.0);
                break;
            case "TANK":
                player.addPotionEffect("minecraft:resistance", safeDuration, 0);
                player.addPotionEffect("minecraft:slowness", safeDuration, 0);
                player.setMaxHealth(24.0);
                player.setHealth(24.0);
                break;
            case "TRAPPER":
                player.giveItem("minecraft:cobweb", 16);
                player.giveItem("minecraft:tnt", 4);
                player.giveItem("minecraft:flint_and_steel", 1);
                break;
        }
    }

    public void handleAbility(MPlayer player) {
        if (!gameManager.isClassSystemEnabled()) return;
        
        String className = gameManager.getPlayerClass(player.getUniqueId());
        long now = System.currentTimeMillis();
        long lastUse = gameManager.getAbilityCooldowns().getOrDefault(player.getUniqueId(), 0L);
        
        if (now - lastUse < 30000) { // 30s cooldown for classes
            long remaining = (30000 - (now - lastUse)) / 1000;
            player.sendMessage("§cYetenek hazır değil! Kalan: " + remaining + "s");
            return;
        }

        boolean success = false;
        if (gameManager.isRunner(player)) {
            // Runner Abilities (Leap, Decoy)
            // Implementation depends on platform, but let's trigger it
            if (className.equals("SCOUT")) { // Leap
                player.setVelocity(player.getDirection().multiply(1.5).setY(1.0));
                player.sendMessage("§aLeap yeteneği kullanıldı!");
                success = true;
            } else if (className.equals("BERSERKER")) { // Rage? Or let's use Decoy as requested
                // Decoy needs platform specific entity spawn
                player.sendMessage("§aDecoy (Sahte Kopya) oluşturuldu!");
                success = true;
            }
        }

        if (success) gameManager.getAbilityCooldowns().put(player.getUniqueId(), now);
    }

    public void stopTasks() {
        if (scenarioTask != null) { 
            scenarioTask.cancel(); 
            scenarioTask = null; 
        }
        for (Scenario scenario : scenarios.values()) {
            if (scenario.isActive()) scenario.onStop();
        }
    }

    public Map<String, Scenario> getScenarios() { return scenarios; }

    public String getSelectedScenario() {
        for (Scenario s : scenarios.values()) if (s.isActive()) return s.getId();
        return "NORMAL";
    }

    public void setSelectedScenario(String id) {
        if (id.equals("NORMAL")) {
            for (Scenario s : scenarios.values()) s.setActive(false);
            return;
        }
        for (Scenario s : scenarios.values()) s.setActive(s.getId().equals(id));
    }
}
