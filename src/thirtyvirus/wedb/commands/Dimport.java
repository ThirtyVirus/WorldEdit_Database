/*
Project:     WorldEdit Database for CraftBukkit / Spigot
Author:      Brandon (ThirtyVirus) Calabrese
File:        dimport.java
Description: the '//dimport' command
 */

package thirtyvirus.wedb.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import thirtyvirus.wedb.WorldEditDatabase;

public class Dimport implements CommandExecutor {

    private WorldEditDatabase main = null;
    public Dimport(WorldEditDatabase main) {
        this.main = main;
    }

    /*
    Function:    onCommand
    Description: process the '//dimport' command
                 Imports schematic from 'saved' section of database
    Args:        sender  - the command sender
                 command - the command
                 label   - N/A
                 args    - command arguments
    Returns:     N/A
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        try {

            if (!(sender instanceof Player)) return false;
            Player player = (Player) sender;




        }
        catch(Exception e) {
            sender.sendMessage(ChatColor.RED + "ERROR");
        }

        return true;
    }
}
