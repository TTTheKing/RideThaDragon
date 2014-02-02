package de.V10lator.RideThaDragon;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;


import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

import net.minecraft.server.v1_6_R3.DamageSource;
import net.minecraft.server.v1_6_R3.EntityComplexPart;
import net.minecraft.server.v1_6_R3.EntityEnderDragon;
import net.minecraft.server.v1_6_R3.MathHelper;
import net.minecraft.server.v1_6_R3.World;
public class V10Dragon extends EntityEnderDragon implements InventoryHolder
{
  private RideThaDragon plugin;
  final String player;
  private boolean onGround = false;
  private final short[] counter = { 0, 0, 0 };
  private boolean directionChanged = false;
  boolean spout;
  byte upDown = 0;
  boolean brr = true;
  boolean ignoreFlying = false;
  private final Inventory virtInv;
  boolean dragonfly = false;
  double fl = 0.0D;
  Server s = (RideThaDragon.s);
  
  
  
  V10Dragon(RideThaDragon plugin, Player player, Location loc, World world)
  {
    super(world);
    this.plugin = plugin;
    this.player = player.getName();
    setPosition(loc.getX(), loc.getY(), loc.getZ());
    yaw = loc.getYaw() + 180;
	while(yaw > 360)
	  yaw -= 360;
	while(yaw < 0)
	  yaw += 360;
	if(yaw < 45 || yaw > 315)
	  yaw = 0F;
	else if(yaw < 135)
	  yaw = 90F;
	else if(yaw < 225)
	  yaw = 180F;
	else
	  yaw = 270F;
	
	virtInv = plugin.getServer().createInventory(this, 54, "Dragon");
  }
  
  V10Dragon(RideThaDragon drrr, String player, double x, double y, double z, float yaw, org.bukkit.World world, int lived, double fl, ItemStack[] inv)
  {
	super(((CraftWorld)world).getHandle());
	this.plugin = drrr;
	this.player = player;
	setPosition(x, y, z);
	this.yaw = yaw;
	ticksLived = lived;
	this.fl = fl;
	virtInv = drrr.getServer().createInventory(this, inv.length, "Dragon");
	for(int i = 0; i < inv.length; i++)
	  virtInv.setItem(i, inv[i]);
  }
  
  
  public V10Dragon(World world)
  {
	super(world);
	plugin = null;
	player = null;
	spout = false;
	virtInv = null;
	getBukkitEntity().remove();
  }
  
  @Override
  public void c()
  {
	if(plugin.lifetime > 0 && ticksLived / 20 > plugin.lifetime)
	{
	  plugin.killIt(player);
	  return;
	}
	
	if(passenger == null)
	{
	  if(!onGround)
	  {
			
		double myY = locY - plugin.rideSpeed;
		int imyY = (int)myY;
		if(imyY < 1)
		  onGround = true;
		if(imyY < 1 || (imyY < world.getWorld().getMaxHeight() && world.getWorld().getBlockAt((int)locX, imyY, (int)locZ).getType() != Material.AIR))
		  onGround = true;
		else 
		  this.setPosition(locX, myY, locZ);
	  }
	  strangeCode();
	  return;
	}
	onGround = false;
	
	Player p = plugin.getServer().getPlayerExact(player);
	if(p == null)
	  return;
	
	if(!spout)
	{
	  if(ignoreFlying)
	  {
		if(++counter[1] > 10)
		{
		  counter[1] = 0;
		  ignoreFlying = true;
		}
	  }
	  //experimentell
	  else if (p.isSneaking())
	  {
		  p.setAllowFlight(RideThaDragon.allowflightbegin);
	  }
	  else if(p.isFlying())
	  {
		if(dragonfly == false)
		{
			dragonfly = true;
			ignoreFlying = true;
		  p.setFlying(false);
		  ignoreFlying = false;
		}
		else
		{
			ignoreFlying = true;
		  dragonfly = false;
		  p.setFlying(false);
		  ignoreFlying = false;

		}
	  }
	}
	if(!dragonfly)
	  return;
	
	if(fl > 0.0D)
	{
	  //if(counter[2] > 600)
		  if(counter[2] > 200)
	  {
		fl -= 0.1D;
		counter[2] = 0;
	  }
	  else
		counter[2]++;
	}
	else
	  counter[2] = 0;
	
	Location loc;
	double myX = locX;
	double myY = locY;
	double myZ = locZ;
	
	if(!spout)
	{
	  if(!directionChanged)
	  {
		loc = p.getEyeLocation();
		double oldYaw = yaw;
		yaw = loc.getYaw() + 180;
		while(yaw > 360)
		  yaw -= 360;
		while(yaw < 0)
		  yaw += 360;
		
		if(yaw < 22.5F || yaw > 337.5D)
	          yaw = 0F;
	        else if(yaw < 67.5F)
	          yaw = 45F;
	        else if(yaw < 112.5F)
	          yaw = 90F;
	        else if(yaw < 157.5F)
	          yaw = 135F;
	        else if(yaw < 202.5F)
	          yaw = 180F;
	        else if(yaw < 247.5F)
	          yaw = 225F;
	        else if(yaw < 292.5F)
	          yaw = 270F;
	        else
	          yaw = 315F;
		
		if(loc.getPitch() < -45)
		  upDown = 2;
		else if(loc.getPitch() > 45)
		  upDown = 1;
		else
		  upDown = 0;
		
		if(oldYaw != yaw || myY != locY)
	      directionChanged = true;
	  }
	  
	  if(directionChanged)
	  {
		  //if(counter[0] < 60)
	    if(counter[0] < 20)
	      counter[0]++;
	    else
	    {
	      counter[0] = 0;
	      directionChanged = false;
	    }
	  }
	}
	
	double rideSpeed = plugin.rideSpeed + fl;
	if(upDown == 1)
	  myY -= rideSpeed;
	else if(upDown == 2)
	  myY += rideSpeed;
	
	if(yaw < 22.5F || yaw > 337.5F)
	  myZ -= rideSpeed;
	else if(yaw < 67.5F)
	{
	  double halfSpeed = rideSpeed / 2;
	  myZ -= halfSpeed;
	  myX += halfSpeed;
	}
	else if(yaw < 112.5F)
	  myX += rideSpeed;
	else if(yaw < 157.5F)
	{
	  double halfSpeed = rideSpeed / 2;
	  myX += halfSpeed;
	  myZ += halfSpeed;
	}
	else if(yaw < 202.5F)
	  myZ += rideSpeed;
	else if(yaw < 247.5F)
	{
	  double halfSpeed = rideSpeed / 2;
	  myZ += halfSpeed;
	  myX -= halfSpeed;
	}
	else if(yaw < 292.5)
	  myX -= rideSpeed;
	else
	{
	  double halfSpeed = rideSpeed / 2;
	  myX -= halfSpeed;
	  myZ -= halfSpeed;
	}
	
	org.bukkit.World w = world.getWorld();
	String wn = w.getName();
	if(plugin.mh.containsKey(wn))
	{
	  int my = plugin.mh.get(wn);
	  if(myY < my)
		myY = my;
	}
	
	if(myY < w.getMaxHeight())
	{
	  loc = new Location(w, myX, myY, myZ);
	  Block b = loc.getBlock();
	  if(plugin.stopGrief.contains(wn) && b != null && b.getType() != Material.AIR && !b.isLiquid())
	    return;
	  Vector v;
	  if(plugin.wg != null)
	  {
		v = toVector(loc);
		for(ProtectedRegion pr: plugin.wg.getRegionManager(w).getApplicableRegions(toVector(loc)))
		  if(pr.contains(v))
			return;
	  }
	  
	  if(plugin.bananAPI != null && plugin.bananAPI.isRegion(b))
	    return;
	  
	  if(plugin.resim != null && plugin.resim.getByLoc(loc) != null)
		return;
	  
	  if(plugin.townyu != null && b != null)
		for(TownBlock tb: plugin.townyu.getAllTownBlocks())
		  if(tb.getX() == b.getX() && tb.getZ() == b.getZ())
			return;
	  
	  if(plugin.factions && Board.getFactionAt(new FLocation(loc)) != null)
		return;
	}
	
	this.setPosition(myX, myY , myZ);
	strangeCode();
  }
  
  // Adjust sitting height, overwritten from Entity.class:
  // Doesn't seem to work anymore... :(
//  @Override
//  public double X()
//  {
//	return 3.3D;
//  }
  
  public Inventory getInventory()
  {
	return virtInv;
  }
  
  public boolean damageEntity(DamageSource damagesource, int i)
  {
    return false;
  }
  
  public boolean a(EntityComplexPart entitycomplexpart, DamageSource damagesource, int i)
  {
    return false;
  }
  
  private void strangeCode()
  {
	this.aA = this.yaw;
	this.bs.width = this.bs.length = 3.0F;
	this.bt.width = this.bt.length = 2.0F;
	this.bu.width = this.bu.length = 2.0F;
	this.bq.width = this.bq.length = 2.0F;
	
	this.bv.length = 3.0F;
	this.bv.width = 5.0F;
	this.bw.length = 2.0F;
	this.bw.width = 4.0F;
	
	this.br.length = 3.0F;
	this.br.width = 4.0F;
	float f1 = (float) (this.b(5, 1.0F)[1] - this.b(10, 1.0F)[1]) * 10.0F / 180.0F * 3.1415927F;
	float f2 = MathHelper.cos(f1);
	float f9 = -MathHelper.sin(f1);
	float f10 = this.yaw * 3.1415927F / 180.0F;
	float f11 = MathHelper.sin(f10);
	float f12 = MathHelper.cos(f10);

	this.bv.l_();
	this.bv.setPositionRotation(this.locX + (double) (f11 * 0.5F), this.locY, this.locZ - (double) (f12 * 0.5F), 0.0F, 0.0F);
	this.bw.l_();
	this.bw.setPositionRotation(this.locX + (double) (f12 * 4.5F), this.locY + 2.0D, this.locZ + (double) (f11 * 4.5F), 0.0F, 0.0F);
	this.bq.l_();
	this.bq.setPositionRotation(this.locX - (double) (f12 * 4.5F), this.locY + 2.0D, this.locZ - (double) (f11 * 4.5F), 0.0F, 0.0F);
	
	double[] adouble = this.b(5, 1.0F);
	double[] adouble1 = this.b(0, 1.0F);
	float f3 = MathHelper.sin(this.yaw * 3.1415927F / 180.0F - this.aw * 0.01F);
	float f13 = MathHelper.cos(this.yaw * 3.1415927F / 180.0F - this.aw * 0.01F);
	
	this.bs.l_();
	this.bs.setPositionRotation(this.locX + (double) (f3 * 5.5F * f2), this.locY + (adouble1[1] - adouble[1]) * 1.0D + (double) (f9 * 5.5F), this.locZ - (double) (f13 * 5.5F * f2), 0.0F, 0.0F);
	
	for (int j = 0; j < 3; ++j) {
	  EntityComplexPart entitycomplexpart = null;
	  
	  if (j == 0) {
		entitycomplexpart = this.bt;
	  }
	  
	  else if (j == 1) {
		  entitycomplexpart = this.bu;
	  }
	  
	  else if (j == 2) {
		  entitycomplexpart = this.bq;
	  }
	  
	  double[] adouble2 = this.b(12 + j * 2, 1.0F);
	  float f14 = this.yaw * 3.1415927F / 180.0F + (float) MathHelper.g(adouble2[0] - adouble[0]) * 3.1415927F / 180.0F * 1.0F;
	  float f15 = MathHelper.sin(f14);
	  float f16 = MathHelper.cos(f14);
	  float f17 = 1.5F;
	  float f18 = (float) (j + 1) * 2.0F;
	  
	  entitycomplexpart.l_();
	  entitycomplexpart.setPositionRotation(this.locX - (double) ((f11 * f17 + f15 * f18) * f2), this.locY + (adouble2[1] - adouble[1]) * 1.0D - (double) ((f18 + f17) * f9) + 1.5D, this.locZ + (double) ((f12 * f17 + f16 * f18) * f2), 0.0F, 0.0F);
	}

	this.au = false;
  }
  
  @Override
  public boolean canSpawn()
  {
	return true;
  }
  
  @Override
  public String r()
  {
	return plugin.silence ? null : super.r();
  }

  @Override
  public String aO()
  {
	return plugin.silence ? null : super.aO();
  }
}
