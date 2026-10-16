package org.cyclops.integratedterminals.client.gui.container.component;

/**
 * The pan and zoom state of a graph inside a viewport.
 *
 * The camera stores the viewport-relative position of the graph origin,
 * so the view stays steady when the graph around the origin changes.
 *
 * This class is independent of Minecraft, so it can be unit-tested and ported easily.
 *
 * @author rubensworks
 */
public class CraftingPlanGraphCamera {

    public static final float DEFAULT_ZOOM = 1F;
    public static final float INITIAL_MAX_ZOOM = 1.5F;
    public static final float MAX_ZOOM = 3F;
    /**
     * Graphs can always be zoomed out to this level, and further if that is needed to see them as a whole.
     */
    public static final float MIN_ZOOM = 0.5F;
    /**
     * Below this level, nodes are too small to recognize, even for the largest graphs.
     */
    public static final float MIN_ZOOM_LARGE_GRAPH = 0.25F;
    /**
     * The factor by which the zoom changes per step.
     */
    public static final float ZOOM_STEP = 1.2F;
    /**
     * Zoom levels this close to the default level are snapped to it.
     */
    public static final float DEFAULT_ZOOM_SNAP = 0.05F;
    public static final int MARGIN = 8;
    private static final float EPSILON = 0.001F;

    private double originX;
    private double originY;
    private float zoom = DEFAULT_ZOOM;
    private float minZoom = MIN_ZOOM;

    public double getOriginX() {
        return originX;
    }

    public double getOriginY() {
        return originY;
    }

    public float getZoom() {
        return zoom;
    }

    public float getMinZoom() {
        return minZoom;
    }

    public boolean canZoomIn() {
        return zoom < MAX_ZOOM - EPSILON;
    }

    public boolean canZoomOut() {
        return zoom > minZoom + EPSILON;
    }

    /**
     * Allow zooming out until the whole graph fits.
     * @param bounds The graph bounds.
     * @param viewWidth The viewport width.
     * @param viewHeight The viewport height.
     */
    public void updateMinZoom(CraftingPlanGraphLayout.Bounds bounds, int viewWidth, int viewHeight) {
        this.minZoom = Math.max(MIN_ZOOM_LARGE_GRAPH, Math.min(MIN_ZOOM, getFitZoom(bounds, viewWidth, viewHeight)));
    }

    public double toGraphX(double viewX) {
        return (viewX - originX) / getZoom();
    }

    public double toGraphY(double viewY) {
        return (viewY - originY) / getZoom();
    }

    public double toViewX(double graphX) {
        return originX + graphX * getZoom();
    }

    public double toViewY(double graphY) {
        return originY + graphY * getZoom();
    }

    public void pan(double dx, double dy) {
        this.originX += dx;
        this.originY += dy;
    }

    /**
     * Change the zoom level while keeping the graph point under the given view position in place.
     * Zooming past or close to the default level stops at it, as that is where graphs look sharpest.
     * @param viewX The anchor X position in the viewport.
     * @param viewY The anchor Y position in the viewport.
     * @param direction Positive to zoom in, negative to zoom out.
     * @return If the zoom level changed.
     */
    public boolean zoomAt(double viewX, double viewY, int direction) {
        float newZoom = zoom * (float) Math.pow(ZOOM_STEP, Integer.signum(direction));
        if ((zoom < DEFAULT_ZOOM - EPSILON && newZoom > DEFAULT_ZOOM) || (zoom > DEFAULT_ZOOM + EPSILON && newZoom < DEFAULT_ZOOM)
                || Math.abs(newZoom - DEFAULT_ZOOM) < DEFAULT_ZOOM_SNAP) {
            newZoom = DEFAULT_ZOOM;
        }
        newZoom = Math.max(minZoom, Math.min(MAX_ZOOM, newZoom));
        if (Math.abs(newZoom - zoom) < EPSILON) {
            return false;
        }
        double graphX = toGraphX(viewX);
        double graphY = toGraphY(viewY);
        this.zoom = newZoom;
        this.originX = viewX - graphX * zoom;
        this.originY = viewY - graphY * zoom;
        return true;
    }

    /**
     * Zoom in as far as possible while the whole graph fits, and center it.
     * If it does not even fit at the smallest zoom level, the root is aligned to the left center.
     * @param bounds The graph bounds.
     * @param viewWidth The viewport width.
     * @param viewHeight The viewport height.
     */
    public void fit(CraftingPlanGraphLayout.Bounds bounds, int viewWidth, int viewHeight) {
        fit(bounds, viewWidth, viewHeight, MAX_ZOOM);
    }

    /**
     * Like {@link #fit(CraftingPlanGraphLayout.Bounds, int, int)}, but without zooming in as far,
     * so that small graphs do not look oversized when they are first shown.
     * @param bounds The graph bounds.
     * @param viewWidth The viewport width.
     * @param viewHeight The viewport height.
     */
    public void reset(CraftingPlanGraphLayout.Bounds bounds, int viewWidth, int viewHeight) {
        reset(bounds, viewWidth, viewHeight, INITIAL_MAX_ZOOM);
    }

    /**
     * Like {@link #reset(CraftingPlanGraphLayout.Bounds, int, int)}, with a custom zoom limit.
     * @param bounds The graph bounds.
     * @param viewWidth The viewport width.
     * @param viewHeight The viewport height.
     * @param maxZoom The largest zoom level to use.
     */
    public void reset(CraftingPlanGraphLayout.Bounds bounds, int viewWidth, int viewHeight, float maxZoom) {
        fit(bounds, viewWidth, viewHeight, maxZoom);
    }

    protected void fit(CraftingPlanGraphLayout.Bounds bounds, int viewWidth, int viewHeight, float maxZoom) {
        updateMinZoom(bounds, viewWidth, viewHeight);
        this.zoom = Math.max(minZoom, Math.min(maxZoom, getFitZoom(bounds, viewWidth, viewHeight)));
        if (fits(bounds, viewWidth, viewHeight, zoom)) {
            this.originX = (viewWidth - bounds.getWidth() * zoom) / 2 - bounds.getMinX() * zoom;
        } else {
            this.originX = MARGIN - bounds.getMinX() * zoom;
        }
        centerVertically(bounds, viewHeight);
    }

    /**
     * @param bounds The graph bounds.
     * @param viewWidth The viewport width.
     * @param viewHeight The viewport height.
     * @return The largest zoom level at which the whole graph fits in the viewport, including margins.
     */
    public static float getFitZoom(CraftingPlanGraphLayout.Bounds bounds, int viewWidth, int viewHeight) {
        float zoomX = bounds.getWidth() > 0 ? (viewWidth - 2F * MARGIN) / bounds.getWidth() : MAX_ZOOM;
        float zoomY = bounds.getHeight() > 0 ? (viewHeight - 2F * MARGIN) / bounds.getHeight() : MAX_ZOOM;
        return Math.max(0, Math.min(zoomX, zoomY));
    }

    /**
     * @param bounds The graph bounds.
     * @param viewWidth The viewport width.
     * @param viewHeight The viewport height.
     * @param zoom A zoom level.
     * @return If the whole graph fits in the viewport at the given zoom level, including margins.
     */
    public static boolean fits(CraftingPlanGraphLayout.Bounds bounds, int viewWidth, int viewHeight, float zoom) {
        return bounds.getWidth() * zoom + 2 * MARGIN <= viewWidth && bounds.getHeight() * zoom + 2 * MARGIN <= viewHeight;
    }

    protected void centerVertically(CraftingPlanGraphLayout.Bounds bounds, int viewHeight) {
        if (bounds.getHeight() * zoom + 2 * MARGIN <= viewHeight) {
            this.originY = (viewHeight - bounds.getHeight() * zoom) / 2 - bounds.getMinY() * zoom;
        } else {
            // Center the root, which is at the origin
            this.originY = viewHeight / 2D - CraftingPlanGraphLayout.ROOT_NODE_SIZE / 2D * zoom;
        }
    }

    /**
     * Restrict panning so that at least one node size of the graph stays visible,
     * and update how far the graph can be zoomed out.
     * @param bounds The graph bounds.
     * @param viewWidth The viewport width.
     * @param viewHeight The viewport height.
     */
    public void clamp(CraftingPlanGraphLayout.Bounds bounds, int viewWidth, int viewHeight) {
        updateMinZoom(bounds, viewWidth, viewHeight);
        double visible = CraftingPlanGraphLayout.NODE_SIZE * zoom;
        this.originX = clamp(originX, visible - bounds.getMaxX() * zoom, viewWidth - visible - bounds.getMinX() * zoom);
        this.originY = clamp(originY, visible - bounds.getMaxY() * zoom, viewHeight - visible - bounds.getMinY() * zoom);
    }

    protected static double clamp(double value, double min, double max) {
        if (min > max) {
            return (min + max) / 2;
        }
        return Math.max(min, Math.min(max, value));
    }

    public void copyFrom(CraftingPlanGraphCamera other) {
        this.originX = other.originX;
        this.originY = other.originY;
        this.zoom = other.zoom;
        this.minZoom = other.minZoom;
    }

}
