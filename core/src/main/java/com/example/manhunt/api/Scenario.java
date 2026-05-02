package com.example.manhunt.api;

public interface Scenario {
    String getId();
    String getName();
    void onStart();
    void onStop();
    void onTick();
    boolean isActive();
    void setActive(boolean active);
}
