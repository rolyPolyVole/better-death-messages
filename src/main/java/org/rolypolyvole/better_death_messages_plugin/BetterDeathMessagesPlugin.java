package org.rolypolyvole.better_death_messages_plugin;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.rolypolyvole.better_death_messages_plugin.event.EntityDamageListener;
import org.rolypolyvole.better_death_messages_plugin.event.EntityDeathListener;
import org.rolypolyvole.better_death_messages_plugin.manager.EntityManager;

public final class BetterDeathMessagesPlugin extends JavaPlugin {

    private JDA jda;

    private EntityManager entityManager;

    private NamespacedKey shouldAnnounceEntityDeathKey = new NamespacedKey(this, "should_announce_death");

    public JDA getJda() {
        return jda;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public NamespacedKey getShouldAnnounceEntityDeathKey() {
        return shouldAnnounceEntityDeathKey;
    }

    @Override
    public void onEnable() {
        YamlConfiguration configuration = (YamlConfiguration) getConfig();

        configuration.options().copyDefaults();
        saveDefaultConfig();

        ConfigurationSection discordMessageSettings = configuration.getConfigurationSection("messages.discord-messages");
        assert discordMessageSettings != null;

        boolean discordEnabled = discordMessageSettings.getBoolean("enabled");

        if (discordEnabled) {
            String token = discordMessageSettings.getString("bot-token");

            JDABuilder jdaBuilder = JDABuilder.createDefault(token);

            try {
                jda = jdaBuilder.build().awaitReady();
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        }

        entityManager = new EntityManager(this);

        Bukkit.getPluginManager().registerEvents(new EntityDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(this), this);
    }
}
