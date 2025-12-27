package net.gommehd.cheetah.network.packet;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.Connection;

/**
 * Interface for handling network packets during their send and receive lifecycle.
 * <p>
 * The PacketListener interface provides methods for intercepting, modifying, or
 * canceling packets as they are sent or received. Listeners can be used to implement
 * custom processing logic, debugging, or packet filtering systems.
 */
public interface PacketListener {

    /**
     * Called when a packet is being sent. This method allows the inspection
     * or modification of the packet and determines the desired action to be
     * performed (e.g., pass, cancel, modify).
     *
     * @param connection the connection through which the packet is being sent, never null
     * @param packet the packet that is being sent, never null
     * @return a {@link PacketProcessingResult} indicating the result of the packet processing,
     *         such as whether the packet should be passed, canceled, or modified
     */
    default PacketProcessingResult onPacketSend(Connection connection, Packet<?> packet) {
        return PacketProcessingResult.pass();
    }

    /**
     * Called when a packet is received. This method allows inspection, modification,
     * or determination of the action to take with the received packet.
     *
     * @param connection the connection through which the packet was received, never null
     * @param packet the packet that was received, never null
     * @return a {@link PacketProcessingResult} indicating the result of the packet processing,
     *         such as whether the packet should be passed, canceled, or modified
     */
    default PacketProcessingResult onPacketReceive(Connection connection, Packet<?> packet) {
        return PacketProcessingResult.pass();
    }

    /**
     * Retrieves the priority level assigned to the listener.
     * The priority determines the order in which the listener is executed
     * relative to other listeners when processing events.
     *
     * @return the {@link ListenerPriority} of this listener, by default {@link ListenerPriority#NORMAL}
     */
    default ListenerPriority priority() {
        return ListenerPriority.NORMAL;
    }

    /**
     * Invoked when a packet error occurs. This method provides an opportunity to handle
     * issues that arise during packet processing, such as exceptions encountered while
     * sending or receiving packets.
     *
     * @param connection the connection associated with the packet, never null
     * @param packet the packet involved in the error, can be null if unavailable
     * @param exception the exception that was thrown during packet processing, never null
     */
    default void onPacketError(Connection connection, Packet<?> packet, Exception exception) {
        // Default implementation does nothing
    }

    /**
     * Represents the result of processing a network packet within a {@link PacketListener}.
     * <p>
     * This record encapsulates the outcome of packet processing and optionally provides
     * a modified version of the packet.
     * It is used to determine whether to pass the packet
     * onward unmodified, cancel its processing entirely, or proceed with a modified version.
     */
    record PacketProcessingResult(PacketResult result, Packet<?> modifiedPacket) {
        public static PacketProcessingResult pass() {
            return new PacketProcessingResult(PacketResult.PASS, null);
        }

        public static PacketProcessingResult cancel() {
            return new PacketProcessingResult(PacketResult.CANCEL, null);
        }

        public static PacketProcessingResult modify(Packet<?> newPacket) {
            return new PacketProcessingResult(PacketResult.MODIFIED, newPacket);
        }
    }

    /**
     * Represents the outcome of a packet processing operation within the context of a {@code PacketListener}.
     * <p>
     * This enum defines the possible results that can be returned after handling a packet, indicating
     * whether the packet should be passed through unaltered, canceled, or forwarded in a modified state.
     * These outcomes are used to control the flow and behavior of packet processing in the packet handling
     * pipeline.
     */
    enum PacketResult {
        /**
         * Indicates that the packet should pass through without any modifications or interruptions.
         * <p>
         * This result signifies that the currently handled packet is to proceed to the next stage
         * of processing without any changes and without being canceled. It allows the packet to
         * continue through the pipeline as it is.
         */
        PASS,

        /**
         * Indicates that packet processing should be canceled.
         * <p>
         * This result signifies that the processing of the currently handled
         * packet should be halted. No further actions will be taken on the packet,
         * and it will not be passed to subsequent handlers in the pipeline.
         * This is typically used to prevent undesired processing or propagation
         * of the packet.
         */
        CANCEL,

        /**
         * Indicates that packet processing should proceed with a modified packet.
         * <p>
         * This result is used when a packet has been altered during processing and
         * should continue through the pipeline in its modified state. Subsequent
         * handlers will receive and process the modified version of the packet.
         */
        MODIFIED
    }

    /**
     * Represents different priority levels for listener execution in the system.
     * The priority levels determine the order in which listeners are executed when processing events,
     * with higher numeric values being executed earlier.
     */
    enum ListenerPriority {
        LOWEST(0),

        LOW(1),

        NORMAL(2),

        HIGH(3),

        HIGHEST(4),

        MONITOR(5);

        private final int priority;

        ListenerPriority(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }
}
