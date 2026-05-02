package com.example.manhunt.scenario;

import com.example.manhunt.api.AbstractScenario;
import com.example.manhunt.manager.LootPoolManager;

public class LootLevelingScenario extends AbstractScenario {
    private final LootPoolManager lootPoolManager;

    public LootLevelingScenario(LootPoolManager lootPoolManager) {
        super("LOOTLEVELING", "Seviyeli Eşya Sistemi");
        this.lootPoolManager = lootPoolManager;
    }

    @Override
    public void onStart() {
        lootPoolManager.setEnabled(true);
        lootPoolManager.setLevelingEnabled(true);
        lootPoolManager.resetProgress();
    }

    @Override
    public void onStop() {
        lootPoolManager.setEnabled(false);
        lootPoolManager.setLevelingEnabled(false);
    }
}
