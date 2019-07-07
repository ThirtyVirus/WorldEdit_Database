/*
Project:     WorldEdit Database for CraftBukkit / Spigot
Author:      Brandon (ThirtyVirus) Calabrese
File:        MainClass.java
Description: Main Class File in Project
 */

package thirtyvirus.wedb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteStreams;
import com.mysql.jdbc.Connection;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.WorldData;

import thirtyvirus.wedb.commands.Dcopy;
import thirtyvirus.wedb.commands.Dexport;
import thirtyvirus.wedb.commands.Dimport;
import thirtyvirus.wedb.commands.Dpaste;

public class WorldEditDatabase extends JavaPlugin {

    // console and IO
    private PluginDescriptionFile descFile = getDescription();
    public PluginManager pm = getServer().getPluginManager();
    public FileConfiguration shopsFileConfig;
    private FileConfiguration config;
    private Logger logger = getLogger();

    // dataBase vars.
    final String username="worldedit_user"; //Enter in your db username
    final String password="worldedit_password"; //Enter your password for the db
    final String url = "jdbc:mysql://184.95.52.170/worldedit_database"; //Enter URL w/db name

    // connection vars
    static Connection connection; //This is the variable we will use to connect to database

    // permissions
    //private Permission user = new Permission("ushops.user");
    //private Permission admin = new Permission("ushops.admin");

    // general settings
    private String prefix = "";
    private String consolePrefix = "";
    private boolean debug = true;

    //Processes to be carried out at server start
    public void onEnable() {

        // enforce WorldEdit Dependancy
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") == null) {
            this.getLogger().severe("WorldEdit Database requires WorldEdit! disabled because WorldEdit dependency not found");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // attempt database connection
        try { //We use a try catch to avoid errors, hopefully we don't get any.
            Class.forName("com.mysql.jdbc.Driver"); //this accesses Driver in jdbc.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("jdbc driver unavailable!");
            return;
        }
        try { //Another try catch to get any SQL errors (for example connections errors)
            connection = (Connection) DriverManager.getConnection(url, username, password);
            //with the method getConnection() from DriverManager, we're trying to set
            //the connection's url, username, password to the variables we made earlier and
            //trying to get a connection at the same time. JDBC allows us to do this.
        } catch (SQLException e) { //catching errors)
            e.printStackTrace(); //prints out SQLException errors to the console (if any)
        }

        //load config.yml (generate one if not there)
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()){
            loadResource(getPlugin(WorldEditDatabase.class), "config.yml");
        }

        loadConfiguration();

        registerCommands();
        registerEvents();
        registerPermissions();

        //posts confirmation in chat
        logger.info(descFile.getName() + " V: " + descFile.getVersion() + " has been enabled");
    }

    //Processes to be carried out at server stop
    public void onDisable() {

        // invoke on disable.
        try { //using a try catch to catch connection errors (like wrong sql password...)
            if (connection!=null && !connection.isClosed()){ //checking if connection isn't null to
                //avoid receiving a nullpointer
                connection.close(); //closing the connection field variable.
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        //posts exit message in chat
        logger.info(descFile.getName() + " V: " + descFile.getVersion() + " has been disabled");
    }

    //load config settings
    public void loadConfiguration() {
        config = this.getConfig();

        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        //autoPurge = config.getBoolean("auto-purge");
        //purgeAge = config.getInt("purge-age");

        //Chat settings
        //informCustomerOfTransaction = config.getBoolean("inform-customer-of-transaction");
        //customerBuyMessage = config.getString("customer-buy-message");

        if (debug) Bukkit.getLogger().info(consolePrefix + "Settings Reloaded from config");
        debug = config.getBoolean("debug");
    }

    //Register Commands
    private void registerCommands() {
        getCommand("/dcopy").setExecutor(new Dcopy(this));;
        getCommand("/dpaste").setExecutor(new Dpaste(this));;
        getCommand("/dimport").setExecutor(new Dimport(this));
        getCommand("/dexport").setExecutor(new Dexport(this));
    }

    //Register Events
    private void registerEvents() {
        //pm.registerEvents(new PlayerChat(), this);
        //pm.registerEvents(new PlayerJoin(this), this);
    }

    //Register Permissions
    private void registerPermissions() {
        //pm.addPermission(user);
        //pm.addPermission(admin);
    }

    //Loads file from JAR with comments
    public File loadResource(Plugin plugin, String resource) {
        File folder = plugin.getDataFolder();
        if (!folder.exists())
            folder.mkdir();
        File resourceFile = new File(folder, resource);
        try {
            if (!resourceFile.exists()) {
                resourceFile.createNewFile();
                try (InputStream in = plugin.getResource(resource);
                     OutputStream out = new FileOutputStream(resourceFile)) {
                    ByteStreams.copy(in, out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resourceFile;
    }

    //__________________________________________

    /*
    Function:    loadSchematic
    Description:
    Args:        N/A
    Returns:     N/A
     */
    public void loadSchematic(String filename, int x, int y, int z, org.bukkit.World world) {
        File dataDirectory = new File (this.getDataFolder(), "maps");
        File file = new File(dataDirectory, filename); // The schematic file
        Vector to = new Vector(x, y, z); // Where you want to paste
        World weWorld = new BukkitWorld(world);
        WorldData worldData = weWorld.getWorldData();
        Clipboard clipboard;
        try {
            clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(file)).read(worldData);
            Extent source = clipboard;
            Extent destination = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1);
            ForwardExtentCopy copy = new ForwardExtentCopy(source, clipboard.getRegion(), clipboard.getOrigin(), destination, to);
            copy.setSourceMask(new ExistingBlockMask(clipboard));
            Operations.completeLegacy(copy);
        } catch (IOException | WorldEditException e) {
            e.printStackTrace();
        }
    }

    /*
    Function:    saveSchematic
    Description:
    Args:        N/A
    Returns:     N/A
     */
    public void saveSchematic(String filename, int x1, int y1, int z1, int x2, int y2, int z2, org.bukkit.World world) {
        World weWorld = new BukkitWorld(world);
        WorldData worldData = weWorld.getWorldData();
        Vector pos1 = new Vector(x1, y1, z1); //First corner of your cuboid
        Vector pos2 = new Vector(x2, y2, z2); //Second corner fo your cuboid
        CuboidRegion cReg = new CuboidRegion(weWorld, pos1, pos2);
        File dataDirectory = new File (this.getDataFolder(), "maps");
        File file = new File(dataDirectory, filename + ".schematic"); // The schematic file
        try {
            BlockArrayClipboard clipboard = new BlockArrayClipboard(cReg);
            Extent source = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1);
            Extent destination = clipboard;
            ForwardExtentCopy copy = new ForwardExtentCopy(source, cReg, clipboard.getOrigin(), destination, pos1);
            copy.setSourceMask(new ExistingBlockMask(source));
            Operations.completeLegacy(copy);
            ClipboardFormat.SCHEMATIC.getWriter(new FileOutputStream(file)).write(clipboard, worldData);
        } catch (IOException | MaxChangedBlocksException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

}