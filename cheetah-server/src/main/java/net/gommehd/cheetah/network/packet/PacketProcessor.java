package net.gommehd.cheetah.network.packet;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.jspecify.annotations.Nullable;

/**
 * Interface for processing packets through the packet listener system.
 */
public interface PacketProcessor {
    /**
     * Processes an inbound packet through registered listeners.
     *
     * @param packet     the packet to process
     * @param connection the connection instance
     * @param <T>        the type of packet listener
     * @return the processed packet, or null if cancelled
     */
    <T extends PacketListener> @Nullable Packet<T> processInbound(Packet<T> packet, Connection connection);

    /**
     * Processes an outbound packet through registered listeners.
     *
     * @param packet     the packet to process
     * @param connection the connection instance
     * @return the processed packet, or null if cancelled
     */
    @Nullable Packet<?> processOutbound(Packet<?> packet, Connection connection);

    /**
     * Notifies listeners of packet processing errors.
     *
     * @param packet     the packet that caused the error
     * @param error      the exception that occurred
     * @param connection the connection instance
     */
    void notifyError(Packet<?> packet, Exception error, Connection connection);
}
