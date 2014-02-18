package de.V10lator.RideThaDragon.listener;

import de.V10lator.RideThaDragon.RideThaDragon;
import de.V10lator.RideThaDragon.SmokeTask;
import de.V10lator.RideThaDragon.model.V10Dragon;
import java.util.Random;

import net.minecraft.server.v1_7_R1.EntityFireball;
import net.minecraft.server.v1_7_R1.EntityLargeFireball;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PlayerListener implements Listener
{
  private final RideThaDragon plugin;
  
  public PlayerListener(RideThaDragon plugin)
  {
	this.plugin = plugin;
  }
  
  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled= true)
  public void onPlayerTeleport(PlayerTeleportEvent event)
  {
	Player p = event.getPlayer();
	String pn = p.getName();
	if(p.isInsideVehicle() && RideThaDragon.dragons.containsKey(pn) && !plugin.allowTeleport.contains(p))
	{
	  Entity pa = RideThaDragon.dragons.get(pn).getPassenger();
	  if(pa != null && pa.getUniqueId() == p.getUniqueId())
	  {
		event.setCancelled(true);
		p.sendMessage(ChatColor.RED+"Can't teleport while riding a dragon!");
	  }
	}
  }
  
  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled= true)
  public void onEntityExplode(EntityExplodeEvent event)
  {
	if(event.getEntity() instanceof V10Dragon && plugin.stopGrief.contains(event.getLocation().getWorld().getName()))
	  event.setCancelled(true);
  }
  
  @EventHandler(ignoreCancelled = false)
  public void shoot(PlayerInteractEvent event)
  {
	if(event.getAction() != Action.LEFT_CLICK_AIR)
	  return;
	Player p = event.getPlayer();
	if(!p.hasPermission("ridetha.shoot"))
	  return;
	String pn = p.getName();
	if(!RideThaDragon.dragons.containsKey(pn))
	  return;
	LivingEntity ld = RideThaDragon.dragons.get(pn);
	Entity pa = ld.getPassenger();
	if(pa == null || pa.getUniqueId() != p.getUniqueId())
	  return;
	Location loc = ld.getLocation();
	float yaw = loc.getYaw();
	
	if(yaw < 22.5F || yaw > 337.5F)
	{
	  loc.setZ(loc.getZ() - 10.0F);
	}
	else if(yaw < 67.5F)
	{
	  loc.setZ(loc.getZ() - 7.0F);
	  loc.setX(loc.getX() + 7.0F);
	}
	else if(yaw < 112.5F)
	{
	  loc.setX(loc.getX() + 10.0F);
	}
	else if(yaw < 157.5F)
	{
	  loc.setZ(loc.getZ() + 7.0F);
	  loc.setX(loc.getX() + 7.0F);
	}
	else if(yaw < 202.5F)
	{
	  loc.setZ(loc.getZ() + 10.0F);
	}
	else if(yaw < 247.5F)
	{
	  loc.setZ(loc.getZ() + 7.0F);
	  loc.setX(loc.getX() - 7.0F);
	}
	else if(yaw < 292.5)
	{
	  loc.setX(loc.getX() - 10.0F);
	}
	else
	{
	  loc.setZ(loc.getZ() - 7.0F);
	  loc.setX(loc.getX() - 7.0F);
	}
	
	yaw += 180.0F;
	while(yaw > 360)
	  yaw -=360;
	loc.setYaw(yaw);
	
	V10Dragon vd = (V10Dragon)((CraftLivingEntity)ld).getHandle();
	if(vd.upDown == 1)
	  loc.setPitch(45.0F);
	else if(vd.upDown == 2)
	  loc.setPitch(-45.0F);
	else
	  loc.setPitch(0.0F);
	
	net.minecraft.server.v1_7_R1.World nw = ((CraftWorld)ld.getWorld()).getHandle();
	
	EntityFireball nf = new EntityLargeFireball(nw, ((CraftPlayer)p).getHandle(), loc.getX(), loc.getY(), loc.getZ());

	nw.addEntity(nf, SpawnReason.CUSTOM);
	Fireball f = (Fireball)nf.getBukkitEntity();
	f.teleport(loc);
	double speed = 5.0D;
	if(!vd.brr)
	{
	  speed += plugin.rideSpeed;
	  speed += vd.fl;
	}
	Vector v = loc.getDirection().multiply(speed);
	f.setDirection(v);
	f.setVelocity(v);
  }
  
  @EventHandler(ignoreCancelled = true)
  public void unloader(ChunkUnloadEvent event)
  {
	Chunk c = event.getChunk();
	int x = c.getX();
	int z = c.getZ();
	String world = c.getWorld().getName();
	for(LivingEntity d: RideThaDragon.dragons.values())
	{
	  c = d.getLocation().getChunk();
	  if(c.getWorld().getName().equals(world) &&
			  c.getX() == x &&
			  c.getZ() == z)
	  {
		d.remove();
	  }
	}
  }
  @EventHandler(ignoreCancelled = true)
  public void reloader(ChunkLoadEvent event)
  {
	Chunk c = event.getChunk();
	int x = c.getX();
	int z = c.getZ();
	String world = c.getWorld().getName();
	Location loc;
	net.minecraft.server.v1_7_R1.World notchWorld;
	V10Dragon v10dragon;
	for(LivingEntity d: RideThaDragon.dragons.values())
	{
	  loc = d.getLocation();
	  c = loc.getChunk();
	  if(c.getWorld().getName().equals(world) &&
			  c.getX() == x &&
			  c.getZ() == z)
	  {
		notchWorld = ((CraftWorld)loc.getWorld()).getHandle();
		v10dragon = (V10Dragon)((CraftLivingEntity)d).getHandle();
		if(!notchWorld.addEntity(v10dragon, SpawnReason.CUSTOM))
		  plugin.getLogger().info("Can't respawn the dragon of "+v10dragon.player);
	  }
	}
  }
}
