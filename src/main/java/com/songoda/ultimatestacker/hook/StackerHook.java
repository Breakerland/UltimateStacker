package com.songoda.ultimatestacker.hook;

import com.songoda.ultimatestacker.entity.EntityStack;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface StackerHook {

    /**
     * Check if prevent support is enable.
     */
	boolean prevent();

    /**
     * Check if correct exp amount support is enable.
     */
	boolean correctAmount();

    /**
     * Applies experience to a player for a killed stack.
     *
     * @param player The player
     * @param entityStack The stack that was killed
     */
    void applyExperience(Player player, EntityStack entityStack);

    /**
     * Applies metadata to an entity.
     *
     * @param entity The entity
     */
	void setTrait(Entity entity);
}
