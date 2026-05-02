package com.example.manhunt.api;

import org.jetbrains.annotations.NotNull;

public interface MScheduler {
    
    interface Task {
        void cancel();
        boolean isCancelled();
    }

    @NotNull Task runTask(@NotNull Runnable runnable);
    @NotNull Task runTaskLater(@NotNull Runnable runnable, long delayTicks);
    @NotNull Task runTaskTimer(@NotNull Runnable runnable, long delayTicks, long periodTicks);
    @NotNull Task runTaskAsync(@NotNull Runnable runnable);
    
    @Deprecated
    default void cancelTask(Object task) {
        if (task instanceof Task) {
            ((Task) task).cancel();
        }
    }
}
