package com.example.manhunt;

import com.example.manhunt.bukkit.BukkitCompassManager;
import com.example.manhunt.bukkit.BukkitGameManager;
import com.example.manhunt.bukkit.impl.BukkitMessageManager;
import com.example.manhunt.bukkit.impl.BukkitPlatform;
import com.example.manhunt.manager.LootPoolManager;
import com.example.manhunt.manager.ScenarioManager;
import com.example.manhunt.manager.ScoreboardManager;
import com.example.manhunt.menu.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Main extends JavaPlugin {

    private static Main instance;

    private BukkitPlatform platform;
    private KitManager kitManager;
    private BukkitMessageManager messageManager;
    private BukkitGameManager gameManager;
    private BukkitCompassManager compassManager;
    private ScoreboardManager scoreboardManager;
    private ScenarioManager scenarioManager;
    private LootPoolManager lootPoolManager;
    private MenuManager menuManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        this.platform = new BukkitPlatform(this);
        this.kitManager = new KitManager(this);
        this.messageManager = new BukkitMessageManager(this.platform);
        this.gameManager = new BukkitGameManager(this);
        this.compassManager = new BukkitCompassManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.lootPoolManager = new LootPoolManager(this.platform);
        this.scenarioManager = new ScenarioManager(this.platform, this.gameManager, this.messageManager, this.lootPoolManager);
        this.menuManager = new MenuManager(this);
        Bukkit.getPluginManager().registerEvents(this.menuManager, this);

        getCommand("manhunt").setExecutor(new ManhuntCommand(this));
        getCommand("manhunt").setTabCompleter(new ManhuntCommand(this));
        
        Bukkit.getPluginManager().registerEvents(new ManhuntListener(this), this);
        
        // PlaceholderAPI check
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new ManhuntExpansion(this).register();
        }

        getLogger().info("Manhunt (Bukkit) has been enabled!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.stopTasks();
        if (scoreboardManager != null) scoreboardManager.stopTasks();
        instance = null;
    }

    @NotNull public static Main getInstance() { return instance; }
    @NotNull public BukkitPlatform getPlatform() { return platform; }
    @NotNull public KitManager getKitManager() { return kitManager; }
    @NotNull public BukkitMessageManager getMessageManager() { return messageManager; }
    @NotNull public BukkitGameManager getGameManager() { return gameManager; }
    @NotNull public BukkitCompassManager getCompassManager() { return compassManager; }
    @NotNull public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    @NotNull public ScenarioManager getScenarioManager() { return scenarioManager; }
    @NotNull public LootPoolManager getLootPoolManager() { return lootPoolManager; }
    @NotNull public MenuManager getMenuManager() { return menuManager; }

    public void updateTabList() { scoreboardManager.updateTabList(); }
}
