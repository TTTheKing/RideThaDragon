/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.V10lator.RideThaDragon.command;

import de.V10lator.RideThaDragon.RideThaDragon;
import de.V10lator.RideThaDragon.model.V10Dragon;
import java.util.Iterator;
import net.minecraft.server.v1_7_R1.EntityEnderDragon;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEnderDragon;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 *
 * @author Daniel
 */
public class Dragon extends CommandHandler {

    private Player player;
    private String playerName;
    private LivingEntity dragon;
    
    public Dragon(RideThaDragon plugin) {
        super(plugin);
    }

    @Override
    protected Boolean OnCommand(CommandSender sender, String[] args) {
        this.sender = sender;
        
        if (!(sender instanceof Player)) {
            return true;
        }

        if (!sender.hasPermission("ridetha.dragon")) {
            sender.sendMessage(ChatColor.RED + "You don't have permissions to use this command!");
            return true;
        }

        player = (Player)sender;
        playerName = player.getName();
        
        plugin.allowTeleport.add(player);
        
        if (RideThaDragon.dragons.containsKey(playerName)) {
            PlayerHaveDragon();
        } else {
            PlayerDontHaveDragon();
        }
        
        plugin.allowTeleport.remove(player);
        
        return true;
    }

    private void PlayerDontHaveDragon() {
          if (plugin.economy != null) {
              if (!plugin.economy.has(playerName, plugin.price)) {
                  player.sendMessage(ChatColor.RED + "Do you have enough money?");
                  plugin.allowTeleport.remove(player);
                  return;
              }
          }
          Location loc = player.getLocation();
          World w = loc.getWorld();
          String wn = w.getName();
          Block b = loc.getBlock();
          if (plugin.mh.containsKey(wn)) {
              int y = loc.getBlockY();
              int mx = plugin.mh.get(wn);
              if (y < mx) {
                  if (w.getHighestBlockYAt(loc) > y) {
                      sender.sendMessage(ChatColor.RED + "Sorry, no free air above you to spawn a dragon here!");
                      plugin.allowTeleport.remove(player);
                      return;
                  }
                  loc.setY(mx);
                  b = loc.getBlock();
                  int mh = loc.getWorld().getMaxHeight();
                  if (mx < mh) {
                      while (b.getY() < mh && b.getType() != Material.AIR) {
                          b = b.getRelative(BlockFace.UP);
                      }
                      loc.setY(b.getY());
                      b = loc.getBlock();

                  }
              }
          }
          net.minecraft.server.v1_7_R1.World notchWorld = ((CraftWorld) loc.getWorld()).getHandle();

          V10Dragon v10dragon = new V10Dragon(plugin, player, loc, notchWorld);
          if (!notchWorld.addEntity(v10dragon, CreatureSpawnEvent.SpawnReason.CUSTOM)) {
              sender.sendMessage(ChatColor.RED + "Can't spawn a dragon here!");
              plugin.allowTeleport.remove(player);
              return;
          }
          if (plugin.economy != null) {
              plugin.economy.withdrawPlayer(playerName, plugin.price);
          }
          LivingEntity v10Dragon = (LivingEntity) v10dragon.getBukkitEntity();
          RideThaDragon.dragons.put(playerName, v10Dragon);

          v10Dragon.setPassenger(player);
          player.setAllowFlight(true);
    }
    
    private void PlayerHaveDragon() {

       dragon = RideThaDragon.dragons.get(playerName);
       Entity mountedPlayer = dragon.getPassenger();
       
       if (isPlayerNotMounted(mountedPlayer, player)) {
           StartRide();
           return;
       }
       
       DesmountPlayer();
    }
    
    public boolean isPlayerNotMounted(Entity mountedPlayer,Player player) {
        return mountedPlayer == null || mountedPlayer.getUniqueId() != player.getUniqueId();
    }
    
    private void StartRide() {
        V10Dragon d = null;
        for (Entity e : player.getNearbyEntities(20, 20, 20)) {
            if (!(e instanceof EnderDragon)) {
                continue;
            }
            EntityEnderDragon eed = ((CraftEnderDragon) e).getHandle();
            if (eed instanceof V10Dragon) {
                d = (V10Dragon)eed;
                if (d.player.equalsIgnoreCase(playerName)) {
                    player.setAllowFlight(true);
                    break;
                } else {
                    d = null;
                }
            }
        }
        if (d == null) {
            sender.sendMessage(ChatColor.RED + "You dragon is to far away!");
        } else {
            d.getBukkitEntity().setPassenger(player);
        }
    }
    
    public void DesmountPlayer() {
        
        Location loc = player.getLocation();
        loc.setY(loc.getWorld().getHighestBlockAt(loc).getRelative(BlockFace.UP).getY());
        RideThaDragon.dragons.get(playerName).eject();
        player.teleport(loc);
        player.setAllowFlight(false);
        
        Iterator<ComplexEntityPart> pi = ((EnderDragon) dragon).getParts().iterator();
        pi.next();
        ComplexEntityPart b = pi.next();
        Entity pa = b.getPassenger();
        if (pa != null) {
            loc = pa.getLocation();
            loc.setY(loc.getWorld().getHighestBlockAt(loc).getRelative(BlockFace.UP).getY());
            b.eject();
            pa.teleport(loc);
        }
        
    }
    
    @Override
    protected Boolean isInvalid(CommandSender sender, String[] args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
