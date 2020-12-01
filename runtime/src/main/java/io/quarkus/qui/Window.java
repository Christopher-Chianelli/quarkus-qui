package io.quarkus.qui;

public interface Window {
    Props<?> view();
    <T extends Props<T>> T defaultView(Class<? extends View<T>> viewClass);
    Window draw(Props<?> props);

    void waitUntilClosed();

    default Props<?> render() {
        Props props = view();
        Props<?> render = props._getView().render(props);
        draw(render);
        ViewManager.INSTANCE.endRender(this);
        return render;
    }
}
