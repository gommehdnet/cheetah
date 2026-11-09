package net.gommehd.cheetah.event.entity;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jspecify.annotations.NullMarked;

/**
 * Called when a projectile collides with a player.
 *
 * @author TrainmasterHD | Jendrik E.
 * @since 18.10.2022
 */
@NullMarked
public class ProjectileAllowCollideWithPlayerEvent extends EntityEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Player collidedWith;
    private boolean allowCollide = false;

    /**
     * Creates an event for a projectile colliding with a player.
     *
     * @param what         the projectile
     * @param collidedWith the player the projectile collided with
     */
    public ProjectileAllowCollideWithPlayerEvent(Projectile what, Player collidedWith) {
        super(what);
        this.collidedWith = collidedWith;
    }

    /**
     * Get the projectile that collided.
     *
     * @return the projectile that collided
     */
    public Projectile getEntity() {
        return (Projectile) super.getEntity();
    }

    /**
     * Get the entity the projectile collided with.
     *
     * @return the entity collided with
     */
    public Player getCollidedWith() {
        return this.collidedWith;
    }

    /**
     * Set if the projectile should be allowed to collide with the player.
     *
     * @param allowCollide true if the projectile should be allowed to collide with the player, otherwise false
     */
    public void setAllowCollide(final boolean allowCollide) {
        this.allowCollide = allowCollide;
    }

    /**
     * If colliding with the player is allowed.
     *
     * @return true if it is allowed, otherwise false
     */
    public boolean isAllowCollide() {
        return this.allowCollide;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
