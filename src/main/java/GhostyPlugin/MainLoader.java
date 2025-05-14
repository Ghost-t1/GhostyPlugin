package GhostyPlugin;

import GhostyPlugin.Modules.ArmorReseter;
import GhostyPlugin.Modules.DirtUpdater;
import org.bukkit.plugin.java.JavaPlugin;

import static GhostyPlugin.Information.Prefix;

public class MainLoader extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ArmorReseter(), this);
        getServer().getPluginManager().registerEvents(new DirtUpdater(), this);
        getLogger().info(Prefix + "Got it and loaded!");
    }

    @Override
    public void onDisable() {
        getLogger().info(Prefix + "Got it and unloaded!");
    }
}