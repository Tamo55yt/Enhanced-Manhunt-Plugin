package com.example.manhunt.manager;

import com.example.manhunt.api.MPlatform;
import com.example.manhunt.api.MPlayer;
import com.example.manhunt.api.MScheduler;
import com.example.manhunt.api.Scenario;
import com.example.manhunt.message.MessageManager;
import com.example.manhunt.scenario.LavaScenario;
import com.example.manhunt.scenario.LootLevelingScenario;
import com.example.manhunt.scenario.LootPoolScenario;
import com.example.manhunt.scenario.SwapScenario;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScenarioManager {

    private final MPlatform platform;
    private final GameManager gameManager;

    private final Map<String, Scenario> scenarios = new ConcurrentHashMap<>();
    private final Map<UUID, String> selectedClasses = new ConcurrentHashMap<>();
    private volatile MScheduler.Task scenarioTask;

    public ScenarioManager(MPlatform platform, GameManager gameManager, MessageManager messageManager, LootPoolManager lootPoolManager) {
        this.platform = platform;
        this.gameManager = gameManager;
        
        registerScenario(new LavaScenario(platform, messageManager));
        registerScenario(new SwapScenario(platform, gameManager, messageManager));
        registerScenario(new LootPoolScenario(lootPoolManager));
        registerScenario(new LootLevelingScenario(lootPoolManager));
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
        String className = selectedClasses.getOrDefault(player.getUniqueId(), "NONE");
        player.setMaxHealth(20.0);
        int safeDuration = 100000;
        
        switch (className) {
            case "SCOUT":
                player.addPotionEffect("SPEED", safeDuration, 0);
                player.setMaxHealth(16.0);
                player.setHealth(16.0);
                break;
            case "TANK":
                player.addPotionEffect("RESISTANCE", safeDuration, 0);
                player.addPotionEffect("SLOWNESS", safeDuration, 0);
                player.setMaxHealth(24.0);
                player.setHealth(24.0);
                break;
            case "TRAPPER":
                player.giveItem("COBWEB", 16);
                player.giveItem("TNT", 4);
                player.giveItem("FLINT_AND_STEEL", 1);
                break;
        }
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

    public String getPlayerClass(UUID uuid) { return selectedClasses.getOrDefault(uuid, "NONE"); }
    public void setPlayerClass(UUID uuid, String className) { selectedClasses.put(uuid, className); }
}
