package net.gommehd.cheetah.event.entity;

import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

/**
 * Called when a {@link Villager} is about to drop gifts to a hero of the village.
 *
 * @author Summerfeeling
 * @since 04.05.2023
 */
@NullMarked
public class VillagerGiveGiftToHeroEvent extends EntityEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private LivingEntity recipient;
    private List<ItemStack> itemStacks;

    /**
     * Creates an event for gifts to a hero of the village.
     *
     * @param what       the villager giving the gifts
     * @param recipient  the entity receiving the gifts
     * @param itemStacks the gifts to drop
     */
    public VillagerGiveGiftToHeroEvent(Entity what, LivingEntity recipient, List<ItemStack> itemStacks) {
        super(what);

        this.itemStacks = itemStacks;
        this.recipient = recipient;
    }

    /**
     * Gets the villager giving the gifts.
     *
     * @return the villager
     */
    public Villager getVillager() {
        return (Villager) getEntity();
    }

    /**
     * Gets the entity receiving the gifts.
     *
     * @return the recipient
     */
    public LivingEntity getRecipient() {
        return this.recipient;
    }

    /**
     * Sets the entity receiving the gifts.
     *
     * @param recipient the recipient
     */
    public void setRecipient(LivingEntity recipient) {
        this.recipient = recipient;
    }

    /**
     * Gets the gifts to drop.
     *
     * @return the gifts
     */
    public List<ItemStack> getItemStacks() {
        return this.itemStacks;
    }

    /**
     * Sets the gifts to drop.
     *
     * @param itemStacks the gifts to drop
     */
    public void setItemStacks(List<ItemStack> itemStacks) {
        this.itemStacks = itemStacks;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
