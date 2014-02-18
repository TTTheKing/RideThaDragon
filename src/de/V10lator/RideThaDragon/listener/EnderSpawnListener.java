/*
 * Copyright (c) 2012-2013 Sean Porter <glitchkey@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.V10lator.RideThaDragon.listener;

import de.V10lator.RideThaDragon.RideThaDragon;
import de.V10lator.RideThaDragon.model.V10Dragon;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.PortalType;

public class EnderSpawnListener implements Listener {

    private final RideThaDragon plugin;

    public EnderSpawnListener(RideThaDragon plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDragonEggTeleport(BlockFromToEvent event) {
        if (event.getBlock().getType().getId() != 122) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityCreatePortal(EntityCreatePortalEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof V10Dragon)) {
            return;
        }

        List<BlockState> blocks = new ArrayList(event.getBlocks());

        for (BlockState block : event.getBlocks()) {
            if (block.getType().getId() == 122) {
                blocks.remove(block);
            }


            if (block.getType().getId() == 7 || block.getType().getId() == 119) {
                blocks.remove(block);
            } else if (block.getType().getId() == 0 || block.getType().getId() == 50) {
                blocks.remove(block);
            } else if (block.getType().getId() == 122) {
                blocks.remove(block);

                Location location = entity.getLocation();
                ItemStack item = new ItemStack(block.getType());

                entity.getWorld().dropItemNaturally(location, item);
            }
        }

        if (blocks.size() != event.getBlocks().size()) {
            event.setCancelled(true);

            LivingEntity newEntity = (LivingEntity) entity;
            PortalType type = event.getPortalType();
            EntityCreatePortalEvent newEvent;
            newEvent = new EntityCreatePortalEvent(newEntity, blocks, type);

            plugin.getServer().getPluginManager().callEvent(newEvent);

            if (!newEvent.isCancelled()) {
                for (BlockState blockState : blocks) {
                    int id = blockState.getTypeId();
                    byte data = blockState.getRawData();
                    blockState.getBlock().setTypeIdAndData(id, data, false);
                }
            }
        }
    }
}
