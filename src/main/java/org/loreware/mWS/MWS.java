package org.loreware.mWS;

import com.google.common.base.Splitter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(6969), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/", new MWSHttpHandler());
        server.setExecutor(null);
        server.start();

        getServer().getPluginManager().registerEvents(this, this);
    }


    static class MWSHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            byte[] requestBody = t.getRequestBody().readAllBytes();
            String requestBodyString = new String(requestBody);
            String requestURI = t.getRequestURI().toString();
            String method = t.getRequestMethod();
            HashMap<String, List<String>> headers = new HashMap<>();
            t.getRequestHeaders().forEach((k, v) -> {
                headers.put(k.toLowerCase(), v);
            });

            debug("Received request: " + requestURI);
            debug("Request method: " + method);
            debug("Headers:");
            headers.forEach((k, v) -> debug(k + ": " + v));
            debug("Request body: " + requestBodyString);

            Bukkit.broadcast(Component.text("Received: " + requestBodyString));

            byte[] response = null;

            switch (method) {
                case "GET" -> {
                    switch (headers.get("x-storagemethod").getFirst()) {
                        case "binary" -> response = readBinData();
                        case "hexa" -> response = readHexaData();
                    }
                    debug("Response: " + new String(response));
                }

                case "POST" -> {
                    response = "Data received in the MWS plugin!".getBytes();
                    debug("Received POST request");
                    switch (headers.get("content-type").getFirst()) {
                        case "text/plain" -> {
                            debug("Handling plain text data");
                            switch (headers.get("x-storagemethod").getFirst()) {
                                case "binary" -> saveBinData(requestBody);
                                case "hexa" -> saveHexaData(requestBody);
                            }
                        }
                    }
                }

                case "OPTIONS" -> {
                    response = new byte[0];
                }
            }


            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, X-StorageMethod");

            assert response != null;
            t.sendResponseHeaders(200, response.length);


            // Write the response
            t.getResponseBody().write(response);
            t.getResponseBody().close();
        }
    }

    static final List<Material> binaryBlockList = new ArrayList<>(List.of(
            Material.WHITE_WOOL,
            Material.BLACK_WOOL));

    static final List<Material> hexaBlockList = new ArrayList<>(List.of(
            Material.WHITE_WOOL,
            Material.LIGHT_GRAY_WOOL,
            Material.GRAY_WOOL,
            Material.BLACK_WOOL,
            Material.BROWN_WOOL,
            Material.RED_WOOL,
            Material.ORANGE_WOOL,
            Material.YELLOW_WOOL,
            Material.LIME_WOOL,
            Material.GREEN_WOOL,
            Material.CYAN_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.BLUE_WOOL,
            Material.PURPLE_WOOL,
            Material.MAGENTA_WOOL,
            Material.PINK_WOOL));



    public static void saveBinData(byte[] data){
        debugb("Saving binary data: " + new String(data));
        int totalBits = data.length * 8;
        BitSet bitset = BitSet.valueOf(data);

        Bukkit.getServer().getScheduler().runTask(getPlugin(MWS.class), () -> {
            int i;
            for(i = 0; i < totalBits; i++){
                int bit = bitset.get(i) ? 1 : 0;

                new Location(Bukkit.getWorld("world"), i, 0, 0).getBlock().setType(binaryBlockList.get(bit));
            }
            new Location(Bukkit.getWorld("world"), i, 0, 0).getBlock().setType(Material.BEDROCK);
        });
    }

    public static byte[] readBinData(){
        StringBuilder bitArray = new StringBuilder();

        int i = 0;
        while(true){
            Material currBlock = new Location(Bukkit.getWorld("world"), i, 0, 0).getBlock().getType();

            if(currBlock == Material.BEDROCK) break;

            i += 1;
            if(currBlock == Material.BLACK_WOOL) bitArray.append("1");
            else bitArray.append("0");
        }

        int dataLen = (int) (i / 8);
        byte[] data = new byte[dataLen];

        AtomicInteger index = new AtomicInteger(1);

        String bitString = bitArray.reverse().toString();
        Splitter.fixedLength(8).split(bitString).forEach(s -> {
            byte dataChunk = (byte) Integer.parseInt(s, 2);
            data[dataLen - index.getAndIncrement()] = dataChunk;
        });


        return data;
    }

    public static void saveHexaData(byte[] data){
        Bukkit.getServer().getScheduler().runTask(getPlugin(MWS.class), () -> {
            Location currLoc = new Location(Bukkit.getWorld("world"), 0, 0, 0);
            for(byte b : data){
                int msb = (b >> 4) & 0x0F;
                int lsb = b & 0x0F;
                currLoc.getBlock().setType(hexaBlockList.get(msb));
                currLoc.add(1, 0, 0);
                currLoc.getBlock().setType(hexaBlockList.get(lsb));
                currLoc.add(1, 0, 0);
            }
            currLoc.getBlock().setType(Material.BEDROCK);
        });
    }

    public static byte[] readHexaData(){
        ByteArrayOutputStream data = new ByteArrayOutputStream();

        Location currLoc = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        while(true){
            Material msbBlock = currLoc.getBlock().getType();
            currLoc.add(1, 0, 0);
            Material lsbBlock = currLoc.getBlock().getType();
            currLoc.add(1, 0, 0);

            if(msbBlock == Material.BEDROCK || lsbBlock == Material.BEDROCK) break;

            int msb = hexaBlockList.indexOf(msbBlock);
            int lsb = hexaBlockList.indexOf(lsbBlock);

            byte dataChunk = (byte) (msb << 4 | lsb);
            data.write(dataChunk);
        }

        return data.toByteArray();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("mws")) {
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
        if (cmd.getName().equalsIgnoreCase("mws")) {
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

    static void debug(String m){
        System.out.println(m);
    }

    static void debugb(String m){
        System.out.println(m);
        Bukkit.broadcast(Component.text(m));
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
