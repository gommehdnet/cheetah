package net.gommehd.cheetah.network.packet;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.Connection;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A builder utility class for creating packet listeners.
 * <p>
 * Packet listeners are used to handle packets sent and received within a network connection.
 * This class provides two types of builders:
 * - GlobalBuilder: For listeners that process all packet types.
 * - SpecificBuilder: For listeners that process specific packet types.
 * <p>
 * Use the static methods provided by this class to create the corresponding builder.
 */
public class PacketListenerBuilder {
    /**
     * Creates a new global packet listener builder.
     * Global listeners receive all packets, regardless of their type.
     *
     * @return a new instance of {@code GlobalBuilder}
     */
    public static GlobalBuilder createGlobal() {
        return new GlobalBuilder();
    }

    /**
     * Creates a new specific packet listener builder.
     * Specific listeners handle packets of only the configured types.
     *
     * @return a new instance of {@code SpecificBuilder}
     */
    public static SpecificBuilder createSpecific() {
        return new SpecificBuilder();
    }

    /**
     * BaseBuilder is an abstract class that provides a foundation for constructing packet listener objects.
     * It supports configuring common properties such as priority and error handling, which can be
     * used across different types of listeners.
     *
     * @param <T> the type of the builder extending from this base class, enabling method chaining
     */
    protected static abstract class BaseBuilder<T extends BaseBuilder<T>> {
        protected PacketListener.ListenerPriority priority = PacketListener.ListenerPriority.NORMAL;
        protected BiFunction<Connection, Exception, Void> errorHandler;

        @SuppressWarnings("unchecked")
        private T self() {
            return (T) this;
        }

        /**
         * Sets the priority level for the packet listener.
         * The priority determines the order in which the listener will
         * handle events relative to other listeners.
         * Higher priorities
         * are executed before lower ones.
         *
         * @param priority the {@code ListenerPriority} to assign to the packet listener
         * @return the current builder instance for method chaining
         */
        public T withPriority(PacketListener.ListenerPriority priority) {
            this.priority = priority;
            return self();
        }

        /**
         * Configures a custom error handler for handling exceptions that occur during packet processing.
         * The provided handler will be invoked with the connection and exception details when an error is encountered.
         *
         * @param handler a {@code BiFunction} that accepts a {@code Connection} and an {@code Exception}, and
         *                performs error-handling logic. The handler must not be {@code null}.
         * @return the current builder instance for method chaining
         * @throws NullPointerException if the provided {@code handler} is {@code null}
         */
        public T onError(BiFunction<Connection, Exception, Void> handler) {
            this.errorHandler = Objects.requireNonNull(handler, "handler cannot be null");
            return self();
        }

        /**
         * Registers and enables the constructed {@link PacketListener}.
         * <p>
         * This method finalizes the configuration of the {@link PacketListener} and adds it
         * to the system for handling packet events such as sending or receiving packets.
         * Once registered, the {@link PacketListener} will begin processing packets based
         * on its implemented logic and configured priority.
         *
         * @return the newly created and registered {@link PacketListener} instance
         */
        public abstract PacketListener register();

        /**
         * Constructs a new instance of a {@link PacketListener} based on the current configuration
         * of the builder.
         *
         * <p>This method finalizes the construction of the {@link PacketListener}, applying all
         * configured settings such as priority and error handling. The returned {@link PacketListener}
         * can then be registered or used directly to intercept packet events.
         *
         * @return a fully constructed {@link PacketListener} instance ready for use
         */
        public abstract PacketListener build();
    }

    /**
     * A builder for constructing global packet listeners.
     * <p>
     * Global listeners receive and handle all packets, regardless of their type.
     * This builder allows configuring handlers for outgoing and incoming packets,
     * as well as inheriting configuration options for priority and error-handling
     * from the {@link BaseBuilder}.
     * <p>
     * The {@code GlobalBuilder} provides methods for defining custom send and receive handlers
     * that will be invoked for every packet processed by the listener. The constructed listener
     * is designed to handle all packets globally, making it a versatile tool for comprehensive
     * packet monitoring or processing.
     */
    public static class GlobalBuilder extends BaseBuilder<GlobalBuilder> {
        private BiFunction<Connection, Packet<?>, PacketListener.PacketProcessingResult> sendHandler;
        private BiFunction<Connection, Packet<?>, PacketListener.PacketProcessingResult> receiveHandler;

        /**
         * Sets a handler for all outgoing packets.
         * The specified handler will be invoked whenever a packet is sent.
         *
         * @param handler the send handler, which processes the connection, the packet,
         *                and produces a {@link PacketListener.PacketProcessingResult}
         * @return this builder for method chaining
         */
        public GlobalBuilder onSend(BiFunction<Connection, Packet<?>, PacketListener.PacketProcessingResult> handler) {
            this.sendHandler = Objects.requireNonNull(handler, "handler cannot be null");
            return this;
        }

        /**
         * Sets a handler for all incoming packets.
         * The specified handler will be invoked whenever a packet is received.
         *
         * @param handler the receive handler, which processes the connection, the packet,
         *                and produces a {@link PacketListener.PacketProcessingResult}
         * @return this builder for method chaining
         */
        public GlobalBuilder onReceive(BiFunction<Connection, Packet<?>, PacketListener.PacketProcessingResult> handler) {
            this.receiveHandler = Objects.requireNonNull(handler, "handler cannot be null");
            return this;
        }

        @Override
        public PacketListener register() {
            PacketListener listener = build();
            PacketListenerRegistry.getInstance().registerGlobalListener(listener);
            return listener;
        }

        @Override
        public PacketListener build() {
            return new GlobalPacketListener(priority, sendHandler, receiveHandler, errorHandler);
        }
    }

    /**
     * SpecificBuilder is a specialized implementation of {@link BaseBuilder}, used to construct
     * packet listeners that handle specific packet types. It provides methods to configure
     * behavior for particular packet types by associating them with handlers.
     * <p>
     * This builder is designed for creating listeners that process a defined subset of packets.
     * During the registration phase, it ensures that the constructed listener is associated
     * with the system's registry for handling the configured packet types.
     */
    public static class SpecificBuilder extends BaseBuilder<SpecificBuilder> {
        private final Map<Class<? extends Packet<?>>, PacketTypeHandler> handlers = new HashMap<>();

        /**
         * Configures handling for a specific packet type.
         *
         * @param <T> the type of the packet being handled
         * @param packetType the class object representing the packet type; cannot be null
         * @return a builder for configuring handlers for the specified packet type
         * @throws NullPointerException if {@code packetType} is null
         */
        public <T extends Packet<?>> PacketTypeHandlerBuilder<T> forPacket(Class<T> packetType) {
            Objects.requireNonNull(packetType, "packetType cannot be null");
            return new PacketTypeHandlerBuilder<>(this, packetType);
        }

        @Override
        public PacketListener register() {
            PacketListener listener = build();
            PacketListenerRegistry registry = PacketListenerRegistry.getInstance();

            for (Class<? extends Packet<?>> packetType : handlers.keySet()) {
                registry.registerListener(packetType, listener);
            }

            return listener;
        }

        @Override
        public PacketListener build() {
            return new SpecificPacketListener(priority, new HashMap<>(handlers), errorHandler);
        }

        private void addHandler(Class<? extends Packet<?>> packetType, PacketTypeHandler handler) {
            handlers.put(packetType, handler);
        }
    }

    /**
     * A builder for configuring and managing handlers for a specific packet type.
     * This class is a part of the {@code PacketListenerBuilder} framework and is designed to
     * handle the configuration of send and receive behaviors for individual packet types.
     *
     * @param <T> the type of the packet this builder is managing
     */
    public static class PacketTypeHandlerBuilder<T extends Packet<?>> {
        private final SpecificBuilder parent;
        private final Class<T> packetType;
        private BiFunction<Connection, T, PacketListener.PacketProcessingResult> sendHandler;
        private BiFunction<Connection, T, PacketListener.PacketProcessingResult> receiveHandler;

        private PacketTypeHandlerBuilder(SpecificBuilder parent, Class<T> packetType) {
            this.parent = parent;
            this.packetType = packetType;
        }

        /**
         * Sets the send handler for this packet type.
         *
         * @param handler the send handler, a {@link BiFunction} that processes a {@link Connection}
         *                and the packet of type T, and returns a {@link PacketListener.PacketProcessingResult}
         * @return this packet type handler builder
         */
        public PacketTypeHandlerBuilder<T> onSend(BiFunction<Connection, T, PacketListener.PacketProcessingResult> handler) {
            this.sendHandler = Objects.requireNonNull(handler, "handler cannot be null");
            return this;
        }

        /**
         * Sets the send handler for this packet type using a single-parameter {@link Function}.
         * The provided handler processes a packet of type T and returns a {@link PacketListener.PacketResult}.
         *
         * @param handler the send handler, a {@link Function} that accepts a packet of type T
         *                and returns a {@link PacketListener.PacketProcessingResult}
         * @return this packet type handler builder
         */
        public PacketTypeHandlerBuilder<T> onSend(Function<T, PacketListener.PacketProcessingResult> handler) {
            Objects.requireNonNull(handler, "handler cannot be null");
            return onSend((conn, packet) -> handler.apply(packet));
        }

        /**
         * Sets the receive handler for this packet type.
         *
         * @param handler the receive handler, a {@link BiFunction} that processes a {@link Connection}
         *                and the packet of type T, and returns a {@link PacketListener.PacketProcessingResult}
         * @return this packet type handler builder
         */
        public PacketTypeHandlerBuilder<T> onReceive(BiFunction<Connection, T, PacketListener.PacketProcessingResult> handler) {
            this.receiveHandler = Objects.requireNonNull(handler, "handler cannot be null");
            return this;
        }

        /**
         * Sets the receive handler for this packet type using a single-parameter {@link Function}.
         * The provided handler processes a packet of type T and returns a {@link PacketListener.PacketProcessingResult}.
         *
         * @param handler the receive handler, a {@link Function} that accepts a packet of type T
         *                and returns a {@link PacketListener.PacketResult}
         * @return this packet type handler builder
         */
        public PacketTypeHandlerBuilder<T> onReceive(Function<T, PacketListener.PacketProcessingResult> handler) {
            Objects.requireNonNull(handler, "handler cannot be null");
            return onReceive((conn, packet) -> handler.apply(packet));
        }

        /**
         * Finalizes the configuration of the packet type handler within the context
         * of the current builder and registers it with the parent builder.
         * <p>
         * This method adds the configured send and receive handlers for the specified
         * packet type to the parent builder and concludes the current builder's operation.
         *
         * @return the parent builder to allow further configuration at a higher level
         */
        public SpecificBuilder done() {
            parent.addHandler(packetType, new PacketTypeHandler(sendHandler, receiveHandler));
            return parent;
        }

        /**
         * Configures a handler for a specific packet type within the current builder context.
         *
         * @param <U> the type of the packet being handled
         * @param packetType the class of the packet type to be handled; cannot be null
         * @return a builder for configuring handlers specific to the given packet type
         * @throws NullPointerException if {@code packetType} is null
         */
        public <U extends Packet<?>> PacketTypeHandlerBuilder<U> forPacket(Class<U> packetType) {
            return done().forPacket(packetType);
        }

        /**
         * Builds and registers the packet listener.
         * This automatically calls {@link #done()} first.
         *
         * @return the created and registered packet listener
         */
        public PacketListener register() {
            return done().register();
        }

        /**
         * Builds and returns a {@link PacketListener} instance.
         * This method finalizes the current configuration by invoking {@code done().build()}.
         *
         * @return the constructed {@link PacketListener} instance
         */
        public PacketListener build() {
            return done().build();
        }
    }

    /**
     * Represents a packet type handler used for processing send and receive operations
     * of packets in a connection. It encapsulates logic for handling the transmission
     * and reception of packets through specific handlers.
     * <p>
     * The handlers are represented as {@code BiFunction} objects which take a
     * {@link Connection} and a packet of a generic type as input and return a
     * {@link PacketListener.PacketProcessingResult}.
     * <p>
     * This is designed for use within packet listener systems to allow flexible handling
     * of different packet types and their respective send and receive behaviors.
     *
     * @param sendHandler    a {@code BiFunction} that processes the sending of packets
     * @param receiveHandler a {@code BiFunction} that processes the receiving of packets
     */
    private record PacketTypeHandler(BiFunction<Connection, ?, PacketListener.PacketProcessingResult> sendHandler,
                                     BiFunction<Connection, ?, PacketListener.PacketProcessingResult> receiveHandler) {}

    /**
     * A packet listener implementation for handling sending, receiving, and error-related packet events globally.
     * <p>
     * This class allows for custom handling of packets traversing through the system by leveraging
     * functional interfaces to define logic for sending, receiving, and error-handling scenarios.
     * The class also defines a priority level which determines the order of execution relative to
     * other listeners.
     * <p>
     * Components:
     * - Sending handler: Logic to process outgoing packets.
     * - Receiving handler: Logic to process incoming packets.
     * - Error handler: Logic to handle exceptions occurring during packet processing.
     * <p>
     * The listener provides a default pass-through behavior if no handlers are defined or an exception is raised.
     */
    private record GlobalPacketListener(ListenerPriority priority, BiFunction<Connection, Packet<?>, PacketProcessingResult> sendHandler,
                                        BiFunction<Connection, Packet<?>, PacketProcessingResult> receiveHandler,
                                        BiFunction<Connection, Exception, Void> errorHandler) implements PacketListener {

        @Override
        public PacketProcessingResult onPacketSend(Connection connection, Packet<?> packet) {
            if (sendHandler != null) {
                try {
                    return sendHandler.apply(connection, packet);
                } catch (Exception e) {
                    handleError(connection, e);
                }
            }
            return PacketProcessingResult.pass();
        }

        @Override
        public PacketProcessingResult onPacketReceive(Connection connection, Packet<?> packet) {
            if (receiveHandler != null) {
                try {
                    return receiveHandler.apply(connection, packet);
                } catch (Exception e) {
                    handleError(connection, e);
                }
            }
            return PacketProcessingResult.pass();
        }

        @Override
        public void onPacketError(Connection connection, Packet<?> packet, Exception exception) {
            handleError(connection, exception);
        }

        private void handleError(Connection connection, Exception exception) {
            if (errorHandler != null) {
                try {
                    errorHandler.apply(connection, exception);
                } catch (Exception ignored) {
                    // Prevent recursive error handling
                }
            }
        }
    }

    /**
     * A specific implementation of {@link PacketListener} that processes packets using configured handlers
     * and an optional error handler. It is designed to allow targeted processing of specific packet types
     * with defined behaviors for sending and receiving packets.
     * <p>
     * This class relies on a map of packet types to handlers, enabling custom logic to be executed
     * for each packet type during sending and receiving. If a handler is not present for a given packet type,
     * the default behavior is to pass processing without modification.
     * <p>
     * The listener operates with a specified {@link ListenerPriority}, controlling the order in which
     * it is invoked relative to other packet listeners.
     * <p>
     * Error handling is facilitated by an optional {@link BiFunction} that can define custom behavior
     * when exceptions occur during packet handling operations.
     */
    private record SpecificPacketListener(ListenerPriority priority, Map<Class<? extends Packet<?>>, PacketTypeHandler> handlers,
                                          BiFunction<Connection, Exception, Void> errorHandler) implements PacketListener {

        @Override
        public PacketProcessingResult onPacketSend(Connection connection, Packet<?> packet) {
            PacketTypeHandler handler = handlers.get(packet.getClass());
            if (handler != null && handler.sendHandler() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    BiFunction<Connection, Packet<?>, PacketProcessingResult> castedHandler =
                        (BiFunction<Connection, Packet<?>, PacketProcessingResult>) handler.sendHandler();
                    return castedHandler.apply(connection, packet);
                } catch (Exception e) {
                    handleError(connection, e);
                }
            }
            return PacketProcessingResult.pass();
        }

        @Override
        public PacketProcessingResult onPacketReceive(Connection connection, Packet<?> packet) {
            PacketTypeHandler handler = handlers.get(packet.getClass());
            if (handler != null && handler.receiveHandler() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    BiFunction<Connection, Packet<?>, PacketProcessingResult> castedHandler =
                        (BiFunction<Connection, Packet<?>, PacketProcessingResult>) handler.receiveHandler();
                    return castedHandler.apply(connection, packet);
                } catch (Exception e) {
                    handleError(connection, e);
                }
            }
            return PacketProcessingResult.pass();
        }

        @Override
        public void onPacketError(Connection connection, Packet<?> packet, Exception exception) {
            handleError(connection, exception);
        }

        private void handleError(Connection connection, Exception exception) {
            if (errorHandler != null) {
                try {
                    errorHandler.apply(connection, exception);
                } catch (Exception ignored) {
                    // Prevent recursive error handling
                }
            }
        }
    }
}
