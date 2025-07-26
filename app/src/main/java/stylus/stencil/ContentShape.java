package stylus.stencil;

import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;

/**
 * A ContentShape is a shape that sizes itself to fit its content.  Each
 * shape must define {@code getRealSize()} which computes its size without
 * taking the {@code minSize} property into account.  Then,
 *
 * <ul>
 * <li>{@code getSize()} computes its full size: the real size as modified by
 *     its {@code minSize}.</li>
 * <li>{@code getRealBounds()} computes its bounds on the canvas given its
 *     {@code at}, {@code tack}, and real size.</li>
 * <li>{@code getBounds()} computes its bounds on the canvas given its
 *     {@code at}, {@code tack}, and full size.</li>
 * </ul>
 *
 * <p>
 * The shape may chose to expand to fill its full bounds, or it may choose to
 * fill its real bounds, in which case it will be positioned within the full
 * bounds according to its {@code tack}.
 * </p>
 * @param <Self> the concrete shape class
 */
@SuppressWarnings({"unchecked", "unused"})
public abstract class ContentShape<Self extends ContentShape<Self>>
    extends StyleBase<Self>
    implements Drawable
{
    //---------------------------------------------------------------------
    // Instance Variables

    // The shape's origin point (x,y)
    protected double x = 0.0;
    protected double y = 0.0;

    // The location of the shape's origin point relative to the bounding
    // box, i.e, where the tack is.
    protected Tack tack = Tack.NORTHWEST;

    // The minimum size of the shape
    protected double minWidth = 1.0;
    protected double minHeight = 1.0;

    //---------------------------------------------------------------------
    // Constructor

    public ContentShape() {
        // Nothing to do
    }

    //---------------------------------------------------------------------
    // ContentShape API

    /**
     * Gets the actual size of the shape, disregarding the minSize property.
     * @return the size
     */
    public abstract Dimension2D getRealSize();

    /**
     * Gets the bounds of the shape given its origin, tack, and real size.
     * @return The bounds
     */
    public final Bounds getRealBounds() {
        var size = getRealSize();
        return Pen.tack2bounds(tack, x, y, size.getWidth(), size.getHeight());
    }

    /**
     * Gets the full size of the shape, given the minSize property.
     * @return the size
     */
    public final Dimension2D getSize() {
        var real = getRealSize();

        return new Dimension2D(
            Math.max(minWidth, real.getWidth()),
            Math.max(minHeight, real.getHeight())
        );
    }

    /**
     * Gets the bounds of the shape given its origin, tack, and full size.
     * @return The bounds
     */
    public final Bounds getBounds() {
        var size = getSize();
        return Pen.tack2bounds(tack, x, y, size.getWidth(), size.getHeight());
    }

    //---------------------------------------------------------------------
    // Getters

    /**
     * Gets the shape's origin point
     * @return The point
     */
    public Point2D getAt() {
        return new Point2D(x, y);
    }

    public Dimension2D getMinSize() {
        return new Dimension2D(minWidth, minHeight);
    }

    /**
     * Gets the shape's tack position: the location of the origin relative
     * to the bounding box.
     * @return The tack
     */
    public Tack getTack() {
        return tack;
    }

    //---------------------------------------------------------------------
    // DSL

    /**
     * Sets the shape's origin point
     * @param point The point
     * @return The shape
     */
    public Self at(Point2D point) {
        return at(point.getX(), point.getY());
    }

    /**
     * Sets the shape's origin point
     * @param x The X coordinate
     * @param y The Y coordinate
     * @return The shape
     */
    public Self at(double x, double y) {
        this.x = x;
        this.y = y;
        return (Self)this;
    }

    /**
     * Sets the minimum size of the shape.
     * @param size The size
     * @return The shape
     */
    public Self minSize(Dimension2D size) {
        return minSize(size.getWidth(), size.getHeight());
    }

    /**
     * Sets the minimum size of the shape
     * @param minWidth The minimum width
     * @param minHeight The minimum height
     * @return The shape
     */
    public Self minSize(double minWidth, double minHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        return (Self)this;
    }

    /**
     * Sets the "tack position": the location of the origin point on the
     * bounding box. Defaults to Tack.NORTHWEST.
     * @param tack The tack
     * @return The shape
     */
    public Self tack(Tack tack) {
        this.tack = tack;
        return (Self)this;
    }
}
