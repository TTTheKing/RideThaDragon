package de.V10lator.V10verlap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

class V10vWL implements Listener
{
  private final V10verlap plugin;
  
  V10vWL(V10verlap plugin)
  {
	this.plugin = plugin;
  }
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void onWorldLoad(WorldLoadEvent event)
  {
	plugin.restartTasks();
  }
  
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWorldUnload(WorldUnloadEvent event)
  {
	plugin.restartTasks();
  }
}
