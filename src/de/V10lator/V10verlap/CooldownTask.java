package de.V10lator.V10verlap;

import java.util.HashSet;
import java.util.UUID;

class CooldownTask implements Runnable
{
  private final HashSet<UUID> cooldown;
  private final UUID uuid;
  
  CooldownTask(HashSet<UUID> cooldown, UUID uuid)
  {
	this.cooldown = cooldown;
	this.uuid = uuid;
  }
  
  public void run()
  {
	cooldown.remove(uuid);
  }
}
