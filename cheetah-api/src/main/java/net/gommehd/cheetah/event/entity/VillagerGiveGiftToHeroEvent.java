package net.gommehd.cheetah.event.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called when a {@link Villager} is about to drop gifts to a hero of the village.
 *
 * @author Summerfeeling
 * @since 04.05.2023
 */
public class VillagerGiveGiftToHeroEvent extends EntityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private LivingEntity recipient;
    private List<ItemStack> itemStacks;

    public VillagerGiveGiftToHeroEvent(@NotNull Entity what, @NotNull LivingEntity recipient, @NotNull List<ItemStack> itemStacks) {
        super(what);

        this.itemStacks = itemStacks;
        this.recipient = recipient;
    }

    @NotNull
    public Villager getVillager() {
        return (Villager) getEntity();
    }

    @NotNull
    public LivingEntity getRecipient() {
        return recipient;
    }

    public void setRecipient(@NotNull LivingEntity recipient) {
        this.recipient = recipient;
    }

    @NotNull
    public List<ItemStack> getItemStacks() {
        return itemStacks;
    }

    public void setItemStacks(@NotNull List<ItemStack> itemStacks) {
        this.itemStacks = itemStacks;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
