package de.V10lator.RideThaDragon;

import de.V10lator.RideThaDragon.model.V10Dragon;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;

public class SmokeTask implements Runnable
{
  private final RideThaDragon plugin;
  private final V10Dragon d;
  int pid;
  short c = 0;
  
  public SmokeTask(RideThaDragon plugin, V10Dragon d)
  {
	this.plugin = plugin;
	this.d = d;
  }
	
  public void setPid(int pid)
  {
	this.pid = pid;
  }
  
  @Override
  public void run()
  {
	World w = d.world.getWorld();
	if(w == null || ++c > 20)
	  plugin.getServer().getScheduler().cancelTask(pid);
	else
	  w.playEffect(new Location(w, d.bv.locX, d.bv.locY + 2, d.bv.locZ), Effect.SMOKE, 4);
  }
}
