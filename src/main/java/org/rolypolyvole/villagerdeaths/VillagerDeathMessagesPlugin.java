package org.rolypolyvole.villagerdeaths;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.rolypolyvole.villagerdeaths.event.VillagerDeathListener;

public final class VillagerDeathMessagesPlugin extends JavaPlugin {

    private final Plugin discordSRV = Bukkit.getPluginManager().getPlugin("DiscordSRV");

    public Plugin getDiscordSRV() {
        return discordSRV;
    }

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(new VillagerDeathListener(this), this);
    }
}
