package com.example.manhunt.api;

public abstract class AbstractScenario implements Scenario {
    protected final String id;
    protected final String name;
    protected boolean active;

    public AbstractScenario(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void setActive(boolean active) { this.active = active; }

    @Override
    public void onStart() {}

    @Override
    public void onStop() {}

    @Override
    public void onTick() {}
}
