package net.gommehd.cheetah.network.packet;

import net.minecraft.network.protocol.Packet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * The {@code PacketListenerRegistry} class serves as a registry for managing listeners associated
 * with various types of packets. This class supports both global and type-specific listeners, as well
 * as listeners registered conditionally based on packet type predicates.
 * <p>
 * A singleton instance is provided to allow centralized management of packet listener registrations,
 * retrievals, and removals across an application.
 */
public class PacketListenerRegistry {
    private static final PacketListenerRegistry INSTANCE = new PacketListenerRegistry();

    private final Map<Class<? extends Packet<?>>, List<PacketListener>> listeners = new ConcurrentHashMap<>();
    private final List<PacketListener> globalListeners = new CopyOnWriteArrayList<>();
    private final Map<PacketListener, Set<Class<? extends Packet<?>>>> listenerToTypes = new ConcurrentHashMap<>();
    private final List<ConditionalListenerEntry> conditionalListeners = new CopyOnWriteArrayList<>();
    private final Map<Class<? extends Packet<?>>, List<PacketListener>> conditionalListenerCache = new ConcurrentHashMap<>();

    /**
     * Returns the singleton instance of the PacketListenerRegistry class.
     *
     * @return the single instance of PacketListenerRegistry
     */
    public static PacketListenerRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers a listener to handle packets of the specified type.
     * <p>
     * The listener will be invoked whenever a packet of the given type is processed.
     * Listeners are stored and managed internally in the registry. The order of invocation
     * is determined by the listeners' priority.
     * <p>
     * The listener is also tracked internally for easier removal when unregistered.
     *
     * @param <T> the type of the packet to associate the listener with, must extend {@code Packet<?>}
     * @param packetType the class object representing the type of packet the listener should handle; must not be null
     * @param listener the listener to register; must not be null
     * @throws NullPointerException if {@code packetType} or {@code listener} is null
     */
    public <T extends Packet<?>> void registerListener(Class<T> packetType, PacketListener listener) {
        Objects.requireNonNull(packetType, "packetType cannot be null");
        Objects.requireNonNull(listener, "listener cannot be null");

        listeners.computeIfAbsent(packetType, k -> new CopyOnWriteArrayList<>())
            .add(listener);
        sortListenersByPriority(listeners.get(packetType));

        // Track listener types for easier unregistering
        listenerToTypes.computeIfAbsent(listener, k -> new HashSet<>()).add(packetType);
    }

    /**
     * Registers a global listener that will be invoked for every packet type.
     * <p>
     * Global listeners are notified regardless of the specific type of packet
     * being processed. The provided listener is added to the internal list of
     * global listeners and sorted by priority.
     *
     * @param listener the {@link PacketListener} to register, must not be null
     * @throws NullPointerException if the {@code listener} is null
     */
    public void registerGlobalListener(PacketListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");

        globalListeners.add(listener);
        sortListenersByPriority(globalListeners);
    }

    /**
     * Registers a listener for multiple packet types.
     * <p>
     * The specified listener will be notified for each packet type listed in
     * the provided array of packet types. The listener must not be null, and
     * the array of packet types must not contain null elements.
     *
     * @param listener the packet listener to register, must not be null
     * @param packetTypes the packet types that the listener should handle
     *                    must not be null and must not contain null elements
     * @throws NullPointerException if listener or packetTypes is null
     */
    @SafeVarargs
    public final void registerListener(PacketListener listener, Class<? extends Packet<?>>... packetTypes) {
        Objects.requireNonNull(listener, "listener cannot be null");
        Objects.requireNonNull(packetTypes, "packetTypes cannot be null");

        for (Class<? extends Packet<?>> packetType : packetTypes) {
            registerListener(packetType, listener);
        }
    }

    /**
     * Registers a conditional packet listener based on the provided condition.
     * <p>
     * The listener will only be invoked for packets that match the predicate
     * specified by the condition parameter. Conditional listeners are sorted by
     * priority, with higher priority listeners being invoked first.
     *
     * @param condition the condition to be checked against packet types; must not be null
     * @param listener the packet listener to register; must not be null
     * @throws NullPointerException if the condition or listener is null
     */
    public void registerConditionalListener(Predicate<Class<? extends Packet<?>>> condition, PacketListener listener) {
        Objects.requireNonNull(condition, "condition cannot be null");
        Objects.requireNonNull(listener, "listener cannot be null");

        conditionalListeners.add(new ConditionalListenerEntry(condition, listener));

        // Clear cache since we have a new conditional listener
        conditionalListenerCache.clear();

        // Sort by priority
        conditionalListeners.sort((a, b) ->
            Integer.compare(b.listener().priority().getPriority(), a.listener().priority().getPriority()));
    }

    /**
     * Unregisters the specified {@link PacketListener} from all contexts in which it
     * was previously registered. This includes global listeners, conditional listeners,
     * and listeners for specific packet types.
     *
     * @param listener the {@link PacketListener} to unregister; must not be null
     * @throws NullPointerException if the provided listener is null
     */
    public void unregisterListener(PacketListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");

        // Remove from global listeners
        globalListeners.remove(listener);

        // Remove from conditional listeners
        conditionalListeners.removeIf(entry -> entry.listener().equals(listener));
        conditionalListenerCache.clear(); // Clear cache since conditional listeners changed

        // Remove from specific packet type listeners
        Set<Class<? extends Packet<?>>> types = listenerToTypes.remove(listener);
        if (types != null) {
            for (Class<? extends Packet<?>> type : types) {
                List<PacketListener> typeListeners = listeners.get(type);
                if (typeListeners != null) {
                    typeListeners.remove(listener);
                    if (typeListeners.isEmpty()) {
                        listeners.remove(type);
                    }
                }
            }
        }
    }

    /**
     * Unregisters the specified listener for the given packet type.
     * If the listener is no longer associated with any packet type,
     * it is removed entirely from the mapping.
     *
     * @param packetType the class type of the packet for which the listener is to be unregistered
     * @param listener the listener instance to be unregistered
     * @throws NullPointerException if the packetType or listener is null
     */
    public void unregisterListener(Class<? extends Packet<?>> packetType, PacketListener listener) {
        Objects.requireNonNull(packetType, "packetType cannot be null");
        Objects.requireNonNull(listener, "listener cannot be null");

        List<PacketListener> typeListeners = listeners.get(packetType);
        if (typeListeners != null) {
            typeListeners.remove(listener);
            if (typeListeners.isEmpty()) {
                listeners.remove(packetType);
            }
        }

        Set<Class<? extends Packet<?>>> types = listenerToTypes.get(listener);
        if (types != null) {
            types.remove(packetType);
            if (types.isEmpty()) {
                listenerToTypes.remove(listener);
            }
        }
    }

    /**
     * Retrieves a list of all packet listeners that are associated with the specified packet type.
     * This method includes global listeners, type-specific listeners, and conditional listeners.
     * It may also consider listeners registered for the superclass hierarchy of the given packet type.
     * The resulting list is sorted by listener priority in descending order.
     *
     * @param packetType the class of the packet type for which listeners are being retrieved;
     *                   must extend the {@code Packet} class and cannot be null.
     * @return a sorted list of {@code PacketListener} objects that are associated with the
     *         specified packet type, including all relevant types of listeners.
     * @throws NullPointerException if the {@code packetType} is null.
     */
    @SuppressWarnings("unchecked")
    public List<PacketListener> getListeners(Class<?> packetType) {
        Objects.requireNonNull(packetType, "packetType cannot be null");

        List<PacketListener> result = new ArrayList<>(globalListeners);

        // Cast to the expected type - this is safe because we only register Packet subclasses
        Class<? extends Packet<?>> safePacketType = (Class<? extends Packet<?>>) packetType;

        // Add type-specific listeners
        List<PacketListener> specificListeners = listeners.get(safePacketType);
        if (specificListeners != null) {
            result.addAll(specificListeners);
        }

        // Add conditional listeners (with caching)
        List<PacketListener> conditionalListeners = getConditionalListenersForType(safePacketType);
        result.addAll(conditionalListeners);

        // Check inheritance hierarchy
        Class<?> currentClass = packetType.getSuperclass();
        while (currentClass != null && Packet.class.isAssignableFrom(currentClass)) {
            @SuppressWarnings("unchecked")
            List<PacketListener> parentListeners = listeners.get((Class<? extends Packet<?>>) currentClass);
            if (parentListeners != null) {
                result.addAll(parentListeners);
            }
            currentClass = currentClass.getSuperclass();
        }

        // Sort by priority
        result.sort((a, b) -> Integer.compare(b.priority().getPriority(), a.priority().getPriority()));
        return result;
    }

    /**
     * Retrieves a list of PacketListener objects that match the specified packet type
     * by evaluating their conditions. If the result is already cached, it retrieves
     * it from the cache; otherwise, it evaluates the conditions, caches the result,
     * and then returns it.
     *
     * @param packetType the class type of the packet for which the matching listeners
     *                   are to be retrieved
     * @return a list of PacketListener instances that are conditionally registered
     *         and match the specified packet type
     */
    private List<PacketListener> getConditionalListenersForType(Class<? extends Packet<?>> packetType) {
        // Check cache first
        List<PacketListener> cached = conditionalListenerCache.get(packetType);
        if (cached != null) {
            return cached;
        }

        // Evaluate conditions and build result
        List<PacketListener> result = new ArrayList<>();
        for (ConditionalListenerEntry entry : conditionalListeners) {
            if (entry.condition().test(packetType)) {
                result.add(entry.listener());
            }
        }

        // Cache the result
        conditionalListenerCache.put(packetType, result);
        return result;
    }

    /**
     * Retrieves a set of all registered packet listeners.
     * <p>
     * This includes:
     * - Global listeners that are invoked for every packet type.
     * - Type-specific listeners registered for particular packet types.
     * - Conditional listeners that match specific conditions.
     * <p>
     * The returned set includes all currently registered listeners, regardless of how they were registered.
     *
     * @return a set of all registered listeners, never null but may be empty.
     */
    public Set<PacketListener> getAllListeners() {
        Set<PacketListener> result = new HashSet<>(globalListeners);
        listeners.values().forEach(result::addAll);

        // Add conditional listeners
        for (ConditionalListenerEntry entry : conditionalListeners) {
            result.add(entry.listener());
        }

        return result;
    }

    /**
     * Retrieves the set of all registered packet types in the registry.
     *
     * @return a set of packet types (classes) for which listeners are registered, never null but may be empty
     */
    public Set<Class<? extends Packet<?>>> getRegisteredPacketTypes() {
        return new HashSet<>(listeners.keySet());
    }

    /**
     * Checks if the specified listener is currently registered in the registry.
     * <p>
     * A listener can be registered as a global listener, associated with specific
     * packet types, or as a conditional listener based on predicates.
     *
     * @param listener the packet listener to check for registration; must not be null
     * @return true if the listener is registered, false otherwise
     * @throws NullPointerException if the listener is null
     */
    public boolean isListenerRegistered(PacketListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");

        if (globalListeners.contains(listener) || listenerToTypes.containsKey(listener)) {
            return true;
        }

        // Check conditional listeners
        return conditionalListeners.stream()
            .anyMatch(entry -> entry.listener().equals(listener));
    }

    /**
     * Clears all registered packet listeners and associated data.
     * <p>
     * This method effectively resets the state of the registry, removing:
     * - All type-specific listeners.
     * - All global listeners.
     * - All listeners registered via predicates (conditional listeners).
     * - Cached data related to conditional listeners.
     * <p>
     * After calling this method, no listeners will remain registered in the registry.
     */
    public void clear() {
        listeners.clear();
        globalListeners.clear();
        listenerToTypes.clear();
        conditionalListeners.clear();
        conditionalListenerCache.clear();
    }

    /**
     * Returns statistics about the current state of the registry, including conditional listeners.
     *
     * @return registry statistics, never null
     */
    public RegistryStats getStats() {
        int totalSpecificListeners = listeners.values().stream().mapToInt(List::size).sum();
        return new RegistryStats(
            globalListeners.size(),
            totalSpecificListeners,
            listeners.size(),
            getAllListeners().size(),
            conditionalListeners.size()
        );
    }

    /**
     * Sorts the given list of PacketListener objects in descending order based on their priority.
     *
     * @param listeners the list of PacketListener objects to be sorted
     */
    private void sortListenersByPriority(List<PacketListener> listeners) {
        listeners.sort((a, b) -> Integer.compare(b.priority().getPriority(), a.priority().getPriority()));
    }

    /**
     * A record representing statistics about the current state of a packet listener registry.
     * Contains various metrics related to registered listeners and packet types.
     *
     * @param globalListeners      the number of global listeners registered in the registry.
     * @param specificListeners    the number of type-specific listeners registered.
     * @param registeredPacketTypes the number of unique packet types that have listeners.
     * @param uniqueListeners       the number of unique listeners (shared across global, specific, and conditional).
     * @param conditionalListeners  the number of conditional listeners registered.
     */
    public record RegistryStats(int globalListeners, int specificListeners, int registeredPacketTypes,
                                    int uniqueListeners, int conditionalListeners) {}

    /**
     * Represents an entry in the {@code PacketListenerRegistry} that associates a
     * conditional predicate with a specific {@code PacketListener}.
     * <p>
     * This record is used for registering conditional listeners, where a listener will only
     * handle packets of types that satisfy the given condition.
     * <p>
     * The condition is specified as a {@code Predicate} applied to the {@code Class} type of
     * the packet, allowing fine-grained control over which packets the listener should handle.
     * <p>
     * It is an immutable data structure designed to store the pairing of a condition and
     * its corresponding listener for efficient processing.
     * <p>
     * Fields:
     * - {@code condition}: A {@code Predicate} that tests the class type of packets.
     * - {@code listener}: A {@code PacketListener} that will handle packets matching the condition.
     */
    private record ConditionalListenerEntry(
        Predicate<Class<? extends Packet<?>>> condition,
        PacketListener listener
    ) {}
}
