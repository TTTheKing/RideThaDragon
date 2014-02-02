package de.V10lator.V10verlap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class PlayerWorldToWorldTpEvent extends V10verlapEvent
{
  private static final HandlerList handlers = new HandlerList();
  private final Player player;
  
  public PlayerWorldToWorldTpEvent(Player player, Location to)
  {
	super(player.getLocation(), to);
	this.player = player;
  }
  
  public Player getPlayer()
  {
	return player;
  }
  
  public HandlerList getHandlers()
  {
    return handlers;
  }
   
  public static HandlerList getHandlerList() 
  {
    return handlers;
  }
}
