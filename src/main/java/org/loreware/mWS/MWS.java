package org.loreware.mWS;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class MWS extends JavaPlugin implements Listener, CommandExecutor {

    FileConfiguration config;

    @Override
    public void onEnable() {
        System.out.println("MWS plugin enabled");

        saveResource("config.yml", /* replace */ true);
        config = getConfig();

//        saveDefaultConfig();
//        config = getConfig();
//        config.options().copyDefaults(true);
//        saveConfig();


        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("bankware")) {
                if (args.length == 1 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl"))) {
                    reloadConfig();
                    config = getConfig();

                    player.sendMessage(getConf("messages.prefix") + "§2Config reloaded.");
                    return true;
                }
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("bankware")) {
            if (sender instanceof Player) {
                List<String> list = new ArrayList<>();

                list.add("reload");
                return list;
            }
        }
        return null;
    }



    // ----------------- UTILS -----------------

    public String translateColor(String message){
        return message.replaceAll("&", "§").replaceAll("§§", "&");
    }

    void debug(String m){
        System.out.println(m);
    }

    public String getConf(String path){
        return translateColor(config.getString(path, String.format("&4&l[entry %s not found]", path)));
    }

    public List<String> getConfList(String path){
        List<String> list = new ArrayList<>();
        for (String line : config.getStringList(path)){
            list.add(translateColor(line));
        }
        return list;
    }

    // ----------------- UTILS -----------------

    @Override
    public void onDisable() {
        System.out.println("MWS plugin disabled");
    }
}
