package net.gommehd.cheetah.event.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

/**
 * @author deroq
 * @since 30.05.2025
 */
public class PlayerShieldBreakEvent extends PlayerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final @Nullable Entity damager;

    private boolean soundCancelled = false;

    public PlayerShieldBreakEvent(@NotNull Player player, @Nullable Entity damager) {
        super(player);
        this.damager = damager;
    }

    /**
     * Returns the entity that caused the shield to break, if available.
     * <p>
     * In previous versions, this method returned a non-null {@link Entity} reference when the shield was broken. Now,
     * it returns an {@link Optional}, which may be empty if no damager was recorded. This change improves null-safety
     * and allows better control flow when checking the presence of a damager.
     *
     * @return an {@link Optional} containing the entity that broke the shield, or an empty {@code Optional} if none
     * was recorded
     */
    public Optional<Entity> getDamager() {
        return Optional.ofNullable(damager);
    }

    /**
     * Checks whether the shield break sound is cancelled.
     * <p>
     * When this flag is set to {@code true}, the shield break sound will not be played when the shield is disabled due
     * to an attack.
     *
     * @return {@code true} if the shield break sound is cancelled, {@code false} otherwise
     */
    public boolean isSoundCancelled() {
        return soundCancelled;
    }

    /**
     * Sets whether the shield break sound should be cancelled.
     * <p>
     * When set to {@code true}, the shield break sound will be suppressed.
     *
     * @param soundCancelled {@code true} to suppress the shield break sound, {@code false} to allow it
     */
    public void setSoundCancelled(boolean soundCancelled) {
        this.soundCancelled = soundCancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
