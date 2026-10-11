package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.ITerminalCraftingPlan;
import org.cyclops.integratedterminals.api.terminalstorage.crafting.TerminalCraftingJobStatus;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.cyclops.integratedterminals.client.gui.container.component.CraftingPlanGraphLayout.COLUMN_PITCH;
import static org.cyclops.integratedterminals.client.gui.container.component.CraftingPlanGraphLayout.NODE_SIZE;
import static org.cyclops.integratedterminals.client.gui.container.component.CraftingPlanGraphLayout.ROW_PITCH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author rubensworks
 */
public class TestCraftingPlanGraphLayout {

    protected static Node node(String name, Node... children) {
        return new Node(name, Arrays.asList(children));
    }

    protected static CraftingPlanGraphLayout.Placement<Node> placement(CraftingPlanGraphLayout<Node> layout, Node node) {
        for (CraftingPlanGraphLayout.Placement<Node> placement : layout.getPlacements()) {
            if (placement.getNode() == node) {
                return placement;
            }
        }
        return null;
    }

    @Test
    public void testSingleNode() {
        Node root = node("root");
        CraftingPlanGraphLayout<Node> layout = CraftingPlanGraphLayout.create(root);
        assertEquals(1, layout.getPlacements().size());
        assertEquals(0, placement(layout, root).getX());
        assertEquals(0, placement(layout, root).getY());
        assertTrue(layout.getSegments().isEmpty());
        assertBounds(layout.getBounds(), 0, 0, NODE_SIZE, NODE_SIZE);
    }

    @Test
    public void testChain() {
        Node c = node("c");
        Node b = node("b", c);
        Node a = node("a", b);
        CraftingPlanGraphLayout<Node> layout = CraftingPlanGraphLayout.create(a);
        assertEquals(0, placement(layout, a).getX());
        assertEquals(COLUMN_PITCH, placement(layout, b).getX());
        assertEquals(2 * COLUMN_PITCH, placement(layout, c).getX());
        for (CraftingPlanGraphLayout.Placement<Node> placement : layout.getPlacements()) {
            assertEquals(0, placement.getY());
        }
        assertBounds(layout.getBounds(), 0, 0, 2 * COLUMN_PITCH + NODE_SIZE, NODE_SIZE);
    }

    @Test
    public void testParentCenteredOnChildren() {
        Node c1 = node("c1");
        Node c2 = node("c2");
        Node c3 = node("c3");
        Node root = node("root", c1, c2, c3);
        CraftingPlanGraphLayout<Node> layout = CraftingPlanGraphLayout.create(root);
        assertEquals(0, placement(layout, root).getY());
        assertEquals(-ROW_PITCH, placement(layout, c1).getY());
        assertEquals(0, placement(layout, c2).getY());
        assertEquals(ROW_PITCH, placement(layout, c3).getY());
        assertBounds(layout.getBounds(), 0, -ROW_PITCH, COLUMN_PITCH + NODE_SIZE, ROW_PITCH + NODE_SIZE);
    }

    @Test
    public void testRootAlwaysAtOrigin() {
        Node root = node("root", node("a", node("a1"), node("a2"), node("a3")), node("b"));
        CraftingPlanGraphLayout<Node> layout = CraftingPlanGraphLayout.create(root);
        assertEquals(0, placement(layout, root).getX());
        assertEquals(0, placement(layout, root).getY());
    }

    @Test
    public void testNestedSubtreesDoNotOverlap() {
        Node a1 = node("a1");
        Node a2 = node("a2");
        Node a = node("a", a1, a2);
        Node b1 = node("b1");
        Node b2 = node("b2");
        Node b = node("b", b1, b2);
        Node root = node("root", a, b);
        CraftingPlanGraphLayout<Node> layout = CraftingPlanGraphLayout.create(root);

        // Each leaf has its own row, in depth-first order
        int y = placement(layout, a1).getY();
        assertEquals(y + ROW_PITCH, placement(layout, a2).getY());
        assertEquals(y + 2 * ROW_PITCH, placement(layout, b1).getY());
        assertEquals(y + 3 * ROW_PITCH, placement(layout, b2).getY());

        // No two nodes in the same column overlap
        for (CraftingPlanGraphLayout.Placement<Node> p1 : layout.getPlacements()) {
            for (CraftingPlanGraphLayout.Placement<Node> p2 : layout.getPlacements()) {
                if (p1 != p2 && p1.getX() == p2.getX()) {
                    assertTrue(Math.abs(p1.getY() - p2.getY()) >= NODE_SIZE);
                }
            }
        }
    }

    @Test
    public void testCollapsedSubtree() {
        Node a1 = node("a1");
        Node a2 = node("a2", node("a21"));
        Node a = node("a", a1, a2);
        Node b = node("b");
        Node root = node("root", a, b);
        a1.visible = false;
        a2.visible = false;
        CraftingPlanGraphLayout<Node> layout = CraftingPlanGraphLayout.create(root);

        assertEquals(3, layout.getPlacements().size());
        assertNull(placement(layout, a1));
        assertEquals(3, placement(layout, a).getHiddenDescendants());
        assertEquals(0, placement(layout, b).getHiddenDescendants());
        // The collapsed node takes a single row
        assertEquals(placement(layout, a).getY() + ROW_PITCH, placement(layout, b).getY());
        assertEquals(placement(layout, a).getY() + NODE_SIZE, layout.getBounds().getMaxY() - ROW_PITCH);
    }

    @Test
    public void testSegmentsConnectParentAndChildren() {
        Node c1 = node("c1");
        Node c2 = node("c2");
        Node root = node("root", c1, c2);
        CraftingPlanGraphLayout<Node> layout = CraftingPlanGraphLayout.create(root);
        CraftingPlanGraphLayout.Placement<Node> pRoot = placement(layout, root);

        int center = NODE_SIZE / 2;
        int trunkX = NODE_SIZE + CraftingPlanGraphLayout.COLUMN_GAP / 2;
        int rootCenterY = pRoot.getY() + center;
        List<CraftingPlanGraphLayout.Segment<Node>> segments = layout.getSegments();
        assertEquals(5, segments.size());

        // Stub out of the parent's right edge, which leads to all children
        CraftingPlanGraphLayout.Segment<Node> stub = segments.get(0);
        assertEquals(Arrays.asList(c1, c2), stub.getTargets());
        assertEquals(pRoot.getX() + NODE_SIZE, stub.getX1());
        assertEquals(rootCenterY, stub.getY1());
        assertEquals(trunkX, stub.getX2());

        // For each child, a part of the trunk from the parent's center, and a stub into the child's left edge
        for (int i = 0; i < 2; i++) {
            Node child = i == 0 ? c1 : c2;
            int childCenterY = placement(layout, child).getY() + center;

            CraftingPlanGraphLayout.Segment<Node> trunk = segments.get(1 + i * 2);
            assertEquals(Collections.singletonList(child), trunk.getTargets());
            assertEquals(trunkX, trunk.getX1());
            assertEquals(trunkX, trunk.getX2());
            assertEquals(Math.min(rootCenterY, childCenterY), trunk.getY1());
            assertEquals(Math.max(rootCenterY, childCenterY), trunk.getY2());

            CraftingPlanGraphLayout.Segment<Node> childStub = segments.get(2 + i * 2);
            assertEquals(Collections.singletonList(child), childStub.getTargets());
            assertEquals(trunkX, childStub.getX1());
            assertEquals(placement(layout, child).getX() - 1, childStub.getX2());
            assertEquals(childCenterY, childStub.getY1());
        }
    }

    @Test
    public void testEachChildHasAConnectedPath() {
        // Every child must be reachable from its parent through segments that lead to it alone,
        // so that the path to a single child can be highlighted
        Node root = node("root", node("a"), node("b"), node("c"));
        CraftingPlanGraphLayout<Node> layout = CraftingPlanGraphLayout.create(root);
        int center = NODE_SIZE / 2;
        int rootCenterY = placement(layout, root).getY() + center;
        for (Node child : root.getChildren()) {
            int childCenterY = placement(layout, child).getY() + center;
            boolean vertical = childCenterY == rootCenterY;
            boolean horizontal = false;
            for (CraftingPlanGraphLayout.Segment<Node> segment : layout.getSegments()) {
                if (segment.getTargets().equals(Collections.singletonList(child))) {
                    if (segment.getX1() == segment.getX2()) {
                        vertical |= segment.getY1() <= rootCenterY && segment.getY2() >= rootCenterY
                                && segment.getY1() <= childCenterY && segment.getY2() >= childCenterY;
                    } else {
                        horizontal |= segment.getY1() == childCenterY;
                    }
                }
            }
            assertTrue("vertical path to " + child, vertical);
            assertTrue("horizontal path to " + child, horizontal);
        }
    }

    @Test
    public void testSingleChildHasNoTrunk() {
        Node root = node("root", node("c"));
        CraftingPlanGraphLayout<Node> layout = CraftingPlanGraphLayout.create(root);
        assertEquals(2, layout.getSegments().size());
    }

    @Test
    public void testPlacementLookup() {
        Node hidden = node("hidden");
        Node c1 = node("c1", hidden);
        Node root = node("root", c1);
        hidden.visible = false;
        CraftingPlanGraphLayout<Node> layout = CraftingPlanGraphLayout.create(root);
        assertSame(c1, layout.getPlacement(c1).getNode());
        assertEquals(COLUMN_PITCH, layout.getPlacement(c1).getX());
        assertNull(layout.getPlacement(hidden));
    }

    @Test
    public void testCraftingPlanElements() {
        ITerminalCraftingPlan<Integer> plan = TestGuiCraftingPlanProgress.plan(1, TerminalCraftingJobStatus.UNSTARTED, 1, 1,
                Lists.newArrayList(
                        TestGuiCraftingPlanProgress.plan(2, TerminalCraftingJobStatus.UNSTARTED, 2, 2, Collections.emptyList()),
                        TestGuiCraftingPlanProgress.plan(3, TerminalCraftingJobStatus.UNSTARTED, 3, 3, Collections.emptyList())
                ));
        List<GuiCraftingPlan.Element> elements = GuiCraftingPlan.getElements(plan);
        GuiCraftingPlan.Element root = elements.get(0);
        assertEquals(3, CraftingPlanGraphLayout.create(root).getPlacements().size());

        // Collapsing in the same way as the list view
        for (GuiCraftingPlan.Element child : root.getChildren()) {
            child.setEnabled(false);
        }
        CraftingPlanGraphLayout<GuiCraftingPlan.Element> layout = CraftingPlanGraphLayout.create(root);
        assertEquals(1, layout.getPlacements().size());
        assertEquals(2, layout.getPlacements().get(0).getHiddenDescendants());
        assertFalse(root.getChildren().get(0).isVisible());
    }

    protected static void assertBounds(CraftingPlanGraphLayout.Bounds bounds, int minX, int minY, int maxX, int maxY) {
        assertEquals(minX, bounds.getMinX());
        assertEquals(minY, bounds.getMinY());
        assertEquals(maxX, bounds.getMaxX());
        assertEquals(maxY, bounds.getMaxY());
    }

    protected static class Node implements CraftingPlanGraphLayout.INode<Node> {
        private final String name;
        private final List<Node> children;
        private boolean visible = true;

        public Node(String name, List<Node> children) {
            this.name = name;
            this.children = children;
        }

        @Override
        public List<Node> getChildren() {
            return children;
        }

        @Override
        public boolean isVisible() {
            return visible;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
