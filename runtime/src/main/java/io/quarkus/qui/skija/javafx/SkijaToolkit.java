package io.quarkus.qui.skija.javafx;

import java.io.File;
import java.io.InputStream;
import java.security.AccessControlContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.sun.glass.ui.CommonDialogs;
import com.sun.glass.ui.GlassRobot;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.perf.PerformanceTracker;
import com.sun.javafx.runtime.async.AsyncOperation;
import com.sun.javafx.runtime.async.AsyncOperationListener;
import com.sun.javafx.scene.text.TextLayoutFactory;
import com.sun.javafx.tk.AppletWindow;
import com.sun.javafx.tk.FileChooserType;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.ImageLoader;
import com.sun.javafx.tk.PlatformImage;
import com.sun.javafx.tk.RenderJob;
import com.sun.javafx.tk.ScreenConfigurationAccessor;
import com.sun.javafx.tk.TKClipboard;
import com.sun.javafx.tk.TKDragGestureListener;
import com.sun.javafx.tk.TKDragSourceListener;
import com.sun.javafx.tk.TKDropTargetListener;
import com.sun.javafx.tk.TKScene;
import com.sun.javafx.tk.TKScreenConfigurationListener;
import com.sun.javafx.tk.TKStage;
import com.sun.javafx.tk.TKSystemMenu;
import com.sun.javafx.tk.Toolkit;
import com.sun.scenario.DelayedRunnable;
import com.sun.scenario.animation.AbstractMasterTimer;
import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.Filterable;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class SkijaToolkit extends Toolkit {

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public boolean canStartNestedEventLoop() {
        return false;
    }

    @Override
    public Object enterNestedEventLoop(Object o) {
        return null;
    }

    @Override
    public void exitNestedEventLoop(Object o, Object o1) {

    }

    @Override
    public void exitAllNestedEventLoops() {

    }

    @Override
    public boolean isNestedLoopRunning() {
        return false;
    }

    @Override
    public TKStage createTKStage(Window window, boolean b, StageStyle stageStyle, boolean b1, Modality modality, TKStage tkStage, boolean b2, AccessControlContext accessControlContext) {
        return null;
    }

    @Override
    public TKStage createTKPopupStage(Window window, StageStyle stageStyle, TKStage tkStage, AccessControlContext accessControlContext) {
        return null;
    }

    @Override
    public TKStage createTKEmbeddedStage(HostInterface hostInterface, AccessControlContext accessControlContext) {
        return null;
    }

    @Override
    public AppletWindow createAppletWindow(long l, String s) {
        return null;
    }

    @Override
    public void closeAppletWindow() {

    }

    @Override
    public void requestNextPulse() {

    }

    @Override
    public Future addRenderJob(RenderJob renderJob) {
        return null;
    }

    @Override
    public ImageLoader loadImage(String s, double v, double v1, boolean b, boolean b1) {
        return null;
    }

    @Override
    public ImageLoader loadImage(InputStream inputStream, double v, double v1, boolean b, boolean b1) {
        return null;
    }

    @Override
    public AsyncOperation loadImageAsync(AsyncOperationListener<? extends ImageLoader> asyncOperationListener, String s, double v, double v1, boolean b, boolean b1) {
        return null;
    }

    @Override
    public ImageLoader loadPlatformImage(Object o) {
        return null;
    }

    @Override
    public PlatformImage createPlatformImage(int i, int i1) {
        return null;
    }

    @Override
    public void startup(Runnable runnable) {

    }

    @Override
    public void defer(Runnable runnable) {

    }

    @Override
    public Map<Object, Object> getContextMap() {
        return null;
    }

    @Override
    public int getRefreshRate() {
        return 0;
    }

    @Override
    public void setAnimationRunnable(DelayedRunnable delayedRunnable) {

    }

    @Override
    public PerformanceTracker getPerformanceTracker() {
        return null;
    }

    @Override
    public PerformanceTracker createPerformanceTracker() {
        return null;
    }

    @Override
    public void waitFor(Task task) {

    }

    @Override
    protected Object createColorPaint(Color color) {
        return null;
    }

    @Override
    protected Object createLinearGradientPaint(LinearGradient linearGradient) {
        return null;
    }

    @Override
    protected Object createRadialGradientPaint(RadialGradient radialGradient) {
        return null;
    }

    @Override
    protected Object createImagePatternPaint(ImagePattern imagePattern) {
        return null;
    }

    @Override
    public void accumulateStrokeBounds(Shape shape, float[] floats, StrokeType strokeType, double v, StrokeLineCap strokeLineCap, StrokeLineJoin strokeLineJoin, float v1, BaseTransform baseTransform) {

    }

    @Override
    public boolean strokeContains(Shape shape, double v, double v1, StrokeType strokeType, double v2, StrokeLineCap strokeLineCap, StrokeLineJoin strokeLineJoin, float v3) {
        return false;
    }

    @Override
    public Shape createStrokedShape(Shape shape, StrokeType strokeType, double v, StrokeLineCap strokeLineCap, StrokeLineJoin strokeLineJoin, float v1, float[] floats, float v2) {
        return null;
    }

    @Override
    public int getKeyCodeForChar(String s) {
        return 0;
    }

    @Override
    public Dimension2D getBestCursorSize(int i, int i1) {
        return null;
    }

    @Override
    public int getMaximumCursorColors() {
        return 0;
    }

    @Override
    public PathElement[] convertShapeToFXPath(Object o) {
        return new PathElement[0];
    }

    @Override
    public Filterable toFilterable(Image image) {
        return null;
    }

    @Override
    public FilterContext getFilterContext(Object o) {
        return null;
    }

    @Override
    public boolean isForwardTraversalKey(KeyEvent keyEvent) {
        return false;
    }

    @Override
    public boolean isBackwardTraversalKey(KeyEvent keyEvent) {
        return false;
    }

    @Override
    public AbstractMasterTimer getMasterTimer() {
        return null;
    }

    @Override
    public FontLoader getFontLoader() {
        return null;
    }

    @Override
    public TextLayoutFactory getTextLayoutFactory() {
        return null;
    }

    @Override
    public Object createSVGPathObject(SVGPath svgPath) {
        return null;
    }

    @Override
    public Path2D createSVGPath2D(SVGPath svgPath) {
        return null;
    }

    @Override
    public boolean imageContains(Object o, float v, float v1) {
        return false;
    }

    @Override
    public TKClipboard getSystemClipboard() {
        return null;
    }

    @Override
    public TKSystemMenu getSystemMenu() {
        return null;
    }

    @Override
    public TKClipboard getNamedClipboard(String s) {
        return null;
    }

    @Override
    public ScreenConfigurationAccessor setScreenConfigurationListener(TKScreenConfigurationListener tkScreenConfigurationListener) {
        return null;
    }

    @Override
    public Object getPrimaryScreen() {
        return null;
    }

    @Override
    public List<?> getScreens() {
        return null;
    }

    @Override
    public ScreenConfigurationAccessor getScreenConfigurationAccessor() {
        return null;
    }

    @Override
    public void registerDragGestureListener(TKScene tkScene, Set<TransferMode> set, TKDragGestureListener tkDragGestureListener) {

    }

    @Override
    public void startDrag(TKScene tkScene, Set<TransferMode> set, TKDragSourceListener tkDragSourceListener, Dragboard dragboard) {

    }

    @Override
    public void enableDrop(TKScene tkScene, TKDropTargetListener tkDropTargetListener) {

    }

    @Override
    public void installInputMethodRequests(TKScene tkScene, InputMethodRequests inputMethodRequests) {

    }

    @Override
    public Object renderToImage(ImageRenderingContext imageRenderingContext) {
        return null;
    }

    @Override
    public CommonDialogs.FileChooserResult showFileChooser(TKStage tkStage, String s, File file, String s1, FileChooserType fileChooserType, List<FileChooser.ExtensionFilter> list, FileChooser.ExtensionFilter extensionFilter) {
        return null;
    }

    @Override
    public File showDirectoryChooser(TKStage tkStage, String s, File file) {
        return null;
    }

    @Override
    public long getMultiClickTime() {
        return 0;
    }

    @Override
    public int getMultiClickMaxX() {
        return 0;
    }

    @Override
    public int getMultiClickMaxY() {
        return 0;
    }

    @Override
    public GlassRobot createRobot() {
        return null;
    }
}
