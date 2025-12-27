package net.gommehd.cheetah.network.packet;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.BundlePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Default implementation of PacketProcessor that handles both regular and bundle packets.
 */
public class DefaultPacketProcessor implements PacketProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPacketProcessor.class);
    private final PacketListenerRegistry registry;
    private final BundlePacketHandler bundleHandler;

    public DefaultPacketProcessor() {
        this.registry = PacketListenerRegistry.getInstance();
        this.bundleHandler = new BundlePacketHandler(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends net.minecraft.network.PacketListener> Packet<T> processInbound(Packet<T> packet, Connection connection) {
        try {
            List<PacketListener> packetListeners =
                registry.getListeners(packet.getClass());

            if (packetListeners.isEmpty()) {
                return packet;
            }

            for (PacketListener packetListener : packetListeners) {
                try {
                    PacketListener.PacketProcessingResult result =
                        packetListener.onPacketReceive(connection, packet);

                    if (result.result() == PacketListener.PacketResult.MODIFIED) {
                        packet = (Packet<T>) result.modifiedPacket();
                    } else if (result.result() == PacketListener.PacketResult.CANCEL) {
                        return null;
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error in packet receive listener for {}", packet.getClass().getSimpleName(), e);
                    handleListenerError(packetListener, connection, packet, e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Critical error in inbound packet processing", e);
        }
        return packet;
    }

    @Override
    public Packet<?> processOutbound(Packet<?> packet, Connection connection) {
        if (packet instanceof BundlePacket<?> bundlePacket) {
            return bundleHandler.processBundlePacket(bundlePacket, connection);
        } else {
            return processRegularPacket(packet, connection);
        }
    }

    Packet<?> processRegularPacket(Packet<?> packet, Connection connection) {
        try {
            List<PacketListener> packetListeners =
                registry.getListeners(packet.getClass());

            if (packetListeners.isEmpty()) {
                return packet;
            }

            for (PacketListener packetListener : packetListeners) {
                try {
                    PacketListener.PacketProcessingResult result =
                        packetListener.onPacketSend(connection, packet);

                    if (result.result() == PacketListener.PacketResult.MODIFIED) {
                        packet = result.modifiedPacket();
                    } else if (result.result() == PacketListener.PacketResult.CANCEL) {
                        return null;
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error in packet send listener for {}", packet.getClass().getSimpleName(), e);
                    handleListenerError(packetListener, connection, packet, e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Critical error in outbound packet processing", e);
        }
        return packet;
    }

    @Override
    public void notifyError(Packet<?> packet, Exception error, Connection connection) {
        try {
            if (packet instanceof BundlePacket<?> bundlePacket) {
                bundleHandler.notifyBundleError(bundlePacket, error, connection);
            } else {
                notifyListenersOfError(packet, error, connection);
            }
        } catch (Exception notificationError) {
            LOGGER.error("Error while notifying packet listeners of error", notificationError);
        }
    }

    private void notifyListenersOfError(Packet<?> packet, Exception error, Connection connection) {
        List<PacketListener> packetListeners =
            registry.getListeners(packet.getClass());

        for (PacketListener packetListener : packetListeners) {
            try {
                packetListener.onPacketError(connection, packet, error);
            } catch (Exception errorHandlerException) {
                LOGGER.error("Error in packet error handler", errorHandlerException);
            }
        }
    }

    private void handleListenerError(PacketListener packetListener, Connection connection, Packet<?> packet, Exception error) {
        try {
            packetListener.onPacketError(connection, packet, error);
        } catch (Exception errorHandlerException) {
            LOGGER.error("Error in packet error handler", errorHandlerException);
        }
    }

    // Package-private getter for bundle handler
    PacketListenerRegistry getRegistry() {
        return registry;
    }
}
