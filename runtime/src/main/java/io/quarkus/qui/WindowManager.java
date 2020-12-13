package io.quarkus.qui;

import io.quarkus.qui.skija.SkijaWindow;

/**
 * Handles the creation, positioning, and
 * content of windows.
 */
public interface WindowManager {

    /**
     * Create a window
     *
     * @param title The title for the window. Not null.
     * @param defaultView The default view to display
     *                    for this window. Not null.
     * @return A new window that will display default
     * View, UNLESS the application was restarted in dev
     * mode, in which case it will show the previous view
     * the window was showing.
     */
    Window createWindow(String title);

    void waitUntilAllWindowsAreClosed();

    /**
     * Dev mode implementation detail. Do not use.
     *
     * Called when a file changed
     */
    void _onFileChange();
}
