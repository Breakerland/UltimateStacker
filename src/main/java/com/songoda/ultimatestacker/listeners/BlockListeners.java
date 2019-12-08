package com.songoda.ultimatestacker.listeners;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.events.SpawnerBreakEvent;
import com.songoda.ultimatestacker.events.SpawnerPlaceEvent;
import com.songoda.ultimatestacker.settings.Settings;
import com.songoda.ultimatestacker.spawner.SpawnerStack;
import com.songoda.ultimatestacker.utils.Methods;

public class BlockListeners implements Listener {

	private final UltimateStacker plugin;

	public BlockListeners(UltimateStacker plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSpawnerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		ItemStack item = event.getPlayer().getInventory().getItemInHand();

		if (block == null || block.getType() != CompatibleMaterial.SPAWNER.getMaterial() || item.getType() != CompatibleMaterial.SPAWNER.getMaterial() || event.getAction() == Action.LEFT_CLICK_BLOCK)
			return;

		List<String> disabledWorlds = Settings.DISABLED_WORLDS.getStringList();
		if (disabledWorlds.stream().anyMatch(worldStr -> event.getPlayer().getWorld().getName().equalsIgnoreCase(worldStr)))
			return;

		if (!plugin.spawnersEnabled())
			return;

		BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
		CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();

		EntityType itemType = cs.getSpawnedType();

		int itemAmount = getSpawnerAmount(item);
		int specific = plugin.getSpawnerFile().getInt("Spawners." + cs.getSpawnedType().name() + ".Max Stack Size");
		int maxStackSize = specific == -1 ? Settings.MAX_STACK_SPAWNERS.getInt() : specific;

		cs = (CreatureSpawner) block.getState();

		EntityType blockType = cs.getSpawnedType();

		event.setCancelled(true);

		if (itemType == blockType) {
			SpawnerStack stack = plugin.getSpawnerStackManager().getSpawner(block);
			if (player.isSneaking())
				return;
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (stack.getAmount() == maxStackSize)
					return;

				ItemStack overflowItem = null;
				if (stack.getAmount() + itemAmount > maxStackSize) {
					overflowItem = Methods.getSpawnerItem(blockType, stack.getAmount() + itemAmount - maxStackSize);
					itemAmount = maxStackSize - stack.getAmount();
				}

				SpawnerPlaceEvent placeEvent = new SpawnerPlaceEvent(player, block, blockType, itemAmount);
				Bukkit.getPluginManager().callEvent(placeEvent);
				if (placeEvent.isCancelled()) {
					event.setCancelled(true);
					return;
				}

				if (overflowItem != null)
					if (player.getInventory().firstEmpty() == -1)
						block.getWorld().dropItemNaturally(block.getLocation().add(.5, 0, .5), overflowItem);
					else
						player.getInventory().addItem(overflowItem);

				stack.setAmount(stack.getAmount() + itemAmount);
				plugin.updateHologram(stack);
				Methods.takeItem(player, itemAmount);
			}
		}

		plugin.updateHologram(block);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onSpawnerPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();

		if (!event.isCancelled()) {
			if (block.getType() != CompatibleMaterial.SPAWNER.getMaterial() || !plugin.spawnersEnabled())
				return;

			CreatureSpawner cs = (CreatureSpawner) block.getState();
			CreatureSpawner cs2 = (CreatureSpawner) ((BlockStateMeta) event.getItemInHand().getItemMeta()).getBlockState();
			int amount = getSpawnerAmount(event.getItemInHand());

			SpawnerPlaceEvent placeEvent = new SpawnerPlaceEvent(player, block, cs2.getSpawnedType(), amount);
			Bukkit.getPluginManager().callEvent(placeEvent);
			if (placeEvent.isCancelled()) {
				event.setCancelled(true);
				return;
			}

			SpawnerStack stack = plugin.getSpawnerStackManager().addSpawner(new SpawnerStack(block.getLocation(), amount));
			plugin.getDataManager().createSpawner(stack);

			cs.setSpawnedType(cs2.getSpawnedType());
			cs.update();

			plugin.updateHologram(stack);
		}

		plugin.updateHologram(block);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block.getType() != CompatibleMaterial.SPAWNER.getMaterial())
			return;

		if (!plugin.spawnersEnabled())
			return;
		event.setExpToDrop(0);

		CreatureSpawner cs = (CreatureSpawner) block.getState();

		EntityType blockType = cs.getSpawnedType();

		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInHand();

		SpawnerStack stack = plugin.getSpawnerStackManager().getSpawner(block);

		event.setCancelled(true);

		int amt = 1;
		boolean remove = false;

		if (player.isSneaking() && Settings.SNEAK_FOR_STACK.getBoolean()) {
			amt = stack.getAmount();
			remove = true;
		} else if (stack.getAmount() <= 1)
			remove = true;

		SpawnerBreakEvent breakEvent = new SpawnerBreakEvent(player, block, blockType, amt);
		Bukkit.getPluginManager().callEvent(breakEvent);
		if (breakEvent.isCancelled())
			return;

		if (remove) {
			event.setCancelled(false);
			plugin.clearHologram(stack);
			SpawnerStack spawnerStack = plugin.getSpawnerStackManager().removeSpawner(block.getLocation());
			plugin.getDataManager().deleteSpawner(spawnerStack);
		} else {
			stack.setAmount(stack.getAmount() - 1);
			plugin.updateHologram(stack);
		}

		if (player.hasPermission("ultimatestacker.spawner.nosilkdrop") || item.getEnchantments().containsKey(Enchantment.SILK_TOUCH) && player.hasPermission("ultimatestacker.spawner.silktouch")) {
			ItemStack spawner = Methods.getSpawnerItem(blockType, amt);
			if (player.getInventory().firstEmpty() == -1 || !Settings.SPAWNERS_TO_INVENTORY.getBoolean())
				block.getWorld().dropItemNaturally(block.getLocation().add(.5, 0, .5), spawner);
			else
				player.getInventory().addItem(spawner);
		}

	}

	private int getSpawnerAmount(ItemStack item) {
		if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
			return 1;
		if (item.getItemMeta().getDisplayName().contains(":")) {
			int amt = NumberUtils.toInt(item.getItemMeta().getDisplayName().replace("\u00A7", "").replace(";", "").split(":")[0], 1);
			return amt == 0 ? 1 : amt;
		}
		return 1;
	}
}
