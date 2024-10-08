# Gamemode Framework 2.0

### Warning!
This is still heavily in development and not everything listed here is already realized.
In the meantime you may take a look at GMFW (the first one). It already contains all the features you may want, just a lot less user friendly, with barren documentation and some spaghetti code. Development has long stopped but I still think it's a good reference and tool. I'm always available for questions or example code.
The old framework (with skywars as a gamemode) is, in a minorly bugged state, running on the Minecraft server `lolsu.de` right now if you want to check it out. 3 Players are needed for the game to start.

### Introduction
This Minecraft 1.8.8 plugin framework, written in java, provides the best tools for your own Minecraft gamemode (e.g. SkyWars). Fully customizable and easy to use, GMFW2 gets rid of most of the tedious work and let's you focus on the creative process of creating a fun experience on your own Minecraft server.

### Features
* Full handling of lobby, podium, maps and more
* Voting, teaming, tracking of statistics
* An easy in-game setup utility for map creation, spawns, and more
* Full customization and documentation
* So easy to use that you can make your own gamemode in just a few hours
* Small and efficient, requires just 512MB of RAM for 12 players!

### Installation
Just download the most recent .jar file in the releases tab on GitHub or from [the homepage](<https://lolsu.de/mc/gmfw2>). After that you just include it as a library in your project (as you would with spigot) and done! You can now fully use the Gamemode Framework 2.0!

### Usage
! A full guide with videos, example code and troubleshooting is available on [the homepage](<https://lolsu.de/mc/gmfw2/tutorial>) !<br>
! An easy and thoroughly commented example project is available [here](<https://github.com/QuetschKuh/GMFW2-example>) !<br>
! All plugins I created with GMFW2 and are online on `lolsu.de` (SkyWars, ...) are also released on [my GitHub](<https://github.com/QuetschKuh>) !

Start by creating your Main-class as you would in a normal spigot plugin.

```
public class Main extends JavaPlugin {

    public static Main plugin;

    public void onEnable() {
        plugin = this;
    }

    public void onDisable() {

    }

}
```

Now you want to add a few core things in your onEnable function:
1. Create a new Gamemode instance and pass in your plugin.
```
Gamemode gamemode = new Gamemode(plugin);
```

2. Configure your gamemode using the config enums, all configuration variables are `0` (=`false`) by default. Strings and other deviating defaults are noted in the javadocs
```
ConfVar.DEATH_DROPS_ALL.setState(true); // Drop all items on death
ConfVar.MODULE_SOUP_HEALING.setInt(4);  // Heal 4 hp when consuming soup
...
```

3. Register the event listeners that should be enabled during the game
```
Listeners.addIngame(new DamageEvents());
Listeners.addIngame(new ChestEvents());
...
```

4. Register your own managers (if you have them)
```
gamemode.setStateMan(new StateManagerGM());
gamemode.setPlayerMan(new PlayerManagerGM());
```

5. Finally, when all is set and done, start the game
```
gamemode.start();
```

To create a listener/event you do the same as with standard spigot.
For a custom manager you're going to want to just extend the class using something along the lines of

```
public class EventCustom extends EventManager {

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        super.onPlayerJoin(event);  // Executes the default behaviour

        // Your code, executed when the default behaviour is done
    }

    @Override
    public void onPlayerDeath(EntityDamageEvent event) {
        // Your code, by default the code is overridden if you don't include the super.function(parameters);
    }

}
```

Here's a list of extendable classes:
Now compile your plugin - you mustn't include the framework in the servers plugin folder, it just ships right with the built .jar -, boot up your server and enjoy!
