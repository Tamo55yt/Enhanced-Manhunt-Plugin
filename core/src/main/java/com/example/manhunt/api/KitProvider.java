package com.example.manhunt.api;

import org.jetbrains.annotations.NotNull;

public interface KitProvider {
    void applyKit(@NotNull MPlayer player, @NotNull String role);
    void saveKit(@NotNull MPlayer player, @NotNull String role);
}
