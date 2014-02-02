package de.V10lator.RideThaDragon;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_7_R1.EntityTypes;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;


public class RideThaDragon extends JavaPlugin
{
	World world;
	ItemStack[] inv;
	static YamlConfiguration dcc = new YamlConfiguration();
	String node;
	static Server s;
	static File folder;
	static Logger log;
	
  final static HashMap<String, LivingEntity> dragons = new HashMap<String, LivingEntity>();
  final HashMap<String, Integer> mh = new HashMap<String, Integer>();
  private Configuration config;
  boolean saveChanged = false;
  WorldGuardPlugin wg;
  boolean factions = false;
  final HashSet<String> stopGrief = new HashSet<String>();
  double rideSpeed;
  boolean spout = false;
  boolean v10verlap = false;
  final HashSet<Player> allowTeleport = new HashSet<Player>();
  Economy economy = null;
  Double price;
  int lifetime;
  String dragonTexture;
  String ownDragonTexture;
  boolean silence;
  static boolean allowflightbegin;
  static boolean allowflightchecked;
  
  
  
    @Override
  public void onEnable()
  {
	s = getServer();
	log = getLogger();
	PluginDescriptionFile pdf = getDescription();
	config = getConfig();
	folder = getDataFolder();

	ConfigurationSection cs = config.getConfigurationSection("heights");
	if(cs != null)
	{
	  for(String wn: cs.getKeys(false))
	  {
		try
		{
		  int m = cs.getInt(wn);
		  World w = s.getWorld(wn);
		  if(w != null)
			mh.put(wn, m);
		  else
			log.log(Level.INFO, "World \"{0}\" not found! Check your config!", wn);
		}
		catch(NumberFormatException e)
		{
		  log.log(Level.INFO, "Invalid max height for world \"{0}\"! Check your config!", wn);
		}
	  }
	}
	config.set("heights", null);
	PluginManager pm = s.getPluginManager();
	
	// Add our new entity to minecrafts entities:
	try
	{
	  Method method = EntityTypes.class.getDeclaredMethod("a", new Class[] {Class.class, String.class, int.class});
	  method.setAccessible(true);
	  method.invoke(EntityTypes.class, V10Dragon.class, "V10Dragon", 163);
	}
	catch(IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e)
	{
	  log.info("Error registering Entity!");
	  e.printStackTrace();
	  pm.disablePlugin(this);
	  return;
	}
	
	List<?> fp = config.getList("FullProtect");
	if(fp != null)
	{
	  for(Object fpo: fp)
	  {
		if(!(fpo instanceof String))
		  log.info("Invalid entry in the configuration section FullProtect!");
		else
		{
		  String w = (String)fpo;
		  if(s.getWorld(w) == null)
			log.info("Couldn't find world in configuraton section FullProtect: "+w);
		  else
			stopGrief.add(w);
		}
	  }
	}
	
	if(!config.isBoolean("WorldGuard"))
	  saveChanged = true;
	if(config.getBoolean("WorldGuard", false))
	  wg = (WorldGuardPlugin)pm.getPlugin("WorldGuard");
	else
	  wg = null;
        
	if(!config.isDouble("RideSpeed"))
	  saveChanged = true;
	rideSpeed = config.getDouble("RideSpeed", 0.6D);
	if(!config.isDouble("Lifetime"))
	  saveChanged = true;
	lifetime = config.getInt("Lifetime", 0);
	if(!config.isString("dragonTexture"))
	  saveChanged = true;
	dragonTexture = config.getString("DragonTexture", "http://www.v10lator.de/RideThaDragonSkin.png");
	ownDragonTexture = config.getString("OwnDragonTexture", "http://www.v10lator.de/RideThaDragonSkin2.png");
	if(!config.isBoolean("WorldGuard"))
	  saveChanged = true;
	silence = config.getBoolean("Silence", true);
	if(pm.getPlugin("Vault") == null)
	{
	  log.info("Vault not found!");
	  log.info("Economy support disabled.");
	}
	else
	{
	  RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
	  if(economyProvider != null)
	  {
        economy = economyProvider.getProvider();
        if(!config.isDouble("DragonCost"))
      	  saveChanged = true;
      	price = config.getDouble("DragonCost", 25.0D);
      	if(price < 0.0D)
      	{
      	  price = 0.0D;
      	  saveChanged = true;
      	}
	  }
	  else
		log.info("Economy support disabled.");
	}
	
	try
	{
	  YamlConfiguration dc = new YamlConfiguration();
	  File sv = new File(getDataFolder(), "dragons.sav");
	  if(!sv.exists())
	  {
		getDataFolder().mkdirs();
		sv.createNewFile();
	  }
	  dc.load(sv);
	  File invf;
	  YamlConfiguration invY;
	  int i;
	  ItemStack[] inv = null;
	  Set<String> keys;
	  World world;
	  boolean unload;
	  for(String node: dc.getKeys(false))
	  {
		String w = dc.getString(node+".world");
		world = s.getWorld(w);
		if(world == null)
		{
		  log.info("Can't spawn dragon for player "+node+": World "+w+" not found!");
		  continue;
		}
		Chunk c = new Location(world, dc.getDouble(node+".x"), dc.getDouble(node+".y"), dc.getDouble(node+".z")).getChunk();
		unload = !c.isLoaded();
		if(unload)
		  c.load();
		
		invf = new File(getDataFolder(), "invs/"+node+".inv");
		if(invf.exists())
		{
		  invY = new YamlConfiguration();
		  invY.load(invf);
		  keys = invY.getKeys(false);
		  inv = new ItemStack[54];
		  for(String invN: keys)
		  {
		    i = Integer.parseInt(invN);
		    inv[i] =  invY.getItemStack(invN);
		  }
		}
		
		V10Dragon d = new V10Dragon(this, node, dc.getDouble(node+".x"), dc.getDouble(node+".y"), dc.getDouble(node+".z"), (float)dc.getDouble(node+".yaw"), world, dc.getInt(node+".lived"), dc.getDouble(node+".food", 0.0D), inv);
		net.minecraft.server.v1_7_R1.World notchWorld = ((CraftWorld)world).getHandle();
		if(!notchWorld.addEntity(d, SpawnReason.CUSTOM))
		  log.info(node+"s dragon can't be spawned!");
		else
		  dragons.put(node, (LivingEntity)d.getBukkitEntity());
	  }
	}
	catch(IOException | NumberFormatException | InvalidConfigurationException e)
	{
	  log.info("Can't read savefile!");
	  e.printStackTrace();
	  pm.disablePlugin(this);
	  return;
	}
	
	RTDL rtdl = new RTDL(this);
	
	getCommand("dragon").setExecutor(new RTDCE(this));
	pm.registerEvents(rtdl, this);
	BukkitScheduler sched = s.getScheduler();
	sched.scheduleSyncRepeatingTask(this, new AutoSave(), 12000L, 12000L);
	log.log(Level.INFO, "v{0} enabled!", pdf.getVersion());
	
	
  }
  
  
  
  public void onDisable()
  {
	Server s = getServer();
	s.getScheduler().cancelTasks(this);
	for(LivingEntity dragon: dragons.values())
	{
	  dragon.eject();
	  dragon.remove();
	}
	saveAll();
  }
  
  private class AutoSave implements Runnable
  {
	public void run()
	{
	  saveAll();
	}
  }
  
  void killIt(String player)
  {
	LivingEntity dragon = dragons.get(player);
	dragon.eject();
	dragon.remove();
	dragons.remove(player);
	World world = dragon.getWorld();
	Location loc = dragon.getLocation();
	for(ItemStack is: ((V10Dragon)((CraftLivingEntity)dragon).getHandle()).getInventory())
	  if(is != null)
		world.dropItemNaturally(loc, is);
  }
  
  void saveAll()
  {
		YamlConfiguration dc = new YamlConfiguration();
		File sv = new File(getDataFolder(), "dragons.sav");
		if(!sv.exists())
		{
		  getDataFolder().mkdirs();
		  try
		  {
			sv.createNewFile();
		  }
		  catch (IOException e)
		  {
			e.printStackTrace();
		  }
		}
		YamlConfiguration invY;
		File invf = new File(getDataFolder(), "invs");
		if(!invf.isDirectory())
		  invf.mkdirs();
		for(Entry<String, LivingEntity> e: dragons.entrySet())
		{
		  String p = e.getKey();
		  V10Dragon d = (V10Dragon)((CraftLivingEntity)e.getValue()).getHandle();
		  dc.set(p+".world", d.world.getWorld().getName());
		  dc.set(p+".x", d.locX);
		  dc.set(p+".y", d.locY);
		  dc.set(p+".z", d.locZ);
		  dc.set(p+".yaw", d.yaw);
		  dc.set(p+".lived", d.ticksLived);
		  dc.set(p+".food", d.fl);
		  try
		  {
			invf = new File(getDataFolder(), "invs/"+p+".inv");
		    if(!invf.exists())
		      invf.createNewFile();
		    invY = new YamlConfiguration();
		    invY.load(invf);
		    Inventory inv = d.getInventory();
		    for(int i = 0; i < inv.getSize(); i++)
		      invY.set(""+i, inv.getItem(i));
		    invY.save(invf);
		  }
		  catch(Exception ex)
		  {
			getLogger().info("WARNING: Couldn't save inventory for "+p);
			ex.printStackTrace();
		  }
		}
		try
		{
		  dc.save(sv);
		}
		catch (IOException e)
		{
		  getLogger().info("WARNING: Couldn't save!");
		  e.printStackTrace();
		}
		if(!saveChanged)
		  return;
		for(Entry<String, Integer> e: mh.entrySet())
		  config.set("heights."+e.getKey(), e.getValue());
		ArrayList<String> sg = new ArrayList<>(stopGrief);
		config.set("FullProtect", sg);
		config.set("WorldGuard", (wg != null));
		config.set("Factions", factions);
		config.set("RideSpeed", rideSpeed);
		config.set("Lifetime", lifetime);
		if(economy != null)
		  config.set("DragonCost", price);
		config.set("DragonTexture", dragonTexture);
		config.set("OwnDragonTexture", ownDragonTexture);
		config.set("Silence", silence);
		saveConfig();
		config.set("heights", null);
		saveChanged = false;
	  }

}	



