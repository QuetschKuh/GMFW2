package de.lolsu.gmfw.config;

import de.lolsu.gmfw.main.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/** A basic interface for a Yaml Configuration. Only supports read operations.<br>
 * For full functionality and direct access to the {@link FileConfiguration} object use {@link Config}. */
public class ConfigReader {

    protected final File file;
    protected final FileConfiguration config;

    public ConfigReader(String path) {
        file = new File(path);
        if(!file.exists()) {
            if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
            try {
                Files.createFile(Paths.get(path));
            } catch (Exception exception) {
                Logger.error("Couldn't create empty config file", exception);
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public ConfigReader(String path, InputStream def) {
        file = new File(path);
        try {
            if(!file.exists() || file.length() == 0) {
                if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                Files.copy(def, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            def.close();
        } catch (Exception exception) {
            Logger.error("Couldn't copy default config", exception);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    /*
     * Native getters
     * */

    /** Checks if the config contains this path and the object there isn't null */
    public boolean contains(String path) {
        return config.contains(path) && config.get(path) != null;
    }

    /** Gets an object from the FileConfiguration */
    public Object get(String path) { return config.get(path); }
    /** Gets an object from the FileConfiguration, returns the given {@code def} (default) when path is null */
    public Object get(String path, Object def) { return config.get(path, def); }

    /** Gets an Integer from the FileConfiguration */
    public int getInt(String path) { return config.getInt(path); }
    /** Gets an Integer from the FileConfiguration, returns the given {@code def} (default) when path is null */
    public int getInt(String path, int def) { return config.getInt(path, def); }

    /** Gets a string from the FileConfiguration */
    public String getString(String path) { return config.getString(path); }
    /** Gets a string from the FileConfiguration, returns the given {@code def} (default) when path is null */
    public String getString(String path, String def) { return config.getString(path, def); }

    /** Gets a string-list from the FileConfiguration */
    public List<String> getStringList(String path) { return config.getStringList(path); }

}
