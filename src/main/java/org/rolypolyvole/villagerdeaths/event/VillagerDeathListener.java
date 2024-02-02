package org.rolypolyvole.villagerdeaths.event;

import net.kyori.adventure.text.TextComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
            String serverClassPackageName = plugin.getServer().getClass().getPackage().getName();
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

            DamageSource damageSource = nmsVillager.getLastDamageSource();
            assert damageSource != null;

            Location deathLocation = villager.getLocation();

            Component deathMessage = damageSource.getLocalizedDeathMessage(nmsVillager).copy().append(" at (" + deathLocation.getBlockX() + ", " + deathLocation.getBlockY() + ", " + deathLocation.getBlockZ() + ")");
            TextComponent message = net.kyori.adventure.text.Component.text(deathMessage.getString());

            boolean announceToAll = Objects.equals(plugin.getConfig().getString("announce-to"), "everyone");

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
        }
    }
}
