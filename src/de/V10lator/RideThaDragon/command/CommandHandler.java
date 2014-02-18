/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.V10lator.RideThaDragon.command;

import de.V10lator.RideThaDragon.RideThaDragon;
import de.V10lator.RideThaDragon.Util;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Daniel
 */
public abstract class CommandHandler extends Util {   

    public final String MsgHr;
    
    public CommandHandler(RideThaDragon plugin) {
        super(plugin);
        this.MsgHr = "&e-----------------------------------------------------";
    }
    
    protected abstract Boolean OnCommand(CommandSender sender, String[] args);
    protected abstract Boolean isInvalid(CommandSender sender, String[] args);
}
