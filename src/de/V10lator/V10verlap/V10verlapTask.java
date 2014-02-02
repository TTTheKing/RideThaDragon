package de.V10lator.V10verlap;

import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

class V10verlapTask implements Runnable
{
  private final V10verlap plugin;
  private final String world;
  private final String lowerWorld;
  private final String upperWorld;
  private final int minY;
  private final int maxY;
  
  V10verlapTask(V10verlap plugin, String world, String lowerWorld, String upperWorld, int minY, int maxY)
  {
	this.plugin = plugin;
	this.world = world;
	this.lowerWorld = lowerWorld;
	this.upperWorld = upperWorld;
	this.minY = minY;
	this.maxY = maxY;
  }
  
  public void run()
  {
	Server s = plugin.getServer();
	World w = s.getWorld(world);
	Location loc;
	World to;
	int y;
	UUID uuid;
	for(Chunk c: w.getLoadedChunks())
	{
	  for(Entity e: c.getEntities())
	  {
		if(e instanceof LivingEntity && ((LivingEntity)e).isInsideVehicle())
		  return;
		uuid = e.getUniqueId();
		if(plugin.cooldown.contains(uuid))
		  return;
	    loc = e.getLocation();
	    y = loc.getBlockY();
	    if(lowerWorld != null && y < minY)
	    {
		  to = s.getWorld(lowerWorld);
		  if(to == null)
		    continue;
		  loc.setWorld(to);
		  loc.setY(plugin.api.getMaxY(to) - 1);
		  if(!plugin.teleport(e, loc, true))
			s.getLogger().info("["+plugin.getName()+"] WARNING: Could not teleport '"+e.getClass()+"' - Event cancelled by other plugin!");
	    }
	    else if(upperWorld != null && y > maxY)
	    {
		  to = s.getWorld(upperWorld);
		  if(to == null)
		    continue;
		  loc.setWorld(to);
		  loc.setY(plugin.api.getMinY(to) + 1);
		  if(!plugin.teleport(e, loc, true))
			s.getLogger().info("["+plugin.getName()+"] WARNING: Could not teleport '"+e.getClass()+"' - Event cancelled by other plugin!");
	    }
	  }
	}
  }
}
