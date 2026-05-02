package com.example.manhunt.scenario;

import com.example.manhunt.api.AbstractScenario;
import com.example.manhunt.manager.LootPoolManager;

public class LootPoolScenario extends AbstractScenario {
    private final LootPoolManager lootPoolManager;

    public LootPoolScenario(LootPoolManager lootPoolManager) {
        super("LOOTPOOL", "Özel Eşya Havuzu");
        this.lootPoolManager = lootPoolManager;
    }

    @Override
    public void onStart() {
        lootPoolManager.setEnabled(true);
    }

    @Override
    public void onStop() {
        lootPoolManager.setEnabled(false);
    }
}
