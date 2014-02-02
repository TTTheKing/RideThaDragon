package de.V10lator.RideThaDragon;

import org.bukkit.craftbukkit.v1_6_R3.entity.CraftEnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.event.input.KeyPressedEvent;
import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.keyboard.Keyboard;
import org.getspout.spoutapi.player.SpoutPlayer;


class RTDSL implements Listener
{
  private final RideThaDragon plugin;
  
  RTDSL(RideThaDragon plugin)
  {
	this.plugin = plugin;
  }
  
  @EventHandler(priority = EventPriority.NORMAL)
  public void onKeyPressedEvent(KeyPressedEvent event)
  {
	SpoutPlayer sp = event.getPlayer();
	if(sp.getActiveScreen() != ScreenType.GAME_SCREEN)
	  return;
	String pn = sp.getName();
	if(!(RideThaDragon.dragons.containsKey(pn)))
	  return;
	Keyboard k = event.getKey();
	V10Dragon d = (V10Dragon)((CraftEnderDragon)RideThaDragon.dragons.get(pn)).getHandle();
	if(k == Keyboard.KEY_RIGHT)
	  d.yaw += 45;
	else if(k == Keyboard.KEY_LEFT)
	  d.yaw -= 45;
	else if(k == Keyboard.KEY_DOWN)
	{
	  if(d.upDown == 2)
	    d.upDown = 0;
	  else
		d.upDown = 1;
	}
	else if(k == Keyboard.KEY_UP)
	{
	  if(d.upDown == 1)
		d.upDown = 0;
	  else
		d.upDown = 2;
	}
	else if(k == sp.getSneakKey())
	  d.brr = !d.brr;
	else
	  return;
	if(d.yaw > 360)
	  d.yaw -= 360;
	else if(d.yaw < 0)
	  d.yaw += 360;
  }
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void registerText(SpoutCraftEnableEvent event)
  {
	plugin.registerTextures(event.getPlayer());
  }
}
