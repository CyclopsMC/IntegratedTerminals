package org.cyclops.integratedterminals.client.gui.container.component;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * Places the nodes of a tree in columns from left to right, with the root at the graph origin.
 *
 * This class is independent of Minecraft, so it can be unit-tested and ported easily.
 *
 * @param <N> The node type.
 * @author rubensworks
 */
public class CraftingPlanGraphLayout<N extends CraftingPlanGraphLayout.INode<N>> {

    public static final int NODE_SIZE = 26;
    public static final int COLUMN_GAP = 22;
    public static final int ROW_GAP = 6;
    public static final int COLUMN_PITCH = NODE_SIZE + COLUMN_GAP;
    public static final int ROW_PITCH = NODE_SIZE + ROW_GAP;

    private final List<Placement<N>> placements;
    private final List<Segment<N>> segments;
    private final Bounds bounds;

    private CraftingPlanGraphLayout(List<Placement<N>> placements, List<Segment<N>> segments, Bounds bounds) {
        this.placements = placements;
        this.segments = segments;
        this.bounds = bounds;
    }

    /**
     * @param root The root node.
     * @param <N> The node type.
     * @return The layout of all nodes that are not collapsed into one of their ancestors.
     */
    public static <N extends INode<N>> CraftingPlanGraphLayout<N> create(N root) {
        List<Placement<N>> placements = Lists.newArrayList();
        List<Segment<N>> segments = Lists.newArrayList();
        Placement<N> rootPlacement = place(root, 0, new int[]{0}, placements, segments);

        // Shift everything vertically so that the root ends up at the origin
        int shiftY = -rootPlacement.getY();
        int minX = 0;
        int minY = Integer.MAX_VALUE;
        int maxX = 0;
        int maxY = Integer.MIN_VALUE;
        for (Placement<N> placement : placements) {
            placement.y += shiftY;
            minY = Math.min(minY, placement.y);
            maxX = Math.max(maxX, placement.x + NODE_SIZE);
            maxY = Math.max(maxY, placement.y + NODE_SIZE);
        }
        for (Segment<N> segment : segments) {
            segment.y1 += shiftY;
            segment.y2 += shiftY;
        }
        return new CraftingPlanGraphLayout<>(Collections.unmodifiableList(placements),
                Collections.unmodifiableList(segments), new Bounds(minX, minY, maxX, maxY));
    }

    private static <N extends INode<N>> Placement<N> place(N node, int depth, int[] nextRow,
                                                           List<Placement<N>> placements, List<Segment<N>> segments) {
        Placement<N> placement = new Placement<>(node, depth * COLUMN_PITCH);
        placements.add(placement);

        List<Placement<N>> childPlacements = Lists.newArrayList();
        int hidden = 0;
        for (N child : node.getChildren()) {
            if (child.isVisible()) {
                childPlacements.add(place(child, depth + 1, nextRow, placements, segments));
            } else {
                hidden += countNodes(child);
            }
        }
        placement.hiddenDescendants = hidden;

        if (childPlacements.isEmpty()) {
            // Leaves (and collapsed nodes) take the next free row
            placement.y = nextRow[0]++ * ROW_PITCH;
        } else {
            int firstY = childPlacements.get(0).y;
            int lastY = childPlacements.get(childPlacements.size() - 1).y;
            placement.y = (firstY + lastY) / 2;

            // Elbow connectors: a stub out of the parent, a vertical trunk, and a stub into each child
            int center = NODE_SIZE / 2;
            int parentRight = placement.x + NODE_SIZE;
            int trunkX = parentRight + COLUMN_GAP / 2;
            segments.add(new Segment<>(parentRight, placement.y + center, trunkX, placement.y + center, null));
            if (firstY != lastY) {
                segments.add(new Segment<>(trunkX, firstY + center, trunkX, lastY + center, null));
            }
            for (Placement<N> childPlacement : childPlacements) {
                segments.add(new Segment<>(trunkX, childPlacement.y + center, childPlacement.x - 1, childPlacement.y + center,
                        childPlacement.getNode()));
            }
        }
        return placement;
    }

    protected static <N extends INode<N>> int countNodes(N node) {
        int count = 1;
        for (N child : node.getChildren()) {
            count += countNodes(child);
        }
        return count;
    }

    /**
     * @return All visible nodes, in depth-first order, starting with the root.
     */
    public List<Placement<N>> getPlacements() {
        return placements;
    }

    /**
     * @return The line segments that connect the nodes.
     */
    public List<Segment<N>> getSegments() {
        return segments;
    }

    public Bounds getBounds() {
        return bounds;
    }

    /**
     * @param graphX An X position in graph space.
     * @param graphY A Y position in graph space.
     * @return The node at the given position, or null.
     */
    public Placement<N> getPlacementAt(double graphX, double graphY) {
        for (Placement<N> placement : placements) {
            if (graphX >= placement.getX() && graphX < placement.getX() + NODE_SIZE
                    && graphY >= placement.getY() && graphY < placement.getY() + NODE_SIZE) {
                return placement;
            }
        }
        return null;
    }

    /**
     * A node in a tree that can be laid out.
     * @param <N> The node type.
     */
    public static interface INode<N extends INode<N>> {
        /**
         * @return All children, including invisible ones.
         */
        public List<N> getChildren();

        /**
         * @return If this node is shown, otherwise it is collapsed into its parent.
         */
        public boolean isVisible();
    }

    /**
     * The position of a node in graph space.
     * @param <N> The node type.
     */
    public static class Placement<N> {
        private final N node;
        private final int x;
        private int y;
        private int hiddenDescendants;

        public Placement(N node, int x) {
            this.node = node;
            this.x = x;
        }

        public N getNode() {
            return node;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        /**
         * @return The number of descendants that are hidden because they are collapsed into this node.
         */
        public int getHiddenDescendants() {
            return hiddenDescendants;
        }
    }

    /**
     * An axis-aligned line in graph space, from (x1, y1) to (x2, y2), both inclusive.
     */
    public static class Segment<N> {
        private final int x1;
        private int y1;
        private final int x2;
        private int y2;
        private final N target;

        public Segment(int x1, int y1, int x2, int y2, N target) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.target = target;
        }

        public int getX1() {
            return x1;
        }

        public int getY1() {
            return y1;
        }

        public int getX2() {
            return x2;
        }

        public int getY2() {
            return y2;
        }

        /**
         * @return The child node this segment leads into, or null if it is shared by all children.
         */
        public N getTarget() {
            return target;
        }
    }

    /**
     * A rectangle in graph space, with exclusive maximum.
     */
    public static class Bounds {
        private final int minX;
        private final int minY;
        private final int maxX;
        private final int maxY;

        public Bounds(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        public int getMinX() {
            return minX;
        }

        public int getMinY() {
            return minY;
        }

        public int getMaxX() {
            return maxX;
        }

        public int getMaxY() {
            return maxY;
        }

        public int getWidth() {
            return maxX - minX;
        }

        public int getHeight() {
            return maxY - minY;
        }
    }

}
