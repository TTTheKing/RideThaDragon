package de.V10lator.V10verlap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class V10verlap extends JavaPlugin
{
  final V10verlap_API api = new V10verlap_API(this);
  final HashSet<UUID> cooldown = new HashSet<UUID>();
  Configuration config;
  
  public void onEnable()
  {
	if(!(new File(getDataFolder(), "config.yml")).exists())
	{
	  config = getConfig();
	  config.set("world.upper", "world_the_end");
	  config.set("world.lower", "world_nether");
	  config.set("world.minY", 2);
	  config.set("world.maxY", 126);
	  config.set("world_the_end.lower", "world");
	  config.set("world_nether.upper", "world");
	  saveConfig();
	  reloadConfig();
	}
	config = getConfig();
	Server s = getServer();
	PluginManager pm = s.getPluginManager();
	pm.registerEvents(new V10vWL(this), this);
	restartTasks();
	PluginDescriptionFile pdf = getDescription();
	s.getLogger().info("["+pdf.getName()+"] v"+pdf.getVersion()+" enabled!");
  }
  
  public void onDisable()
  {
	Server s = getServer();
	s.getScheduler().cancelTasks(this);
	s.getLogger().info("["+getDescription().getName()+"] Disabled!");
  }
  
  void restartTasks()
  {
	Server s = getServer();
	BukkitScheduler bs = s.getScheduler();
	bs.cancelTasks(this);
	List<World> worlds = s.getWorlds();
	Set<String> nodes = config.getKeys(false);
	ArrayList<String> handled = new ArrayList<String>();
	for(World world: worlds)
	{
	  String wn = world.getName();
	  if(nodes.contains(wn))
		handled.add(wn);
	}
	int c = handled.size();
	if(c == 0)
	{
	  getServer().getLogger().info("["+getName()+"] WARNING: Can't find any world in the config!");
	  getServer().getLogger().info("["+getName()+"] Hopefully they will be loaded later. ;)");
	  return;
	}
	int delay;
	if(c < 10)
	  delay = 10 / c;
	else
	  delay = 1;
	for(String world: handled)
	{
	  while(delay < 0)
		delay += 10;
	  while(delay > 9)
		delay -= 10;
	  if(delay == 0)
		delay = 1;
	  World lower = api.getLowerWorld(world);
	  World upper = api.getUpperWorld(world);
	  String lowerWorld;
	  String upperWorld;
	  if(lower != null)
		lowerWorld = lower.getName();
	  else
		lowerWorld = null;
	  if(upper != null)
		upperWorld = upper.getName();
	  else
		upperWorld = null;
	  bs.scheduleSyncRepeatingTask(this, new V10verlapTask(this, world, lowerWorld, upperWorld, api.getMinY(world), api.getMaxY(world)), delay, 10L);
	  delay += delay;
	}
  }
  
  public V10verlap_API getAPI()
  {
	return api;
  }
  
  boolean teleport(Entity e, Location to, boolean cd)
  {
	Server s = getServer();
	net.minecraft.server.v1_7_R1.Entity entity = ((CraftEntity)e).getHandle();
	V10verlapEvent event;
	
	//Create the event
	if(e instanceof Player)
	  event = new PlayerWorldToWorldTpEvent((Player)e, to);
	else if(e instanceof LivingEntity && entity.passenger != null)
	  event = new VehicleWorldToWorldTpEvent((LivingEntity)e, entity.passenger.getBukkitEntity(), to);
	else if(e instanceof Item)
	  event = new ItemWorldToWorldTpEvent((Item)e, to);
	else
	  event = new EntityWorldToWorldTpEvent(e, to);
	//Call and parse it
	s.getPluginManager().callEvent(event);
	if(event.isCancelled())
	  return false;
	if(!(to.equals(event.to)))
	{
	  s.getLogger().info("["+getName()+"] Another plugin changed the destination!");
	  to = event.to;
	}
	
	BukkitScheduler bs = s.getScheduler();
	
	if(cd)
	{
	  UUID uuid = e.getUniqueId();
	  cooldown.add(uuid);
	  bs.scheduleSyncDelayedTask(this, new CooldownTask(cooldown, uuid), 100L);
	}
	
	World w = to.getWorld();
	w.getChunkAt(to).load();
	bs.scheduleSyncDelayedTask(this, new ChunkUnloader(to), 1L);
	
	if(e instanceof Player)
	  return e.teleport(to);
	
    //transfer entity cross-worlds
    if(entity.passenger != null)
    {
      //set out of vehicle?
      net.minecraft.server.v1_7_R1.Entity passenger = entity.passenger;
      entity.passenger = null;
      passenger.vehicle = null;
      if(teleport(passenger.getBukkitEntity(), to, cd))
    	getServer().getScheduler().scheduleSyncDelayedTask(this, new Teleporter(passenger, entity), 0L);
      else
      {
    	entity.passenger = passenger;
    	passenger.vehicle = entity;
    	return false;
      }
    }
    //teleport this entity
    ((WorldServer)entity.world).tracker.untrackEntity(entity);
    entity.world.removeEntity(entity);
    entity.dead = false;
    WorldServer newworld = ((CraftWorld)w).getHandle();
    entity.world = newworld;
    entity.setLocation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
    entity.world.addEntity(entity);
    ((WorldServer)entity.world).tracker.track(entity);
    return true;
  }
  
  private class Teleporter implements Runnable
  {
	private final net.minecraft.server.Entity e;
	private final net.minecraft.server.Entity v;
	
	private Teleporter(net.minecraft.server.Entity e, net.minecraft.server.Entity v)
	{
	  this.e = e;
	  this.v = v;
	}
	
	public void run()
	{
	  e.setPassengerOf(v);
	}
  }
  
  private class ChunkUnloader implements Runnable
  {
	private final Location to;
	
	private ChunkUnloader(Location to)
	{
	  this.to = to;
	}
	
	public void run()
	{
	  to.getChunk().unload(true, true);
	}
  }
}
