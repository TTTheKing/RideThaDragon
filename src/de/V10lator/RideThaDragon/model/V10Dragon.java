package de.V10lator.RideThaDragon.model;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;

import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;
import de.V10lator.RideThaDragon.RideThaDragon;

import net.minecraft.server.v1_7_R1.DamageSource;
import net.minecraft.server.v1_7_R1.EntityComplexPart;
import net.minecraft.server.v1_7_R1.EntityEnderDragon;
import net.minecraft.server.v1_7_R1.World;

public class V10Dragon extends EntityEnderDragon{

    private final RideThaDragon plugin;
    public final String player;
    private boolean onGround = false;
    private final short[] counter = {0, 0, 0};
    private boolean directionChanged = false;
    public boolean spout;
    public byte upDown = 0;
    public boolean brr = true;
    public boolean ignoreFlying = false;
    public boolean dragonfly = false;
    public double fl = 0.0D;

    public V10Dragon(RideThaDragon plugin, Player player, Location loc, World world) {
        super(world);
        this.plugin = plugin;
        this.player = player.getName();
        setPosition(loc.getX(), loc.getY(), loc.getZ());
        yaw = loc.getYaw() + 180;
        while (yaw > 360) {
            yaw -= 360;
        }
        while (yaw < 0) {
            yaw += 360;
        }
        if (yaw < 45 || yaw > 315) {
            yaw = 0F;
        } else if (yaw < 135) {
            yaw = 90F;
        } else if (yaw < 225) {
            yaw = 180F;
        } else {
            yaw = 270F;
        }
    }

    public V10Dragon(RideThaDragon drrr, String player, double x, double y, double z, float yaw, org.bukkit.World world, int lived, double fl, ItemStack[] inv) {
        super(((CraftWorld) world).getHandle());
        this.plugin = drrr;
        this.player = player;
        setPosition(x, y, z);
        this.yaw = yaw;
        ticksLived = lived;
        this.fl = fl;
 
    }

    public V10Dragon(World world) {
        super(world);
        plugin = null;
        player = null;
        spout = false;
        getBukkitEntity().remove();
    }

    @Override
    public void e() {
        if (plugin.lifetime > 0 && ticksLived / 20 > plugin.lifetime) {
            plugin.killIt(player);
            return;
        }

        if (passenger == null) {
            if (!onGround) {

                double myY = locY - plugin.rideSpeed;
                int imyY = (int) myY;
                if (imyY < 1) {
                    onGround = true;
                }
                if (imyY < 1 || (imyY < world.getWorld().getMaxHeight() && world.getWorld().getBlockAt((int) locX, imyY, (int) locZ).getType() != Material.AIR)) {
                    onGround = true;
                } else {
                    this.setPosition(locX, myY, locZ);
                }
            }
            return;
        }
        onGround = false;

        Player p = plugin.getServer().getPlayerExact(player);
        if (p == null) {
            return;
        }


        if (ignoreFlying) {
            if (++counter[1] > 10) {
                counter[1] = 0;
                ignoreFlying = true;
            }
        } //experimentell
        else if (p.isSneaking()) {
            p.setAllowFlight(RideThaDragon.allowflightbegin);
        } else if (p.isFlying()) {
            if (dragonfly == false) {
                dragonfly = true;
                ignoreFlying = true;
                p.setFlying(false);
                ignoreFlying = false;
            } else {
                ignoreFlying = true;
                dragonfly = false;
                p.setFlying(false);
                ignoreFlying = false;

            }
        }

        if (!dragonfly) {
            return;
        }

        if (fl > 0.0D) {
            if (counter[2] > 200) {
                fl -= 0.1D;
                counter[2] = 0;
            } else {
                counter[2]++;
            }
        } else {
            counter[2] = 0;
        }

        Location loc;
        double myX = locX;
        double myY = locY;
        double myZ = locZ;

            if (!directionChanged) {
                loc = p.getEyeLocation();
                double oldYaw = yaw;
                yaw = loc.getYaw() + 180;
                while (yaw > 360) {
                    yaw -= 360;
                }
                while (yaw < 0) {
                    yaw += 360;
                }

                if (yaw < 22.5F || yaw > 337.5D) {
                    yaw = 0F;
                } else if (yaw < 67.5F) {
                    yaw = 45F;
                } else if (yaw < 112.5F) {
                    yaw = 90F;
                } else if (yaw < 157.5F) {
                    yaw = 135F;
                } else if (yaw < 202.5F) {
                    yaw = 180F;
                } else if (yaw < 247.5F) {
                    yaw = 225F;
                } else if (yaw < 292.5F) {
                    yaw = 270F;
                } else {
                    yaw = 315F;
                }

                if (loc.getPitch() < -45) {
                    upDown = 2;
                } else if (loc.getPitch() > 45) {
                    upDown = 1;
                } else {
                    upDown = 0;
                }

                if (oldYaw != yaw || myY != locY) {
                    directionChanged = true;
                }
            }

            if (directionChanged) {
                if (counter[0] < 20) {
                    counter[0]++;
                } else {
                    counter[0] = 0;
                    directionChanged = false;
                }
            }

        double rideSpeed = plugin.rideSpeed + fl;
        if (upDown == 1) {
            myY -= rideSpeed;
        } else if (upDown == 2) {
            myY += rideSpeed;
        }

        if (yaw < 22.5F || yaw > 337.5F) {
            myZ -= rideSpeed;
        } else if (yaw < 67.5F) {
            double halfSpeed = rideSpeed / 2;
            myZ -= halfSpeed;
            myX += halfSpeed;
        } else if (yaw < 112.5F) {
            myX += rideSpeed;
        } else if (yaw < 157.5F) {
            double halfSpeed = rideSpeed / 2;
            myX += halfSpeed;
            myZ += halfSpeed;
        } else if (yaw < 202.5F) {
            myZ += rideSpeed;
        } else if (yaw < 247.5F) {
            double halfSpeed = rideSpeed / 2;
            myZ += halfSpeed;
            myX -= halfSpeed;
        } else if (yaw < 292.5) {
            myX -= rideSpeed;
        } else {
            double halfSpeed = rideSpeed / 2;
            myX -= halfSpeed;
            myZ -= halfSpeed;
        }

        org.bukkit.World w = world.getWorld();
        String wn = w.getName();
        if (plugin.mh.containsKey(wn)) {
            int my = plugin.mh.get(wn);
            if (myY < my) {
                myY = my;
            }
        }

        if (myY < w.getMaxHeight()) {
            loc = new Location(w, myX, myY, myZ);
            Block b = loc.getBlock();
            if (plugin.stopGrief.contains(wn) && b != null && b.getType() != Material.AIR && !b.isLiquid()) {
                return;
            }
            Vector v;
            if (plugin.wg != null) {
                v = toVector(loc);
                for (ProtectedRegion pr : plugin.wg.getRegionManager(w).getApplicableRegions(toVector(loc))) {
                    if (pr.contains(v)) {
                        return;
                    }
                }
            }

        }

        this.setPosition(myX, myY, myZ);
    }

    public boolean damageEntity(DamageSource damagesource, int i) {
        return false;
    }

    public boolean a(EntityComplexPart entitycomplexpart, DamageSource damagesource, int i) {
        return false;
    }
    
    @Override
    public String t()
    {
          return plugin.silence ? null : super.t();
    }

    @Override
    public String aT()
    {
          return plugin.silence ? null : super.aT();
    }
    
}
