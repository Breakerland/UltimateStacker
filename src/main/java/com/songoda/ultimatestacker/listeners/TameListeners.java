package com.songoda.ultimatestacker.listeners;

import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.entity.EntityStack;
import com.songoda.ultimatestacker.entity.EntityStackManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class TameListeners implements Listener {

    private UltimateStacker plugin;

    public TameListeners(UltimateStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTame(EntityTameEvent event) {
        Entity entity = event.getEntity();

        EntityStackManager stackManager = plugin.getEntityStackManager();
        if (!stackManager.isStacked(entity)) return;

        Tameable tameable = (Tameable) entity;

        EntityStack stack = plugin.getEntityStackManager().getStack(entity);

        if (stack.getAmount() <= 1) return;

        LivingEntity newEntity = plugin.getEntityUtils().newEntity((LivingEntity) tameable);

        EntityStack second = plugin.getEntityStackManager().addStack(new EntityStack(newEntity, stack.getAmount() - 1));
        stack.setAmount(1);
        second.setAmount(stack.getAmount() - 1);
        plugin.getEntityStackManager().removeStack(entity);
        entity.setVelocity(getRandomVector());
    }

    private Vector getRandomVector() {
        return new Vector(ThreadLocalRandom.current().nextDouble(-1, 1.01), 0, ThreadLocalRandom.current().nextDouble(-1, 1.01)).normalize().multiply(0.5);
    }
}
