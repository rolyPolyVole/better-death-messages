package org.rolypolyvole.better_death_messages.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.rolypolyvole.better_death_messages.BetterDeathMessagesPlugin;

public class EntityManager {

    private final BetterDeathMessagesPlugin plugin;

    public EntityManager(BetterDeathMessagesPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean shouldDeathBeAnnounced(Entity entity) {
        YamlConfiguration configuration = (YamlConfiguration) plugin.getConfig();

        ConfigurationSection deathMessageConfiguration = configuration.getConfigurationSection("death-messages");
        assert deathMessageConfiguration != null;

        if (entity instanceof WanderingTrader) {
            return deathMessageConfiguration.getBoolean("wandering-traders.enabled");
        } else if (entity instanceof Villager) {
            return deathMessageConfiguration.getBoolean("villagers.enabled");
        } else if (entity instanceof Tameable) {
            return deathMessageConfiguration.getBoolean("pets.enabled");
        } else if (entity.customName() != null) {
            return deathMessageConfiguration.getBoolean("named-entities.enabled");
        }

        return false;
    }
}
