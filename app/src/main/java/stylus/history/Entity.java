package stylus.history;

/**
 * An entity in a history.
 * @param id The entity's unique ID
 * @param name An entity name for display
 * @param type The entity's type, for grouping.
 * @param prime If true, the entity is of primary importance.
 */
public record Entity(
    String id,
    String name,
    String type,
    boolean prime
) {
}
