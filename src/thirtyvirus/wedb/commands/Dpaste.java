/*
Project:     WorldEdit Database for CraftBukkit / Spigot
Author:      Brandon (ThirtyVirus) Calabrese
File:        dpaste.java
Description: the '//dpaste' command
 */

package thirtyvirus.wedb.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import thirtyvirus.wedb.WorldEditDatabase;

public class Dpaste implements CommandExecutor {

    private WorldEditDatabase main = null;
    public Dpaste(WorldEditDatabase main) {
        this.main = main;
    }

    /*
    Function:    onCommand
    Description: process the '//dpaste' command
                  Paste schematic from 'clipboard' section of database
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

            main.loadSchematic(player.getName(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockY(), player.getLocation().getWorld());

        }
        catch(Exception e) {
            sender.sendMessage(ChatColor.RED + "ERROR");
        }

        return true;
    }
}
