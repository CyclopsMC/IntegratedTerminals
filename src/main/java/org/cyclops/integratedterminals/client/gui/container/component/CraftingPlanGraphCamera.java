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

    public static final float[] ZOOM_LEVELS = {0.5F, 0.75F, 1F, 1.5F, 2F};
    public static final int DEFAULT_ZOOM_INDEX = 2;
    public static final int MARGIN = 8;

    private double originX;
    private double originY;
    private int zoomIndex = DEFAULT_ZOOM_INDEX;

    public double getOriginX() {
        return originX;
    }

    public double getOriginY() {
        return originY;
    }

    public int getZoomIndex() {
        return zoomIndex;
    }

    public float getZoom() {
        return ZOOM_LEVELS[zoomIndex];
    }

    public boolean canZoomIn() {
        return zoomIndex < ZOOM_LEVELS.length - 1;
    }

    public boolean canZoomOut() {
        return zoomIndex > 0;
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
     * @param viewX The anchor X position in the viewport.
     * @param viewY The anchor Y position in the viewport.
     * @param direction Positive to zoom in, negative to zoom out.
     * @return If the zoom level changed.
     */
    public boolean zoomAt(double viewX, double viewY, int direction) {
        int newZoomIndex = Math.max(0, Math.min(ZOOM_LEVELS.length - 1, zoomIndex + Integer.signum(direction)));
        if (newZoomIndex == zoomIndex) {
            return false;
        }
        double graphX = toGraphX(viewX);
        double graphY = toGraphY(viewY);
        this.zoomIndex = newZoomIndex;
        this.originX = viewX - graphX * getZoom();
        this.originY = viewY - graphY * getZoom();
        return true;
    }

    /**
     * Pick the largest zoom level at which the whole graph fits, and center it.
     * If it does not even fit at the smallest level, the root is aligned to the left center.
     * @param bounds The graph bounds.
     * @param viewWidth The viewport width.
     * @param viewHeight The viewport height.
     */
    public void fit(CraftingPlanGraphLayout.Bounds bounds, int viewWidth, int viewHeight) {
        if (fitZoom(bounds, viewWidth, viewHeight, ZOOM_LEVELS.length - 1)) {
            this.originX = (viewWidth - bounds.getWidth() * getZoom()) / 2 - bounds.getMinX() * getZoom();
        } else {
            this.originX = MARGIN - bounds.getMinX() * getZoom();
        }
        centerVertically(bounds, viewHeight);
    }

    /**
     * Like {@link #fit(CraftingPlanGraphLayout.Bounds, int, int)}, but never zooms in beyond the default level,
     * and always keeps the root at the left.
     * @param bounds The graph bounds.
     * @param viewWidth The viewport width.
     * @param viewHeight The viewport height.
     */
    public void reset(CraftingPlanGraphLayout.Bounds bounds, int viewWidth, int viewHeight) {
        fitZoom(bounds, viewWidth, viewHeight, DEFAULT_ZOOM_INDEX);
        this.originX = MARGIN - bounds.getMinX() * getZoom();
        centerVertically(bounds, viewHeight);
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

    protected boolean fitZoom(CraftingPlanGraphLayout.Bounds bounds, int viewWidth, int viewHeight, int maxZoomIndex) {
        for (int i = maxZoomIndex; i >= 0; i--) {
            if (fits(bounds, viewWidth, viewHeight, ZOOM_LEVELS[i])) {
                this.zoomIndex = i;
                return true;
            }
        }
        this.zoomIndex = 0;
        return false;
    }

    protected void centerVertically(CraftingPlanGraphLayout.Bounds bounds, int viewHeight) {
        float zoom = getZoom();
        if (bounds.getHeight() * zoom + 2 * MARGIN <= viewHeight) {
            this.originY = (viewHeight - bounds.getHeight() * zoom) / 2 - bounds.getMinY() * zoom;
        } else {
            // Center the root, which is at the origin
            this.originY = viewHeight / 2D - CraftingPlanGraphLayout.NODE_SIZE / 2D * zoom;
        }
    }

    /**
     * Restrict panning so that at least one node size of the graph stays visible.
     * @param bounds The graph bounds.
     * @param viewWidth The viewport width.
     * @param viewHeight The viewport height.
     */
    public void clamp(CraftingPlanGraphLayout.Bounds bounds, int viewWidth, int viewHeight) {
        float zoom = getZoom();
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
        this.zoomIndex = other.zoomIndex;
    }

}
