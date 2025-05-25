package GhostyPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public interface Information {
    public static String Prefix = "[ V " + JavaPlugin.getPlugin(MainLoader.class).getDescription().getVersion() + " ] ";
}
