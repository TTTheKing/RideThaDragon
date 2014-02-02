package de.V10lator.RideThaDragon;

import java.io.File;
import java.util.Iterator;
import java.util.Set;


import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_6_R3.EntityEnderDragon;


import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftEnderDragon;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;

import org.getspout.spoutapi.player.SpoutPlayer;

import com.bekvon.bukkit.residence.Residence;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.P;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.V10lator.BananaRegion.BananaRegion;

class RTDCE implements CommandExecutor
{ 

boolean allowflightcurrent;

	
	
	
  private final RideThaDragon plugin;
  
  RTDCE(RideThaDragon plugin)
  {
	this.plugin = plugin;
  }
  
  @SuppressWarnings({ })
public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args)
  
  {
	if(args.length < 1)
	{
	  if(!(sender instanceof Player))
		return true;
	  
	  if(!sender.hasPermission("ridetha.dragon"))
	  {
		sender.sendMessage(ChatColor.RED+"You don't have permissions to use this command!");
		return true;
	  }
	  
	  Player player = (Player)sender;
	  String pn = player.getName();
	  if(RideThaDragon.allowflightchecked == false)
	  {
	  RideThaDragon.allowflightbegin = player.getAllowFlight();
	  RideThaDragon.allowflightchecked = true;
	  }
	
	  plugin.allowTeleport.add(player);
	  if(RideThaDragon.dragons.containsKey(pn))
	  {
		LivingEntity ld = RideThaDragon.dragons.get(pn);
		Entity p = ld.getPassenger();
		if(p == null || p.getUniqueId() != player.getUniqueId())
		{
		  V10Dragon d = null;
		  for(Entity e: player.getNearbyEntities(20, 20, 20))
		  {
			if(!(e instanceof EnderDragon))
			  continue;
			EntityEnderDragon eed = ((CraftEnderDragon)e).getHandle();
			if(eed instanceof V10Dragon)
			{
			  d = (V10Dragon)eed;
			  if(d.player.equalsIgnoreCase(pn))
			  {
				  
				  {
				  player.setAllowFlight(true);
				 
				  //player.sendMessage("Flug aktiviert");
				  }
				break;
			  }
			  else
				d = null;
			}
		  }
		  if(d == null)
			sender.sendMessage(ChatColor.RED+"You dragon is to far away!");
		  else
		  {
			boolean spout = plugin.spout;
			if(spout &&
					!((SpoutPlayer)sender).isSpoutCraftEnabled())
			   spout = false;
			d.spout = spout;
		    d.getBukkitEntity().setPassenger(player);
		  }
		}
		else
		{
		  Location loc = player.getLocation();
		  loc.setY(loc.getWorld().getHighestBlockAt(loc).getRelative(BlockFace.UP).getY());
		  RideThaDragon.dragons.get(pn).eject();
		  player.teleport(loc);
		  player.setAllowFlight(RideThaDragon.allowflightbegin);
		  RideThaDragon.allowflightchecked = false;
//		  if(RideThaDragon.allowflightbegin == false)
//		   {
//		   player.setAllowFlight(false);
//		   RideThaDragon.flightchecked = false;
//		   //player.sendMessage("Flug deaktiviert");
//		   }
		  //TODO: 
		  Iterator<ComplexEntityPart> pi = ((EnderDragon)ld).getParts().iterator();
		  pi.next();
		  ComplexEntityPart b = pi.next();
		  Entity pa = b.getPassenger();
		  if(pa != null)
		  {
			loc = pa.getLocation();
			loc.setY(loc.getWorld().getHighestBlockAt(loc).getRelative(BlockFace.UP).getY());
			b.eject();
			pa.teleport(loc);
			
			
		  }
		}
	  }
	  else
	  {
		if(plugin.economy != null)
		{
		  if(!plugin.economy.has(pn, plugin.price))
		  {
			player.sendMessage(ChatColor.RED+"Do you have enough money?");
			plugin.allowTeleport.remove(player);
			return true;
		  }
		}
		Location loc = player.getLocation();
		World w = loc.getWorld();
		String wn = w.getName();
		Block b = loc.getBlock();
		if(plugin.mh.containsKey(wn))
		{
		  int y = loc.getBlockY();
		  int mx = plugin.mh.get(wn);
		  if(y < mx)
		  {
			if(w.getHighestBlockYAt(loc) > y)
			{
			  sender.sendMessage(ChatColor.RED+"Sorry, no free air above you to spawn a dragon here!");
			  plugin.allowTeleport.remove(player);
			  return true;
			}
			loc.setY(mx);
			b = loc.getBlock();
			int mh = loc.getWorld().getMaxHeight();
			if(mx < mh)
			{
			  while(b.getY() < mh && b.getType() != Material.AIR)
				b = b.getRelative(BlockFace.UP);
			  loc.setY(b.getY());
			  b = loc.getBlock();
			  
			}
		  }
		}
		if(plugin.wg != null)
		{
		  RegionManager rm = plugin.wg.getRegionManager(w);
		  Vector v = toVector(loc);
		  for(ProtectedRegion pr: rm.getApplicableRegions(v))
		  {
			if(pr.contains(v))
			  loc.setY(loc.getWorld().getMaxHeight() + 1);
			break;
		  }
		}
		if(plugin.bananAPI != null && plugin.bananAPI.isRegion(b))
		  loc.setY(loc.getWorld().getMaxHeight() + 1);
		if(plugin.resim != null && plugin.resim.getByLoc(loc) != null)
		  loc.setY(loc.getWorld().getMaxHeight() + 1);
		if(plugin.townyu != null && b != null)
		  for(TownBlock tb: plugin.townyu.getAllTownBlocks())
			if(tb.getX() == b.getX() && tb.getZ() == b.getZ())
			{
			  loc.setY(loc.getWorld().getMaxHeight() + 1);
			  break;
			}
		if(plugin.factions && Board.getFactionAt(new FLocation(loc)) != null)
		  loc.setY(loc.getWorld().getMaxHeight() + 1);
		net.minecraft.server.v1_6_R3.World notchWorld = ((CraftWorld) loc.getWorld()).getHandle();
		
		V10Dragon v10dragon = new V10Dragon(plugin, player, loc, notchWorld);
		if(!notchWorld.addEntity(v10dragon, SpawnReason.CUSTOM))
		{
		  sender.sendMessage(ChatColor.RED+"Can't spawn a dragon here!");
		  plugin.allowTeleport.remove(player);
		  return true;
		}
		if(plugin.economy != null)
		  plugin.economy.withdrawPlayer(pn, plugin.price);
		LivingEntity dragon = (LivingEntity)v10dragon.getBukkitEntity();
		RideThaDragon.dragons.put(pn, dragon);
		
		if(plugin.spout)
		  for(Player p: plugin.getServer().getOnlinePlayers())
			plugin.registerTexture((SpoutPlayer)p, dragon, pn.equalsIgnoreCase(p.getName()));
		dragon.setPassenger(player);
		player.setAllowFlight(true);
		
	  }
	  plugin.allowTeleport.remove(player);
	}
	else if(sender.hasPermission("ridetha.locate") &&
			args[0].equalsIgnoreCase("locate"))
	{
	  if(!(sender instanceof Player))
		return true;
	  Player p =(Player)sender;
	  String pn = p.getName();
	  if(!RideThaDragon.dragons.containsKey(pn))
	  {
		p.sendMessage(ChatColor.RED+"You don't own a dragon!");
		return true;
	  }
	  ItemStack ih = p.getItemInHand();
	  if(ih.getType() != Material.COMPASS)
	  {
		p.sendMessage(ChatColor.RED+"You don't ohave a compass in your hand!");
		return true;
	  }
	  Location dl = RideThaDragon.dragons.get(pn).getLocation();
	  if(!p.getWorld().getName().equals(dl.getWorld().getName()))
	  {
		p.sendMessage(ChatColor.RED+"Your dragon isn't in the same world as you!");
		return true;
	  }
	  p.setCompassTarget(dl);
	  p.sendMessage(ChatColor.GREEN+"Your compass points to your dragon now!");
	  //48, 2378
	}
	else if(sender.hasPermission("ridetha.missing") &&
			args[0].equalsIgnoreCase("missing"))
	{
		Player p =(Player)sender;
	  if(!(sender instanceof Player))
		return true;
	  if(args.length > 1)
	    {
		  String wn;
		  if(args.length == 2)
		    wn = args[1];
		  else
		  {
		    StringBuilder sb = new StringBuilder(args[1]);
		    for(int i = 2; i < args.length; i++)
			  sb.append(' ').append(args[i]);
		    wn = sb.toString();
		  }
		  World w = plugin.getServer().getWorld(wn);
		  if(w == null)
		  {
			sender.sendMessage(ChatColor.RED+"World \""+ChatColor.BLUE+wn+ChatColor.RED+"\" not found!");
			return true;
		  }
		  Iterator<LivingEntity> diter;
		  for(Entity e: w.getEntities())
		  {
		    if(e instanceof EnderDragon)
		    {
		      LivingEntity le = (LivingEntity)e;
		      if(RideThaDragon.dragons.containsValue(le))
		      {
		    	diter = RideThaDragon.dragons.values().iterator();
		    	while(diter.hasNext())
		    	{
		    	  if(diter.next().equals(le))
		    	  {
				    diter.remove();
				    break;
		    	  }
		    	}
		      }
		      le.eject();
		      le.remove();
		    }
		  }
		  sender.sendMessage(ChatColor.GREEN+"Removed dragons from world\""+ChatColor.YELLOW+wn+ChatColor.GREEN+"\".");
		  return true;
	    }
	    for(LivingEntity d: RideThaDragon.dragons.values())
		  d.remove();
	    RideThaDragon.dragons.clear();
	    sender.sendMessage(ChatColor.GREEN+"Dragons removed.");
	  
  
	  try
		{
		  YamlConfiguration dcc = new YamlConfiguration();
		  File svv = new File(RideThaDragon.folder, "dragons.sav");
		  if(!svv.exists())
		  {
			RideThaDragon.folder.mkdirs();
			svv.createNewFile();
		  }
		  dcc.load(svv);
		  File invff;
		  YamlConfiguration invYY;
		  int i;
		  ItemStack[] inv = null;
		  Set<String> keys;
		  World world;
		  boolean unload;
		  for(String node: dcc.getKeys(false))
		  {
			String w = dcc.getString(node+".world");
			world = RideThaDragon.s.getWorld(w);
			
			if(world == null)
			{
			  
			  continue;
			}
			Chunk c = new Location(world, dcc.getDouble(node+".x"), dcc.getDouble(node+".y"), dcc.getDouble(node+".z")).getChunk();
			unload = !c.isLoaded();
			if(unload)
			  c.load();
			
			invff = new File(RideThaDragon.folder, "invs/"+node+".inv");
			if(invff.exists())
			{
			  invYY = new YamlConfiguration();
			  invYY.load(invff);
			  keys = invYY.getKeys(false);
			  inv = new ItemStack[54];
			  for(String invN: keys)
			  {
			    i = Integer.parseInt(invN);
			    inv[i] =  invYY.getItemStack(invN);
			  }
			}
			
			V10Dragon d = new V10Dragon(plugin, node, dcc.getDouble(node+".x"), dcc.getDouble(node+".y"), dcc.getDouble(node+".z"), (float)dcc.getDouble(node+".yaw"), world, dcc.getInt(node+".lived"), dcc.getDouble(node+".food", 0.0D), inv);
			net.minecraft.server.v1_6_R3.World notchWorld = ((CraftWorld)world).getHandle();
			if(!notchWorld.addEntity(d, SpawnReason.CUSTOM))
			  p.sendMessage("Fehler beim respawnen...");
			else
			  RideThaDragon.dragons.put(node, (LivingEntity)d.getBukkitEntity());
			p.sendMessage("Dragons were reloaded...");}}
			catch(Exception e)
			{
				
			  p.sendMessage("Error, can't reload the sav file...");
			  return true;
			}
	}

	//TODO: 
	else if(sender.hasPermission("ridetha.passenger") &&
			args[0].equalsIgnoreCase("passenger"))
	{
	  if(!(sender instanceof Player))
		return true;
	  if(args.length < 2)
	    return false;
	  Player p =(Player)sender;
	  String pn = p.getName();
	  if(!RideThaDragon.dragons.containsKey(pn))
	  {
		p.sendMessage(ChatColor.RED+"You don't own a dragon!");
		return true;
	  }
	  LivingEntity ld = RideThaDragon.dragons.get(pn);
	  Entity pa = ld.getPassenger();
	  if(pa == null || pa.getUniqueId() != p.getUniqueId())
	  {
		p.sendMessage(ChatColor.RED+"You must ride your dragon to set a passenger!");
		return true;
	  }
	  Player pas = plugin.getServer().getPlayer(args[1]);
	  if(pas == null)
	  {
		p.sendMessage(ChatColor.RED+"Player "+args[1]+" not found!");
		return true;
	  }
	  boolean found = false;
	  for(Entity e: pas.getNearbyEntities(20, 20, 20))
	  {
		if(e instanceof EnderDragon &&
				e.getUniqueId() == ld.getUniqueId())
		{
		  found = true;
		  break;
		}
	  }
	  if(!found)
	  {
		p.sendMessage(ChatColor.RED+"Player "+args[1]+" is to far away!");
		return true;
	  }
	  Iterator<ComplexEntityPart> pi = ((EnderDragon)ld).getParts().iterator();
	  pi.next();
	  ComplexEntityPart b = pi.next();
	  if(b.getPassenger() != null)
	  {
		p.sendMessage(ChatColor.RED+"You can't have more than one passenger!");
		return true;
	  }
	  b.setPassenger(pas);
	  
	}
	

	
	
	else if(sender.hasPermission("ridetha.remove") &&
			args[0].equalsIgnoreCase("remove"))
	{
	  if(!(sender instanceof Player))
		return true;
	  
	  String pn = ((Player)sender).getName();
	  if(!RideThaDragon.dragons.containsKey(pn))
		sender.sendMessage(ChatColor.RED+"You don't own a dragon!");
	  else
	  {
		plugin.killIt(pn);
		sender.sendMessage(ChatColor.GOLD+"Your dragon has been removed.");
	  }
	}
	else if(sender.hasPermission("ridetha.admin"))
	{
		
	  if(args[0].equalsIgnoreCase("removeall"))
	  {
	    if(args.length > 1)
	    {
		  String wn;
		  if(args.length == 2)
		    wn = args[1];
		  else
		  {
		    StringBuilder sb = new StringBuilder(args[1]);
		    for(int i = 2; i < args.length; i++)
			  sb.append(' ').append(args[i]);
		    wn = sb.toString();
		  }
		  World w = plugin.getServer().getWorld(wn);
		  if(w == null)
		  {
			sender.sendMessage(ChatColor.RED+"World \""+ChatColor.BLUE+wn+ChatColor.RED+"\" not found!");
			return true;
		  }
		  Iterator<LivingEntity> diter;
		  for(Entity e: w.getEntities())
		  {
		    if(e instanceof EnderDragon)
		    {
		      LivingEntity le = (LivingEntity)e;
		      if(RideThaDragon.dragons.containsValue(le))
		      {
		    	diter = RideThaDragon.dragons.values().iterator();
		    	while(diter.hasNext())
		    	{
		    	  if(diter.next().equals(le))
		    	  {
				    diter.remove();
				    break;
		    	  }
		    	}
		      }
		      le.eject();
		      le.remove();
		    }
		  }
		  sender.sendMessage(ChatColor.GREEN+"Removed dragons from world\""+ChatColor.YELLOW+wn+ChatColor.GREEN+"\".");
		  return true;
	    }
	    for(LivingEntity d: RideThaDragon.dragons.values())
		  d.remove();
	    RideThaDragon.dragons.clear();
	    sender.sendMessage(ChatColor.GREEN+"Dragons removed.");
	  }
		
	  else if(args[0].equalsIgnoreCase("height"))
	  {
		if(args.length < 2)
		  return false;
		String wn;
		if(args.length < 3)
		{
		  if(!(sender instanceof Player))
		  {
			sender.sendMessage("Please specify a world.");
			return true;
		  }
		  wn = ((Player)sender).getWorld().getName();
		}
		else if(args.length > 3)
		{
		  StringBuilder sb = new StringBuilder(args[1]);
		  for(int i = 2; i < args.length - 1; i++)
		  {
			sb.append(' ');
			sb.append(args[i]);
		  }
		  wn = sb.toString();
		}
		else
		  wn = args[1];
		if(plugin.getServer().getWorld(wn) == null)
		{
		  sender.sendMessage(ChatColor.RED+"World \""+ChatColor.YELLOW+wn+ChatColor.RED+"\" not found!");
		  return true;
		}
		try
		{
		  int mx = Integer.parseInt(args[args.length - 1]);
		  if(mx > 0)
		    plugin.mh.put(wn, mx);
		  else
			plugin.mh.remove(wn);
		  sender.sendMessage(ChatColor.GREEN+"Height changed!");
		  plugin.saveChanged = true;
		}
		catch(NumberFormatException e)
		{
		  sender.sendMessage(ChatColor.RED+"Invalid height: "+ChatColor.YELLOW+args[args.length - 1]);
		}
	  }
	  else if(args[0].equalsIgnoreCase("protect"))
	  {
		String wn;
		if(args.length < 2)
		{
		  if(!(sender instanceof Player))
		  {
			sender.sendMessage("Please specify a world.");
			return true;
		  }
		  wn = ((Player)sender).getWorld().getName();
		}
		else if(args.length > 2)
		{
		  StringBuilder sb = new StringBuilder(args[1]);
		  for(int i = 1; i < args.length - 1; i++)
		 {
			sb.append(' ');
			sb.append(args[i]);
		  }
		  wn = sb.toString();
		}
		else
		  wn = args[1];
		if(plugin.getServer().getWorld(wn) == null)
		{
		  sender.sendMessage(ChatColor.RED+"World \""+ChatColor.YELLOW+wn+ChatColor.RED+"\" not found!");
		  return true;
		}
		if(plugin.stopGrief.contains(wn))
		{
		  plugin.stopGrief.remove(wn);
		  sender.sendMessage("Full protection disabled!");
		}
		else
		{
		  plugin.stopGrief.add(wn);
		  sender.sendMessage("Full protection enabled!");
		}
		plugin.saveChanged = true;
	  }
	  else if(args[0].equalsIgnoreCase("spawn"))
	  {
		if(!(sender instanceof Player))
		  return true;
		Location loc = ((Player)sender).getLocation();
		World w = loc.getWorld();
		String wn = w.getName();
		if(plugin.mh.containsKey(wn))
		{
		  int y = loc.getBlockY();
		  int mx = plugin.mh.get(wn);
		  if(y < mx)
		  {
			loc.setY(mx);
			Block b = loc.getBlock();
			while(b != null && b.getType() != Material.AIR)
			{
			  loc.setY(loc.getY() + 1);
			  b = loc.getBlock();
			}
		  }
		}
		loc.getWorld().spawnEntity(loc, EntityType.ENDER_DRAGON);
		sender.sendMessage(ChatColor.GREEN+"Wild dragon spawned!");
	  }
	  else if(args[0].equalsIgnoreCase("speed"))
	  {
		if(args.length < 2)
		  return false;
		try
		{
		  plugin.rideSpeed = Double.parseDouble(args[1]);
		  sender.sendMessage(ChatColor.GOLD+"New speed: "+plugin.rideSpeed);
		  plugin.saveChanged = true;
		}
		catch(NumberFormatException e)
		{
		  sender.sendMessage(ChatColor.RED+args[1]+" is no valid speed!");
		}
	  }
	  else if(args[0].equalsIgnoreCase("WorldGuard"))
	  {
		if(plugin.wg == null)
		{
		  plugin.wg = (WorldGuardPlugin)plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		  if(plugin.wg == null)
			sender.sendMessage(ChatColor.RED+"WorldGuard not found!");
		  else
			sender.sendMessage(ChatColor.GREEN+"WorldGuard support enabled!");
		}
		else
		{
		  plugin.wg = null;
		  sender.sendMessage(ChatColor.GOLD+"WorldGuard support disabled!");
		}
		plugin.saveChanged = true;
	  }
	  else if(args[0].equalsIgnoreCase("BananaRegion"))
	  {
		if(plugin.bananAPI == null)
		{
		  BananaRegion br = (BananaRegion)plugin.getServer().getPluginManager().getPlugin("BananaRegion");
		  if(br == null)
			sender.sendMessage(ChatColor.RED+"BananaRegion not found!");
		  else
		  {
			plugin.bananAPI = br.getAPI();
			sender.sendMessage(ChatColor.GREEN+"BananaRegion support enabled!");
		  }
		}
		else
		{
		  plugin.bananAPI = null;
		  sender.sendMessage(ChatColor.GOLD+"BananaRegion support disabled!");
		}
		plugin.saveChanged = true;
	  }
	  else if(args[0].equalsIgnoreCase("Towny"))
	  {
		if(plugin.townyu == null)
		{
		  Towny towny = (Towny)plugin.getServer().getPluginManager().getPlugin("Towny");
		  if(towny != null)
		  {
			plugin.townyu = towny.getTownyUniverse();
			sender.sendMessage(ChatColor.GREEN+"Towny support enabled!");
		  }
		  else
			sender.sendMessage(ChatColor.RED+"Towny not found!");
		}
		else
		{
		  plugin.townyu = null;
		  sender.sendMessage(ChatColor.GOLD+"Towny support disabled!");
		}
		plugin.saveChanged = true;
	  }
	  else if(args[0].equalsIgnoreCase("Residence"))
	  {
		if(plugin.resim == null)
		{
		  if(plugin.getServer().getPluginManager().getPlugin("Residence") == null)
			sender.sendMessage(ChatColor.RED+"Residence not found!");
		  else
		  {
			plugin.resim = Residence.getResidenceManager();
			sender.sendMessage(ChatColor.GREEN+"Residence support enabled!");
		  }
		}
		else
		{
		  plugin.resim = null;
		  sender.sendMessage(ChatColor.GOLD+"Residence support disabled!");
		}
		plugin.saveChanged = true;
	  }
	  else if(args[0].equalsIgnoreCase("Factions"))
	  {
		if(!plugin.factions)
		{
		  P factions = (P)plugin.getServer().getPluginManager().getPlugin("Factions");
		  if(factions == null)
			sender.sendMessage(ChatColor.RED+"Factions not found!");
		  else
		  {
			plugin.factions = true;
			sender.sendMessage(ChatColor.GREEN+"Factions support enabled!");
		  }
		}
		else
		{
		  plugin.factions = false;
		  sender.sendMessage(ChatColor.GOLD+"Factions support disabled!");
		}
		plugin.saveChanged = true;
	  }
	  else if(args[0].equalsIgnoreCase("cost"))
	  {
		if(args.length < 2)
		  return false;
		double p;
		try
		{
		  p = Double.parseDouble(args[1]);
		}
		catch(NumberFormatException e)
		{
		  sender.sendMessage(ChatColor.RED+"Invalid cost: "+args[1]);
		  return true;
		}
		if(plugin.economy == null)
		{
		  PluginManager pm = plugin.getServer().getPluginManager();
		  if(pm.getPlugin("Vault") == null)
		  {
			sender.sendMessage(ChatColor.RED+"Vault not found!");
			return true;
		  }
		  RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
		  if(economyProvider != null)
	        plugin.economy = economyProvider.getProvider();
		  else
		  {
			sender.sendMessage(ChatColor.RED+"Vault error: Economy is null!");
			return true;
		  }
		}
		plugin.price = p;
		sender.sendMessage(ChatColor.GREEN+"New cost: "+p);
	  }
	  else if(args[0].equalsIgnoreCase("lifetime"))
	  {
		if(args.length < 2)
		  return false;
		try
		{
		  plugin.lifetime = Integer.parseInt(args[1]);
		  sender.sendMessage(ChatColor.GREEN+"New lifetime: "+plugin.lifetime+" seconds.");
		}
		catch(NumberFormatException e)
		{
		  sender.sendMessage(ChatColor.RED+"Invalid lifetime: "+args[1]);
		  return true;
		}
	  }
	  else if(args[0].equalsIgnoreCase("texture"))
	  {
		if(args.length < 2)
		  return false;
		plugin.dragonTexture = args[1];
		sender.sendMessage(ChatColor.GREEN+"New texture: "+args[1]);
	  }
	  else if(args[0].equalsIgnoreCase("owntexture"))
	  {
		if(args.length < 2)
		  return false;
		plugin.ownDragonTexture = args[1];
		sender.sendMessage(ChatColor.GREEN+"New texture: "+args[1]);
	  }
	  else
		return false;
	}
	else
	  sender.sendMessage(ChatColor.RED+"You don't have permissions to use this command!");
	return true;
  }

private Vector toVector(Location loc) {
	// TODO Auto-generated method stub
	return null;
}
}
