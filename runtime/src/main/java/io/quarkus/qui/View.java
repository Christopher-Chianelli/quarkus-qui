package io.quarkus.qui;

import java.util.function.Function;

/**
 * A View is something that can be drawn
 * in a window. It should have a no-args
 * constructor; it properties will be
 * supplied via the props parameter to render.
 *
 * @param <T> The props of the view,
 *           which can be set by its
 *           parent
 */
public interface View<T extends Props> {

    /**
     * Renders a view. This does not perform
     * the actual drawing, but supplies the
     * information required to draw the view.
     * You can use fields in this method; updating
     * a field (ex: adding an element to a list
     * or incrementing a number) will trigger a
     * redraw.
     *
     * @param props The props this view accept
     *              from its parent
     * @return The props that describe how this
     *         component should be drawn
     */
    Props<?> render(T props);

    /**
     * Returns the value of a prop. DO NOT OVERRIDE
     *
     * @param propSetter The setter of the prop to get
     * @param <V> The type of the prop
     * @return The value of the prop
     */
    @SuppressWarnings("unchecked")
    default <V> V get(Function<V, T> propSetter) {
        return (V)(propSetter.apply(null)._get());
    }

    /**
     * Returns the props object. Do not override
     * any value of the props. DO NOT OVERRIDE
     */
    @SuppressWarnings("unchecked")
    default T getProps() {
        return (T) ViewManager.INSTANCE.getProps(this);
    }

    /**
     * Constructs an instance of a view and return its
     * props. DO NOT OVERRIDE
     * @param view The class of the view to construct
     * @param <P> The type of props for that view
     *
     * @return The props for the constructed view
     */
    @SuppressWarnings("unchecked")
    default <P extends Props> P show(Class<? extends View<P>> view) {
        return (P) ViewManager.INSTANCE.createView(this, view);
    }
}
