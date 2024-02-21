package org.rolypolyvole.villagerdeaths.event;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.network.chat.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.rolypolyvole.villagerdeaths.VillagerDeathMessagesPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class VillagerDeathListener implements Listener {

    private final VillagerDeathMessagesPlugin plugin;

    public VillagerDeathListener(VillagerDeathMessagesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVillagerDeath(@NotNull EntityDeathEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            String serverClassPackageName = Bukkit.getServer().getClass().getPackage().getName();
            String version = serverClassPackageName.substring(serverClassPackageName.lastIndexOf('.') + 1);

            Class<?> craftVillager;

            try {
                craftVillager = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftVillager");
            } catch (ClassNotFoundException exception) {
                throw new RuntimeException(exception);
            }

            Method getHandle;

            try {
                getHandle = craftVillager.getMethod("getHandle");
            } catch (NoSuchMethodException exception) {
                throw new RuntimeException(exception);
            }

            net.minecraft.world.entity.npc.Villager nmsVillager;

            try {
                nmsVillager = (net.minecraft.world.entity.npc.Villager) getHandle.invoke(villager);
            } catch (IllegalAccessException | InvocationTargetException exception) {
                throw new RuntimeException(exception);
            }

            Location deathLocation = villager.getLocation();

            Component deathMessage = nmsVillager.getCombatTracker().getDeathMessage().copy().append(" at (" + deathLocation.getBlockX() + ", " + deathLocation.getBlockY() + ", " + deathLocation.getBlockZ() + ")");
            TextComponent message = net.kyori.adventure.text.Component.text(deathMessage.getString());

            YamlConfiguration configuration = (YamlConfiguration) plugin.getConfig();

            boolean announceToAll = Objects.equals(configuration.getString("announce-to"), "everyone");

            Bukkit.getLogger().info("message = " + message);

            if (announceToAll) {
                Bukkit.broadcast(message);
            } else {
                int announcementRadius = Math.abs(configuration.getInt("announcement-radius"));

                for (Player player : deathLocation.getWorld().getPlayers()) {
                    if (deathLocation.distance(player.getLocation()) <= announcementRadius) {
                        player.sendMessage(message);
                    }
                }
            }

            String channelId = configuration.getString("discord-messages.channel-id");
            assert channelId != null;

            JDA jda = plugin.getJda();

            TextChannel textChannel = jda.getChannelById(TextChannel.class, channelId);
            assert textChannel != null;

            String messageString = message.content();

            Villager.Type villagerType = villager.getVillagerType();
            String villagerTypeString = villagerType.toString().toLowerCase();
            villagerTypeString = String.valueOf(villagerTypeString.charAt(0)).toUpperCase() + villagerTypeString.substring(1);

            Villager.Profession villagerProfession = villager.getProfession();
            String villagerProfessionString = villagerProfession.toString().toLowerCase();
            villagerProfessionString = String.valueOf(villagerProfessionString.charAt(0)).toUpperCase() + villagerProfessionString.substring(1);

            String villagerImageLink = "https://static.wikia.nocookie.net/minecraft_gamepedia/images/5/50/" + villagerTypeString + "_" + villagerProfessionString + ".png/revision/latest?cb=20190327151407";

            Bukkit.getLogger().info(villagerImageLink);

            MessageCreateAction messageCreateAction = textChannel.sendMessageEmbeds(new MessageEmbed(
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    null,
                    null,
                    new MessageEmbed.AuthorInfo(messageString, null, villagerImageLink, null),
                    null,
                    null,
                    null,
                    null
            ));

            messageCreateAction.submit();
        }
    }
}
