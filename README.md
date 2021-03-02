# Minecraft Backdoor

There are probably much better backdoor templates out there, but here we are.
### Compile instructions:
* Add desired users UUID's into Config.java to allow them to use backdoor commands.
You can find minecraft UUID's at: [NameMC](www.namemc.com)
* Change other Config.java settings as desired.
* Build using Maven.
### Default commands
() = required [] = optional
* #op [player] - Set player to operator (sets self if no player specified)
* #deop [player] - Removes player from operator (Removes self if no player specified)
* #gamemode / gm (number / name) - Set self to specified gamemode.
* #give (item) [amount] - Gives the specified item in the specified quantity (defaults to 1)
* #chaos - Deop and Ban all ops currently online (UUIDs in config.java are exempt). Give admin to everyone else.
