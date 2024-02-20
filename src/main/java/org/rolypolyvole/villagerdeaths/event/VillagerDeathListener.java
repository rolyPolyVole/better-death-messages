package org.rolypolyvole.villagerdeaths.event;

import net.kyori.adventure.text.TextComponent;
import net.minecraft.network.chat.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.rolypolyvole.villagerdeaths.VillagerDeathMessagesPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
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

            boolean announceToAll = Objects.equals(plugin.getConfig().getString("announce-to"), "everyone");

            Bukkit.getLogger().info("message = " + message);

            if (announceToAll) {
                Bukkit.broadcast(message);
            } else {
                int announcementRadius = Math.abs(plugin.getConfig().getInt("announcement-radius"));

                for (Player player : deathLocation.getWorld().getPlayers()) {
                    if (deathLocation.distance(player.getLocation()) <= announcementRadius) {
                        player.sendMessage(message);
                    }
                }
            }

            // I tried so hard to use the DiscordSRV and JDA dependencies, but it just WOULD NOT work.
            // For this reason, I have to resort to the absolute cancer you can see down below. I am not proud of it, but it is necessary.

            Plugin discordSRV = plugin.getDiscordSRV();

            Object mainChannel;

            try {
                Class<? extends Plugin> discordSRVPluginClass = discordSRV.getClass();

                Bukkit.getLogger().info("discordSRVPluginClass = " + discordSRVPluginClass);

                Method getMainTextChannel = discordSRVPluginClass.getMethod("getMainTextChannel");

                Bukkit.getLogger().info("getMainTextChannel = " + getMainTextChannel);

                mainChannel = getMainTextChannel.invoke(discordSRV);

                Bukkit.getLogger().info("mainChannel = " + mainChannel);

                Class<?> mainChannelClass = mainChannel.getClass();

                Bukkit.getLogger().info("mainChannelClass = " + mainChannelClass);

                Class<?> messageEmbedClass = Class.forName("github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed");

                Bukkit.getLogger().info("messageEmbedClass = " + messageEmbedClass);

                Method sendMessageEmbeds = mainChannelClass.getMethod("sendMessage", messageEmbedClass);

                Bukkit.getLogger().info("sendMessageEmbeds = " + sendMessageEmbeds);

                Class<?> embedTypeEnum = Class.forName("github.scarsz.discordsrv.dependencies.jda.api.entities.EmbedType");

                Bukkit.getLogger().info("embedTypeEnum = " + embedTypeEnum);

                Class<?> thumbnailClass = (Class<?>) Arrays.stream(messageEmbedClass.getClasses()).filter((Class<?> c) -> {
                    Bukkit.getLogger().info("c = " + c);
                    Bukkit.getLogger().info("c.name = " + c.getName());

                    return c.getName().equals("github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed$Thumbnail");
                }).toArray()[0];

                Bukkit.getLogger().info("thumbnailClass = " + thumbnailClass);

                Class<?> providerClass = (Class<?>) Arrays.stream(messageEmbedClass.getClasses()).filter((Class<?> c) -> {
                    Bukkit.getLogger().info("c = " + c);
                    Bukkit.getLogger().info("c.name = " + c.getName());

                    return c.getName().equals("github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed$Provider");
                }).toArray()[0];

                Bukkit.getLogger().info("providerClass = " + providerClass);

                Class<?> authorInfoClass = (Class<?>) Arrays.stream(messageEmbedClass.getClasses()).filter((Class<?> c) -> c.getName().equals("github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed$AuthorInfo")).toArray()[0];

                Bukkit.getLogger().info("authorInfoClass = " + authorInfoClass);

                Class<?> videoInfoClass = (Class<?>) Arrays.stream(messageEmbedClass.getClasses()).filter((Class<?> c) -> c.getName().equals("github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed$VideoInfo")).toArray()[0];

                Bukkit.getLogger().info("videoInfoClass = " + videoInfoClass);

                Class<?> footerClass = (Class<?>) Arrays.stream(messageEmbedClass.getClasses()).filter((Class<?> c) -> c.getName().equals("github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed$Footer")).toArray()[0];

                Bukkit.getLogger().info("footerClass = " + footerClass);

                Class<?> imageInfoClass = (Class<?>) Arrays.stream(messageEmbedClass.getClasses()).filter((Class<?> c) -> c.getName().equals("github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed$ImageInfo")).toArray()[0];

                Bukkit.getLogger().info("imageInfoClass = " + imageInfoClass);

                Bukkit.getLogger().info("constructors = " + Arrays.toString(messageEmbedClass.getConstructors()));

                Constructor<?> messageEmbedConstructor = messageEmbedClass.getConstructor(
                        String.class,
                        String.class,
                        String.class,
                        embedTypeEnum,
                        OffsetDateTime.class,
                        int.class,
                        thumbnailClass,
                        providerClass,
                        authorInfoClass,
                        videoInfoClass,
                        footerClass,
                        imageInfoClass,
                        List.class
                );

                Bukkit.getLogger().info("messageEmbedConstructor = " + messageEmbedConstructor);

                Object messageEmbed = messageEmbedConstructor.newInstance(null, message.toString(), null, null, null, 0, null, null, null, null, null, null, null);

                Bukkit.getLogger().info("messageEmbed = " + messageEmbed);
                Bukkit.getLogger().info("messageEmbed.class = " + messageEmbed.getClass());

                Object result = sendMessageEmbeds.invoke(mainChannel, messageEmbed);

                Bukkit.getLogger().info("result = " + result);

                Class<?> resultClass = result.getClass();

                Bukkit.getLogger().info("result.class = " + resultClass);
                Bukkit.getLogger().info("resultClass.fields = " + Arrays.toString(resultClass.getFields()));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                     ClassNotFoundException | InstantiationException exception) {
                exception.printStackTrace();
                throw new RuntimeException(exception);
            }
        }
    }
}
