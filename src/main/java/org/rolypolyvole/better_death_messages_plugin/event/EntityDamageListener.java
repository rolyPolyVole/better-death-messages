package org.rolypolyvole.better_death_messages_plugin.event;

import org.bukkit.entity.Cat;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.rolypolyvole.better_death_messages_plugin.BetterDeathMessagesPlugin;

public class EntityDamageListener implements Listener {

    private final BetterDeathMessagesPlugin plugin;

    public EntityDamageListener(BetterDeathMessagesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Tameable tameableEntity)) {
            return;
        }

        if (!(tameableEntity instanceof Cat || tameableEntity instanceof Wolf || tameableEntity instanceof Parrot)) {
            return;
        }

        if (event.getDamage() >= tameableEntity.getHealth()) {
            tameableEntity.setOwner(null);

            PersistentDataContainer dataContainer = tameableEntity.getPersistentDataContainer();
            dataContainer.set(plugin.getShouldAnnounceEntityDeathKey(), PersistentDataType.BOOLEAN, true);
        }
    }
}
