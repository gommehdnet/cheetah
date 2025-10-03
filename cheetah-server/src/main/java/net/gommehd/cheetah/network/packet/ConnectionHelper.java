package net.gommehd.cheetah.network.packet;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jspecify.annotations.Nullable;

/**
 * Helper utility for connection-related packet processing operations.
 */
public class ConnectionHelper {
    /**
     * Extracts Connection instance from PacketListener context.
     *
     * @param listener the packet listener
     * @return the connection instance, or null if not found
     */
    @Nullable
    public static Connection getCurrentConnection(PacketListener listener) {
        if (listener instanceof ServerCommonPacketListenerImpl serverListener) {
            return serverListener.connection;
        }
        // For other listener types, try to find connection through reflection or other means
        // This is implementation-specific and may need adjustment based on your server version
        return null;
    }
}
