package net.gommehd.cheetah.event.entity;

import java.util.List;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} is about to get advancement rewards
 *
 * @author Summerfeeling
 * @since 15.10.2025
 */
public class PlayerAdvancementRewardsGrantEvent extends PlayerEvent implements Cancellable {


    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private List<NamespacedKey> recipes;
    private List<ItemStack> items;
    private int experience;

    public PlayerAdvancementRewardsGrantEvent(final @NotNull Player who, List<NamespacedKey> recipes, List<ItemStack> items, int experience) {
        super(who);

        this.experience = experience;
        this.recipes = recipes;
        this.items = items;
    }

    public @NotNull List<NamespacedKey> getRecipes() {
        return recipes;
    }

    public void setRecipes(@NotNull List<NamespacedKey> recipes) {
        this.recipes = recipes;
    }

    public @NotNull List<ItemStack> getItems() {
        return items;
    }

    public void setItems(@NotNull List<ItemStack> items) {
        this.items = items;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
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
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

}
