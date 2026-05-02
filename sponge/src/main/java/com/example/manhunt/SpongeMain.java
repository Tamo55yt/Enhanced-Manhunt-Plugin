package com.example.manhunt;

import com.example.manhunt.manager.CompassManager;
import com.example.manhunt.manager.LootPoolManager;
import com.example.manhunt.manager.ScenarioManager;
import com.example.manhunt.message.MessageManager;
import com.example.manhunt.sponge.SpongeCommand;
import com.example.manhunt.sponge.SpongeCompassManager;
import com.example.manhunt.sponge.SpongeGameManager;
import com.example.manhunt.sponge.SpongeListener;
import com.example.manhunt.sponge.impl.SpongePlatform;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;

import com.example.manhunt.sponge.manager.SpongeScoreboardManager;

@Plugin("manhunt")
public class SpongeMain {

    private final PluginContainer container;
    private final Logger logger;
    private final Path configDir;

    private SpongePlatform platform;
    private MessageManager messageManager;
    private SpongeGameManager gameManager;
    private CompassManager compassManager;
    private ScenarioManager scenarioManager;
    private LootPoolManager lootPoolManager;
    private SpongeScoreboardManager scoreboardManager;

    @Inject
    public SpongeMain(PluginContainer container, Logger logger, @ConfigDir(sharedRoot = false) Path configDir) {
        this.container = container;
        this.logger = logger;
        this.configDir = configDir;
    }

    @Listener
    public void onServerStart(StartedEngineEvent<Server> event) {
        this.platform = new SpongePlatform(container, configDir);
        this.messageManager = new MessageManager(platform);

        this.gameManager = new SpongeGameManager(this);
        this.compassManager = new SpongeCompassManager(this);
        this.lootPoolManager = new LootPoolManager(platform);
        this.scenarioManager = new ScenarioManager(platform, gameManager, messageManager, lootPoolManager);
        this.scoreboardManager = new SpongeScoreboardManager(this);

        // Register Listeners
        Sponge.eventManager().registerListeners(container, new SpongeListener(this));

        logger.info("Manhunt (Sponge) has been enabled!");
    }

    @Listener
    public void onRegisterCommands(org.spongepowered.api.event.lifecycle.RegisterCommandEvent<org.spongepowered.api.command.Command.Parameterized> event) {
        new SpongeCommand(this).register(event);
    }

    @Listener
    public void onServerStop(StoppingEngineEvent<Server> event) {
        if (scoreboardManager != null) scoreboardManager.stopTasks();
        logger.info("Manhunt (Sponge) has been disabled!");
    }

    public PluginContainer getContainer() { return container; }
    public Logger getLogger() { return logger; }
    public SpongePlatform getPlatform() { return platform; }
    public MessageManager getMessageManager() { return messageManager; }
    public SpongeGameManager getGameManager() { return gameManager; }
    public CompassManager getCompassManager() { return compassManager; }
    public ScenarioManager getScenarioManager() { return scenarioManager; }
    public LootPoolManager getLootPoolManager() { return lootPoolManager; }
    public SpongeScoreboardManager getScoreboardManager() { return scoreboardManager; }
}
