package net.gommehd.cheetah.event.entity;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a projectile collides with a player
 *
 * @author TrainmasterHD | Jendrik E.
 * @since 18.10.2022
 */
public class ProjectileAllowCollideWithPlayerEvent extends EntityEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final Player collidedWith;
    private boolean allowCollide = false;

    public ProjectileAllowCollideWithPlayerEvent(@NotNull Projectile what, @NotNull Player collidedWith) {
        super(what);
        this.collidedWith = collidedWith;
    }

    /**
     * Get the projectile that collided
     *
     * @return the projectile that collided
     */
    @NotNull
    public Projectile getEntity() {
        return (Projectile) super.getEntity();
    }

    /**
     * Get the entity the projectile collided with
     *
     * @return the entity collided with
     */
    @NotNull
    public Player getCollidedWith() {
        return collidedWith;
    }

    /**
     * Set if the projectile should be allowed to collide with the player
     *
     * @param allowCollide true if the projectile should be allowed to collide with the player, otherwise false
     */
    public void setAllowCollide(final boolean allowCollide) {
        this.allowCollide = allowCollide;
    }

    /**
     * If colliding with the player is allowed
     *
     * @return true if it is allowed, otherwise false
     */
    public boolean isAllowCollide() {
        return allowCollide;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
