package stylus.fx;


import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;

/**
 * Base interface for Control molder methods.
 * @param <C> The specific Control class
 * @param <Self> The concrete molder
 */
@SuppressWarnings({"unchecked", "unused"})
public interface ControlMolderBase<C extends Control, Self>
    extends RegionMolderBase<C, Self>
{
    /**
     * Sets the object's tooltip.
     * @param value The tooltip
     * @return The molder
     */
    default Self tooltip(Tooltip value) {
        object().setTooltip(value);
        return (Self)this;
    }

    /**
     * Sets the object's tooltip to a new Tooltip with the given text.
     * @param value The text
     * @return The molder
     */
    default Self tooltipText(String value) {
        object().setTooltip(new Tooltip(value));
        return (Self)this;
    }
}
