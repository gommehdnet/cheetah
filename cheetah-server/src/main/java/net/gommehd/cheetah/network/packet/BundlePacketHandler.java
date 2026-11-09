package net.gommehd.cheetah.network.packet;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles processing of bundle packets and their sub-packets.
 *
 * @param processor the processor for regular packets
 */
public record BundlePacketHandler(DefaultPacketProcessor processor) {
    private static final Logger LOGGER = LoggerFactory.getLogger(BundlePacketHandler.class);

    public @Nullable Packet<?> processBundlePacket(BundlePacket<?> bundlePacket, Connection connection) {
        // Process sub-packets
        List<Packet<?>> processedSubPackets = new ArrayList<>();
        boolean anySubPacketModified = false;

        for (Packet<?> subPacket : bundlePacket.subPackets()) {
            Packet<?> processedSubPacket = this.processor.processRegularPacket(subPacket, connection);
            if (processedSubPacket != null) {
                processedSubPackets.add(processedSubPacket);
                if (processedSubPacket != subPacket) {
                    anySubPacketModified = true;
                }
            } else {
                anySubPacketModified = true; // Sub-packet was cancelled
            }
        }

        // If all sub-packets were cancelled, cancel the entire bundle
        if (processedSubPackets.isEmpty()) {
            return null;
        }

        // Reconstruct bundle if sub-packets were modified/cancelled
        Packet<?> resultPacket = bundlePacket;
        if (anySubPacketModified || processedSubPackets.size() != this.getBundleSubPacketCount(bundlePacket)) {
            resultPacket = this.reconstructBundlePacket(bundlePacket, processedSubPackets);
        }

        // Process the bundle packet itself
        return this.processor.processRegularPacket(resultPacket, connection);
    }

    public void notifyBundleError(BundlePacket<?> bundlePacket, Exception error, Connection connection) {
        // Notify listeners about error for each sub-packet
        for (Packet<?> subPacket : bundlePacket.subPackets()) {
            this.notifyListenersOfError(subPacket, error, connection);
        }

        // Notify listeners about the bundle packet error
        this.notifyListenersOfError(bundlePacket, error, connection);
    }

    private void notifyListenersOfError(Packet<?> packet, Exception error, Connection connection) {
        List<PacketListener> packetListeners =
            this.processor.getRegistry().getListeners(packet.getClass());

        for (PacketListener packetListener : packetListeners) {
            try {
                packetListener.onPacketError(connection, packet, error);
            } catch (Exception errorHandlerException) {
                LOGGER.error("Error in packet error handler", errorHandlerException);
            }
        }
    }

    private Packet<?> reconstructBundlePacket(BundlePacket<?> originalBundle, List<Packet<?>> processedSubPackets) {
        if (originalBundle instanceof ClientboundBundlePacket) {
            @SuppressWarnings("unchecked")
            List<Packet<? super ClientGamePacketListener>> typedSubPackets =
                (List<Packet<? super ClientGamePacketListener>>) (List<?>) processedSubPackets;
            return new ClientboundBundlePacket(typedSubPackets);
        }
        // Add support for other bundle packet types as needed
        return originalBundle;
    }

    private int getBundleSubPacketCount(BundlePacket<?> bundlePacket) {
        int count = 0;
        for (Packet<?> _ : bundlePacket.subPackets()) {
            count++;
        }
        return count;
    }
}
