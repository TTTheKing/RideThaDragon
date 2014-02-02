package de.V10lator.V10verlap;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class V10verlap_API
{
  private final V10verlap plugin;
  private final double version = 1.3D;
  
  V10verlap_API(V10verlap plugin)
  {
	this.plugin = plugin;
  }
  
  /** Returns the APIs version as a double.
   *  The first number will change whenever there's an API breakage while
   *  the second will change whenever there are new things.
   * 
   * @return double
   */
  public double getVersion()
  {
	return version;
  }
  
  /** Returns the minimum Y
   * 
   * @param world - The world
   * @return int
   */
  public int getMinY(String world)
  {
	return plugin.config.getInt(world+".minY", 0);
  }
  
  /** Returns the minimum Y
   * 
   * @param world - The world
   * @return int
   */
  public int getMinY(World world)
  {
	return getMinY(world.getName());
  }
  
  /** Returns the maximum Y
   * 
   * @param world - The world
   * @return int
   */
  public int getMaxY(String world)
  {
	if(plugin.config.contains(world+".maxY"))
	  return plugin.config.getInt(world+".maxY");
	World w = plugin.getServer().getWorld(world);
	if(w == null)
	  return -1;
	return w.getMaxHeight();
  }
  
  /** Returns the maximum Y
   * 
   * @param world - The world
   * @return int
   */
  public int getMaxY(World world)
  {
	return getMaxY(world.getName());
  }
  
  /** Returns the upper world
   * 
   * @param world - The world
   * @return World - The upper world
   */
  public World getUpperWorld(String world)
  {
	if(plugin.config.contains(world+".upper"))
	  return plugin.getServer().getWorld(plugin.config.getString(world+".upper"));
	return null;
  }
  
  /** Returns the upper world
   * 
   * @param world - The world
   * @return World - The upper world
   */
  public World getUpperWorld(World world)
  {
	if(world == null)
	  return null;
	return getUpperWorld(world.getName());
  }
  
  /** Returns the lower world
   * 
   * @param world - The world
   * @return World - The upper world
   */
  public World getLowerWorld(String world)
  {
	if(plugin.config.contains(world+".lower"))
	  return plugin.getServer().getWorld(plugin.config.getString(world+".lower"));
	return null;
  }
  
  /** Returns the lower world
   * 
   * @param world - The world
   * @return World - The upper world
   */
  public World getLowerWorld(World world)
  {
	if(world == null)
	  return null;
	return getLowerWorld(world.getName());
  }
  
  /** Sets the cooldown for an entity to 5 seconds.
   * 
   * @param entity - The Entity
   */
  public void addCooldown(Entity entity)
  {
	if(entity == null)
	  return;
	UUID uuid = entity.getUniqueId();
	plugin.cooldown.add(uuid);
	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new CooldownTask(plugin.cooldown, uuid), 100L);
  }
  
  /** Sets the cooldown for an entity.
   * 
   * @param entity - The Entity
   * @param ticks - The cooldown in ticks
   */
  public void addCooldown(Entity entity, Long ticks)
  {
	if(entity == null)
	  return;
	UUID uuid = entity.getUniqueId();
	plugin.cooldown.add(uuid);
	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new CooldownTask(plugin.cooldown, uuid), ticks);
  }
  
  /** Returns if an entity has a cooldown.
   * 
   * @param entity - The Entity
   */
  public boolean hasCooldown(Entity entity)
  {
	if(entity == null)
	  return false;
	return plugin.cooldown.contains(entity.getUniqueId());
  }
  
  /** Teleport an Entity with v10verlaps teleport method.
   *  This is a safe method for world to world TPs.
   *  This will ignore cooldowns.
   * 
   * @param entity - The Entity
   * @param to - The Location to teleport to.
   */
  public boolean teleport(Entity entity, Location to)
  {
	if(entity == null || to == null)
	  return false;
	return plugin.teleport(entity, to, false);
  }
  
  /** Teleport an Entity with v10verlaps teleport method.
   *  This is a safe method for world to world TPs.
   *  This will check for (but not set) cooldowns.
   * 
   * @param entity - The Entity
   * @param to - The Location to teleport to.
   * @param cooldown - Check for cooldown?
   */
  public boolean teleport(Entity entity, Location to, boolean cooldown)
  {
	if(entity == null || to == null)
	  return false;
	if(cooldown && plugin.cooldown.contains(entity.getUniqueId()))
	  return false;
	return plugin.teleport(entity, to, false);
  }
}
