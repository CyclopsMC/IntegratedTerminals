package org.cyclops.integratedterminals.client.gui.container.component;

import org.junit.jupiter.api.Test;

import static org.cyclops.integratedterminals.client.gui.container.component.CraftingPlanGraphCamera.MARGIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author rubensworks
 */
public class TestCraftingPlanGraphCamera {

    private static final double EPSILON = 0.0001;

    @Test
    public void testDefaults() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        assertEquals(1F, camera.getZoom(), EPSILON);
        assertEquals(10, camera.toViewX(10), EPSILON);
        assertEquals(10, camera.toGraphY(10), EPSILON);
    }

    @Test
    public void testPan() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        camera.pan(5, -3);
        assertEquals(5, camera.toViewX(0), EPSILON);
        assertEquals(-3, camera.toViewY(0), EPSILON);
        assertEquals(-5, camera.toGraphX(0), EPSILON);
    }

    @Test
    public void testZoomAtKeepsAnchorFixed() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        camera.pan(17, 23);
        double graphX = camera.toGraphX(100);
        double graphY = camera.toGraphY(60);

        assertTrue(camera.zoomAt(100, 60, 1));
        assertEquals(1.2F, camera.getZoom(), EPSILON);
        assertEquals(100, camera.toViewX(graphX), EPSILON);
        assertEquals(60, camera.toViewY(graphY), EPSILON);

        assertTrue(camera.zoomAt(100, 60, -1));
        assertTrue(camera.zoomAt(100, 60, -1));
        assertEquals(1F / 1.2F, camera.getZoom(), EPSILON);
        assertEquals(100, camera.toViewX(graphX), EPSILON);
        assertEquals(60, camera.toViewY(graphY), EPSILON);
    }

    @Test
    public void testZoomIsClamped() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        for (int i = 0; i < 20; i++) {
            camera.zoomAt(0, 0, 1);
        }
        assertEquals(CraftingPlanGraphCamera.MAX_ZOOM, camera.getZoom(), EPSILON);
        assertFalse(camera.canZoomIn());
        assertFalse(camera.zoomAt(0, 0, 1));
        for (int i = 0; i < 20; i++) {
            camera.zoomAt(0, 0, -1);
        }
        assertEquals(CraftingPlanGraphCamera.MIN_ZOOM, camera.getZoom(), EPSILON);
        assertFalse(camera.canZoomOut());
        assertFalse(camera.zoomAt(0, 0, -1));
    }

    @Test
    public void testZoomStopsAtDefault() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        // Fits at 0.92
        camera.fit(new CraftingPlanGraphLayout.Bounds(0, 0, 200, 50), 200, 300);
        assertTrue(camera.zoomAt(0, 0, 1));
        assertEquals(1F, camera.getZoom(), EPSILON);
        assertTrue(camera.zoomAt(0, 0, 1));
        assertEquals(1.2F, camera.getZoom(), EPSILON);
    }

    @Test
    public void testZoomSnapsToDefault() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        for (int i = 0; i < 20; i++) {
            camera.zoomAt(0, 0, 1);
        }
        // Zooming out from the maximum does not hit the default level exactly, so it snaps to it
        while (camera.getZoom() > 1.1F) {
            camera.zoomAt(0, 0, -1);
        }
        assertEquals(1F, camera.getZoom(), EPSILON);
    }

    @Test
    public void testMinZoomDependsOnGraphSize() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();

        // Small graphs can be zoomed out to the default minimum
        camera.updateMinZoom(new CraftingPlanGraphLayout.Bounds(0, 0, 100, 100), 200, 200);
        assertEquals(CraftingPlanGraphCamera.MIN_ZOOM, camera.getMinZoom(), EPSILON);

        // Larger graphs can be zoomed out until they fit
        camera.updateMinZoom(new CraftingPlanGraphLayout.Bounds(0, 0, 600, 100), 200, 200);
        assertEquals(184F / 600, camera.getMinZoom(), EPSILON);

        // But not until nodes are too small to recognize
        camera.updateMinZoom(new CraftingPlanGraphLayout.Bounds(0, 0, 10000, 100), 200, 200);
        assertEquals(CraftingPlanGraphCamera.MIN_ZOOM_LARGE_GRAPH, camera.getMinZoom(), EPSILON);
    }

    @Test
    public void testClampUpdatesMinZoom() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        camera.clamp(new CraftingPlanGraphLayout.Bounds(0, 0, 600, 100), 200, 200);
        for (int i = 0; i < 20; i++) {
            camera.zoomAt(0, 0, -1);
        }
        assertEquals(184F / 600, camera.getZoom(), EPSILON);
    }

    @Test
    public void testFitSmallGraphZoomsIn() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        CraftingPlanGraphLayout.Bounds bounds = new CraftingPlanGraphLayout.Bounds(0, -16, 74, 42);
        camera.fit(bounds, 238, 178);
        assertEquals(162F / 58, camera.getZoom(), EPSILON);
        // Centered
        assertEquals(238 - camera.toViewX(74), camera.toViewX(0), EPSILON);
        assertEquals(178 - camera.toViewY(42), camera.toViewY(-16), EPSILON);
    }

    @Test
    public void testFitTinyGraphIsLimited() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        camera.fit(new CraftingPlanGraphLayout.Bounds(0, 0, 26, 26), 400, 300);
        assertEquals(CraftingPlanGraphCamera.MAX_ZOOM, camera.getZoom(), EPSILON);
    }

    @Test
    public void testFitZoomsToTheGraph() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        // 184 / 200 = 0.92 horizontally, which is the limit
        CraftingPlanGraphLayout.Bounds bounds = new CraftingPlanGraphLayout.Bounds(0, 0, 200, 50);
        camera.fit(bounds, 200, 300);
        assertEquals(0.92F, camera.getZoom(), EPSILON);
        assertTrue(CraftingPlanGraphCamera.fits(bounds, 200, 300, camera.getZoom()));
    }

    @Test
    public void testFitTooLargeAlignsRootLeft() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        CraftingPlanGraphLayout.Bounds bounds = new CraftingPlanGraphLayout.Bounds(0, -1000, 1000, 1000);
        camera.fit(bounds, 200, 100);
        assertEquals(CraftingPlanGraphCamera.MIN_ZOOM_LARGE_GRAPH, camera.getZoom(), EPSILON);
        assertEquals(MARGIN, camera.toViewX(0), EPSILON);
        // Root center is vertically centered
        assertEquals(50, camera.toViewY(CraftingPlanGraphLayout.ROOT_NODE_SIZE / 2D), EPSILON);
    }

    @Test
    public void testResetLimitsZoomAndCenters() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        CraftingPlanGraphLayout.Bounds bounds = new CraftingPlanGraphLayout.Bounds(0, -16, 74, 42);
        camera.reset(bounds, 238, 178);
        // It would fit at 2.79, but the initial view is limited to 1.5
        assertEquals(1.5F, camera.getZoom(), EPSILON);
        assertEquals(238 - camera.toViewX(74), camera.toViewX(0), EPSILON);
        assertEquals(178 - camera.toViewY(42), camera.toViewY(-16), EPSILON);
    }

    @Test
    public void testResetWithCustomLimit() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        CraftingPlanGraphLayout.Bounds bounds = new CraftingPlanGraphLayout.Bounds(0, -16, 74, 42);
        camera.reset(bounds, 238, 178, CraftingPlanGraphCamera.DEFAULT_ZOOM);
        assertEquals(1F, camera.getZoom(), EPSILON);
    }

    @Test
    public void testResetLargeGraphZoomsOut() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        CraftingPlanGraphLayout.Bounds bounds = new CraftingPlanGraphLayout.Bounds(0, -100, 170, 100);
        camera.reset(bounds, 200, 200);
        assertEquals(0.92F, camera.getZoom(), EPSILON);
        assertEquals(200 - camera.toViewX(170), camera.toViewX(0), EPSILON);
    }

    @Test
    public void testResetTooLargeAlignsRootLeft() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        CraftingPlanGraphLayout.Bounds bounds = new CraftingPlanGraphLayout.Bounds(0, -1000, 1000, 1000);
        camera.reset(bounds, 200, 100);
        assertEquals(CraftingPlanGraphCamera.MIN_ZOOM_LARGE_GRAPH, camera.getZoom(), EPSILON);
        assertEquals(MARGIN, camera.toViewX(0), EPSILON);
    }

    @Test
    public void testClampKeepsGraphReachable() {
        CraftingPlanGraphLayout.Bounds bounds = new CraftingPlanGraphLayout.Bounds(0, -50, 100, 76);
        int node = CraftingPlanGraphLayout.NODE_SIZE;

        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        camera.pan(-10000, -10000);
        camera.clamp(bounds, 200, 150);
        assertEquals(node, camera.toViewX(100), EPSILON);
        assertEquals(node, camera.toViewY(76), EPSILON);

        camera.pan(20000, 20000);
        camera.clamp(bounds, 200, 150);
        assertEquals(200 - node, camera.toViewX(0), EPSILON);
        assertEquals(150 - node, camera.toViewY(-50), EPSILON);

        // Positions within the limits are untouched
        CraftingPlanGraphCamera camera2 = new CraftingPlanGraphCamera();
        camera2.pan(10, 60);
        camera2.clamp(bounds, 200, 150);
        assertEquals(10, camera2.getOriginX(), EPSILON);
        assertEquals(60, camera2.getOriginY(), EPSILON);
    }

    @Test
    public void testFits() {
        CraftingPlanGraphLayout.Bounds bounds = new CraftingPlanGraphLayout.Bounds(0, -100, 100, 100);
        assertTrue(CraftingPlanGraphCamera.fits(bounds, 116, 216, 1F));
        assertFalse(CraftingPlanGraphCamera.fits(bounds, 115, 216, 1F));
        assertFalse(CraftingPlanGraphCamera.fits(bounds, 116, 215, 1F));
        assertTrue(CraftingPlanGraphCamera.fits(bounds, 66, 116, 0.5F));
    }

    @Test
    public void testCopyFrom() {
        CraftingPlanGraphCamera camera = new CraftingPlanGraphCamera();
        camera.pan(3, 4);
        camera.zoomAt(0, 0, 1);
        CraftingPlanGraphCamera copy = new CraftingPlanGraphCamera();
        copy.copyFrom(camera);
        assertEquals(camera.getOriginX(), copy.getOriginX(), EPSILON);
        assertEquals(camera.getOriginY(), copy.getOriginY(), EPSILON);
        assertEquals(camera.getZoom(), copy.getZoom(), EPSILON);
    }

}
