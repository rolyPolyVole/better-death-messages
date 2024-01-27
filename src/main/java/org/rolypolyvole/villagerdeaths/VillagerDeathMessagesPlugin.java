package org.rolypolyvole.villagerdeaths;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.rolypolyvole.villagerdeaths.event.VillagerDeathListener;

public final class VillagerDeathMessagesPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(new VillagerDeathListener(this), this);
    }
}
