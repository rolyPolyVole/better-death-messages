package org.rolypolyvole.better_death_messages.event;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.rolypolyvole.better_death_messages.BetterDeathMessagesPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class EntityDeathListener implements Listener {

    private final BetterDeathMessagesPlugin plugin;

    public EntityDeathListener(BetterDeathMessagesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVillagerDeath(@NotNull EntityDeathEvent event) {
        String serverClassPackageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = serverClassPackageName.substring(serverClassPackageName.lastIndexOf('.') + 1);

        Class<?> craftEntity;

        try {
            craftEntity = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftEntity");
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }

        Method getHandle;

        try {
            getHandle = craftEntity.getMethod("getHandle");
        } catch (NoSuchMethodException exception) {
            throw new RuntimeException(exception);
        }

        LivingEntity entity = event.getEntity();

        net.minecraft.world.entity.LivingEntity nmsEntity;

        try {
            nmsEntity = (net.minecraft.world.entity.LivingEntity) getHandle.invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }

        Location deathLocation = entity.getLocation();

        MutableComponent deathMessage = nmsEntity.getCombatTracker().getDeathMessage().copy();

        YamlConfiguration configuration = (YamlConfiguration) plugin.getConfig();

        ConfigurationSection messageSettings = configuration.getConfigurationSection("messages");
        assert messageSettings != null;

        boolean includeCoordinates = messageSettings.getBoolean("include-coordinates");

        TextComponent message = net.kyori.adventure.text.Component.text(deathMessage.getString());

        if (includeCoordinates) {
            deathMessage.append(Component.literal(" @ (" + deathLocation.getBlockX() + ", " + deathLocation.getBlockY() + ", " + deathLocation.getBlockZ() + ")"));
        }

        boolean announceToAll = Objects.equals(configuration.getString("announce-to"), "everyone");

        Bukkit.getLogger().info("message = " + message);

        int announcementRadius = Math.abs(configuration.getInt("announcement-radius"));

        for (Player player : deathLocation.getWorld().getPlayers()) {
            if (announceToAll || deathLocation.distance(player.getLocation()) <= announcementRadius) {
                try {
                    Method getPlayerHandle = player.getClass().getMethod("getHandle");

                    ServerPlayer serverPlayer = (ServerPlayer) getPlayerHandle.invoke(player);

                    serverPlayer.displayClientMessage(
                            deathMessage,
                            false // Whether the message is an action bar message
                    );
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }

        String channelId = messageSettings.getString("discord-messages.channel-id");
        assert channelId != null;

        JDA jda = plugin.getJda();

        TextChannel textChannel = jda.getChannelById(TextChannel.class, channelId);
        assert textChannel != null;

        String messageString = message.content();

        MessageCreateAction messageCreateAction = textChannel.sendMessageEmbeds(new MessageEmbed(
                null,
                null,
                null,
                null,
                null,
                0,
                null,
                null,
                new MessageEmbed.AuthorInfo(messageString, null, null, null),
                null,
                null,
                null,
                null
        ));

        messageCreateAction.submit();
    }
}
