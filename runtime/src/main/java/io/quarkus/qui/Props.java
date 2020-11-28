package io.quarkus.qui;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Props describe how a view
 * will be drawn. The only methods
 * allowed in the extensions of this
 * interface are those that sets a
 * prop and returns itself. A fluent
 * implementation is generated automatically.
 * To get the value of a field, use
 * View.get(props::field)
 */
public interface Props<T extends Props<T>> {

    /**
     * Implementation detail. Do not use.
     *
     * Reset this props so all properties (except current view)
     * are null
     *
     * @return self, with properties null
     */
    T _reset();

    /**
     * Implementation detail. Do not use.
     *
     * Return the value of the prop that
     * was last set with null.
     *
     * @return The value of the prop that
     * was last set with __GET_VALUE__.
     */
    <V> V _get();

    /**
     * Implementation detail. Do not use.
     *
     * Sets the view associated with
     * this props
     *
     * @param view The view to be associated
     *             with this props
     * @param <V> The type of view
     *
     * @return Self
     */
    <V extends View<T>> T _setView(V view);

    /**
     * Implementation detail. Do not use.
     *
     * Return the view associated with
     * this props
     *
     * @return The view associated with this
     * props
     */
    <V extends View<T>> V _getView();

    /**
     * Implementation detail. Do not use.
     *
     * Sets the window associated with this props
     *
     * @param view The view to be associated
     *             with this props
     *
     * @return Self
     */
    T _setWindow(Window view);

    /**
     * Implementation detail. Do not use.
     *
     * Get the window associated with this props
     *
     * @return Self
     */
    Window _getWindow();

    DomLocation _getDomLocation();

    T _setDomLocation(DomLocation domLocation);

    /**
     * Draws the object.
     */
    void draw(QuiCanvas canvas);
}
