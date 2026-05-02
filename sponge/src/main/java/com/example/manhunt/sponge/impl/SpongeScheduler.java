package com.example.manhunt.sponge.impl;

import com.example.manhunt.api.MScheduler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.plugin.PluginContainer;

import java.util.concurrent.TimeUnit;

public class SpongeScheduler implements MScheduler {
    private final PluginContainer container;

    public SpongeScheduler(PluginContainer container) {
        this.container = container;
    }

    private static class SpongeTaskWrapper implements MScheduler.Task {
        private final ScheduledTask scheduledTask;

        private SpongeTaskWrapper(ScheduledTask scheduledTask) {
            this.scheduledTask = scheduledTask;
        }

        @Override
        public void cancel() {
            scheduledTask.cancel();
        }

        @Override
        public boolean isCancelled() {
            return scheduledTask.isCancelled();
        }
    }

    @Override
    public @NotNull MScheduler.Task runTask(@NotNull Runnable runnable) {
        return new SpongeTaskWrapper(Sponge.server().scheduler().submit(org.spongepowered.api.scheduler.Task.builder()
                .execute(runnable)
                .plugin(container)
                .build()));
    }

    @Override
    public @NotNull MScheduler.Task runTaskLater(@NotNull Runnable runnable, long delayTicks) {
        return new SpongeTaskWrapper(Sponge.server().scheduler().submit(org.spongepowered.api.scheduler.Task.builder()
                .execute(runnable)
                .delay(delayTicks * 50L, TimeUnit.MILLISECONDS)
                .plugin(container)
                .build()));
    }

    @Override
    public @NotNull MScheduler.Task runTaskTimer(@NotNull Runnable runnable, long delayTicks, long periodTicks) {
        return new SpongeTaskWrapper(Sponge.server().scheduler().submit(org.spongepowered.api.scheduler.Task.builder()
                .execute(runnable)
                .delay(delayTicks * 50L, TimeUnit.MILLISECONDS)
                .interval(periodTicks * 50L, TimeUnit.MILLISECONDS)
                .plugin(container)
                .build()));
    }

    @Override
    public @NotNull MScheduler.Task runTaskAsync(@NotNull Runnable runnable) {
        return new SpongeTaskWrapper(Sponge.asyncScheduler().submit(org.spongepowered.api.scheduler.Task.builder()
                .execute(runnable)
                .plugin(container)
                .build()));
    }
}
