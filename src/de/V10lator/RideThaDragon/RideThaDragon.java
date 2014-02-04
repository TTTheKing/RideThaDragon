package de.V10lator.RideThaDragon;

import java.io.File;
import java.io.IOException;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.V10lator.RideThaDragon.command.RTDCommand;
import de.V10lator.RideThaDragon.listener.PlayerListener;
import de.V10lator.RideThaDragon.model.V10Dragon;
import java.lang.reflect.Field;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;

public class RideThaDragon extends JavaPlugin {

    public World world;
    ItemStack[] inv;
    static YamlConfiguration dcc = new YamlConfiguration();
    String node;
    public static Server server;
    public static File folder;
    static Logger log;

    public final static HashMap<String, LivingEntity> dragons = new HashMap<>();
    public final HashMap<String, Integer> mh = new HashMap<>();
    private Configuration config;
    public boolean saveChanged = false;
    public WorldGuardPlugin wg;
    boolean factions = false;
    public final HashSet<String> stopGrief = new HashSet<>();
    public double rideSpeed;
    boolean v10verlap = false;
    public final HashSet<Player> allowTeleport = new HashSet<>();
    public Economy economy = null;
    public Double price;
    public int lifetime;
    public boolean silence;
    public static boolean allowflightbegin;
    public static boolean allowflightchecked;

    @Override
    public void onEnable() {
        server = getServer();
        log = getLogger();
        PluginDescriptionFile pdf = getDescription();
        config = getConfig();
        folder = getDataFolder();

        ConfigurationSection cs = config.getConfigurationSection("heights");
        if (cs != null) {
            for (String wn : cs.getKeys(false)) {
                try {
                    int m = cs.getInt(wn);
                    World w = server.getWorld(wn);
                    if (w != null) {
                        mh.put(wn, m);
                    } else {
                        log.log(Level.INFO, "World \"{0}\" not found! Check your config!", wn);
                    }
                } catch (NumberFormatException e) {
                    log.log(Level.INFO, "Invalid max height for world \"{0}\"! Check your config!", wn);
                }
            }
        }
        config.set("heights", null);
        PluginManager pm = server.getPluginManager();

        registerEntity();

        List<?> fp = config.getList("FullProtect");
        if (fp != null) {
            for (Object fpo : fp) {
                if (!(fpo instanceof String)) {
                    log.info("Invalid entry in the configuration section FullProtect!");
                } else {
                    String w = (String) fpo;
                    if (server.getWorld(w) == null) {
                        log.log(Level.INFO, "Couldn''t find world in configuraton section FullProtect: {0}", w);
                    } else {
                        stopGrief.add(w);
                    }
                }
            }
        }

        if (!config.isBoolean("WorldGuard")) {
            saveChanged = true;
        }
        if (config.getBoolean("WorldGuard", false)) {
            wg = (WorldGuardPlugin) pm.getPlugin("WorldGuard");
        } else {
            wg = null;
        }

        if (!config.isDouble("RideSpeed")) {
            saveChanged = true;
        }
        rideSpeed = config.getDouble("RideSpeed", 0.6D);
        if (!config.isDouble("Lifetime")) {
            saveChanged = true;
        }
        lifetime = config.getInt("Lifetime", 0);
        if (!config.isString("dragonTexture")) {
            saveChanged = true;
        }
        if (!config.isBoolean("WorldGuard")) {
            saveChanged = true;
        }
        silence = config.getBoolean("Silence", true);
        if (pm.getPlugin("Vault") == null) {
            log.info("Vault not found!");
            log.info("Economy support disabled.");
        } else {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
                if (!config.isDouble("DragonCost")) {
                    saveChanged = true;
                }
                price = config.getDouble("DragonCost", 25.0D);
                if (price < 0.0D) {
                    price = 0.0D;
                    saveChanged = true;
                }
            } else {
                log.info("Economy support disabled.");
            }
        }

        try {
            YamlConfiguration dc = new YamlConfiguration();
            File sv = new File(getDataFolder(), "dragons.sav");
            if (!sv.exists()) {
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
            for (String node : dc.getKeys(false)) {
                String w = dc.getString(node + ".world");
                world = server.getWorld(w);
                if (world == null) {
                    log.log(Level.INFO, "Can''t spawn dragon for player {0}: World {1} not found!", new Object[]{node, w});
                    continue;
                }
                Chunk c = new Location(world, dc.getDouble(node + ".x"), dc.getDouble(node + ".y"), dc.getDouble(node + ".z")).getChunk();
                unload = !c.isLoaded();
                if (unload) {
                    c.load();
                }

                invf = new File(getDataFolder(), "invs/" + node + ".inv");
                if (invf.exists()) {
                    invY = new YamlConfiguration();
                    invY.load(invf);
                    keys = invY.getKeys(false);
                    inv = new ItemStack[54];
                    for (String invN : keys) {
                        i = Integer.parseInt(invN);
                        inv[i] = invY.getItemStack(invN);
                    }
                }

                V10Dragon d = new V10Dragon(this, node, dc.getDouble(node + ".x"), dc.getDouble(node + ".y"), dc.getDouble(node + ".z"), (float) dc.getDouble(node + ".yaw"), world, dc.getInt(node + ".lived"), dc.getDouble(node + ".food", 0.0D), inv);
                net.minecraft.server.v1_7_R1.World notchWorld = ((CraftWorld) world).getHandle();
                if (!notchWorld.addEntity(d, SpawnReason.CUSTOM)) {
                    log.log(Level.INFO, "{0}s dragon can''t be spawned!", node);
                } else {
                    dragons.put(node, (LivingEntity) d.getBukkitEntity());
                }
            }
        } catch (IOException | NumberFormatException | InvalidConfigurationException e) {
            log.info("Can't read savefile!");
            e.printStackTrace();
            pm.disablePlugin(this);
            return;
        }

        PlayerListener playerListener = new PlayerListener(this);

        getCommand("dragon").setExecutor(new RTDCommand(this));
        pm.registerEvents(playerListener, this);
        BukkitScheduler sched = server.getScheduler();
        sched.scheduleSyncRepeatingTask(this, new AutoSave(), 12000L, 12000L);
        log.log(Level.INFO, "v{0} enabled!", pdf.getVersion());

    }

    private boolean registerEntity() {

        try {
            Class entityTypeClass = EntityTypes.class;

            Field c = entityTypeClass.getDeclaredField("c");
            c.setAccessible(true);
            HashMap c_map = (HashMap) c.get(null);
            c_map.put("V10Dragon", V10Dragon.class);

            Field d = entityTypeClass.getDeclaredField("d");
            d.setAccessible(true);
            HashMap d_map = (HashMap) d.get(null);
            d_map.put(V10Dragon.class, "V10Dragon");

            Field e = entityTypeClass.getDeclaredField("e");
            e.setAccessible(true);
            HashMap e_map = (HashMap) e.get(null);
            e_map.put(Integer.valueOf(63), V10Dragon.class);

            Field f = entityTypeClass.getDeclaredField("f");
            f.setAccessible(true);
            HashMap f_map = (HashMap) f.get(null);
            f_map.put(V10Dragon.class, Integer.valueOf(63));

            Field g = entityTypeClass.getDeclaredField("g");
            g.setAccessible(true);
            HashMap g_map = (HashMap) g.get(null);
            g_map.put("V10Dragon", Integer.valueOf(63));

            return true;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            log.info("Error registering Entity!");
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        for (LivingEntity dragon : dragons.values()) {
            dragon.eject();
            dragon.remove();
        }
        saveAll();
    }

    private class AutoSave implements Runnable {

        @Override
        public void run() {
            saveAll();
        }
    }

    public void killIt(String player) {
        LivingEntity dragon = dragons.get(player);
        dragon.eject();
        dragon.remove();
        dragons.remove(player);
    }

    void saveAll() {
        YamlConfiguration dc = new YamlConfiguration();
        File sv = new File(getDataFolder(), "dragons.sav");
        if (!sv.exists()) {
            getDataFolder().mkdirs();
            try {
                sv.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Entry<String, LivingEntity> e : dragons.entrySet()) {
            String p = e.getKey();
            V10Dragon d = (V10Dragon) ((CraftLivingEntity) e.getValue()).getHandle();
            dc.set(p + ".world", d.world.getWorld().getName());
            dc.set(p + ".x", d.locX);
            dc.set(p + ".y", d.locY);
            dc.set(p + ".z", d.locZ);
            dc.set(p + ".yaw", d.yaw);
            dc.set(p + ".lived", d.ticksLived);
            dc.set(p + ".food", d.fl);

        }
        try {
            dc.save(sv);
        } catch (IOException e) {
            getLogger().info("WARNING: Couldn't save!");
            e.printStackTrace();
        }
        if (!saveChanged) {
            return;
        }
        for (Entry<String, Integer> e : mh.entrySet()) {
            config.set("heights." + e.getKey(), e.getValue());
        }
        ArrayList<String> sg = new ArrayList<>(stopGrief);
        config.set("FullProtect", sg);
        config.set("WorldGuard", (wg != null));
        config.set("Factions", factions);
        config.set("RideSpeed", rideSpeed);
        config.set("Lifetime", lifetime);
        if (economy != null) {
            config.set("DragonCost", price);
        }
        config.set("Silence", silence);
        saveConfig();
        config.set("heights", null);
        saveChanged = false;
    }

}
