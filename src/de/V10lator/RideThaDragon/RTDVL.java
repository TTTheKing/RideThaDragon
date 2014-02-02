package de.V10lator.RideThaDragon;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

class RTDVL implements Listener
{
  private final RideThaDragon plugin;
  
  RTDVL(RideThaDragon plugin)
  {
	this.plugin = plugin;
  }
  
//  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//  public void v10verlapTP(VehicleWorldToWorldTpEvent event)
//  {
//	Entity e = event.getPassenger();
//	if(!(e instanceof Player))
//	  return;
//	Player p = (Player)e;
//	if(!RideThaDragon.dragons.containsKey(p.getName()))
//	  return;
//	plugin.allowTeleport.add(p);
//	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RemoveTeleporter(p), 1L);
//  }
//  
//  private class RemoveTeleporter implements Runnable
//  {
//	private final Player p;
//	
//	private RemoveTeleporter(Player p)
//	{
//	  this.p = p;
//	}
//	
//	public void run()
//	{
//	  plugin.allowTeleport.remove(p);
//	}
//  }
}
