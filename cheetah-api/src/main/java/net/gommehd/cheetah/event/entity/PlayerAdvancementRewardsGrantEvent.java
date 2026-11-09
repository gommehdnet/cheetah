package net.gommehd.cheetah.event.entity;

import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

/**
 * Called when a {@link Player} is about to get advancement rewards.
 *
 * @author Summerfeeling
 * @since 15.10.2025
 */
@NullMarked
public class PlayerAdvancementRewardsGrantEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;

    private List<NamespacedKey> recipes;
    private List<ItemStack> items;
    private int experience;

    /**
     * Creates an event for advancement rewards.
     *
     * @param who        the player receiving the rewards
     * @param recipes    the recipes to unlock
     * @param items      the items to grant
     * @param experience the experience to grant
     */
    public PlayerAdvancementRewardsGrantEvent(final Player who, List<NamespacedKey> recipes, List<ItemStack> items, int experience) {
        super(who);

        this.experience = experience;
        this.recipes = recipes;
        this.items = items;
    }

    /**
     * Gets the recipes to unlock.
     *
     * @return the recipes to unlock
     */
    public List<NamespacedKey> getRecipes() {
        return this.recipes;
    }

    /**
     * Sets the recipes to unlock.
     *
     * @param recipes the recipes to unlock
     */
    public void setRecipes(List<NamespacedKey> recipes) {
        this.recipes = recipes;
    }

    /**
     * Gets the items to grant.
     *
     * @return the items to grant
     */
    public List<ItemStack> getItems() {
        return this.items;
    }

    /**
     * Sets the items to grant.
     *
     * @param items the items to grant
     */
    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    /**
     * Gets the experience to grant.
     *
     * @return the experience to grant
     */
    public int getExperience() {
        return this.experience;
    }

    /**
     * Sets the experience to grant.
     *
     * @param experience the experience to grant
     */
    public void setExperience(int experience) {
        this.experience = experience;
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
