package edu.shalini.ai.map;

/**
 * Template class defining te edge
 */
public class Edge {
    private final Node fromNode;
    private final Node toNode;
    private final double weight;

    public Edge(final Node fromNode, final Node toNode) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.weight = fromNode.getDistanceFrom(toNode);
    }

    public Node getFromNode() {
        return fromNode;
    }

    public Node getToNode() {
        return toNode;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        if (!fromNode.equals(edge.fromNode)) return false;
        return toNode.equals(edge.toNode);
    }

    @Override
    public int hashCode() {
        int result = fromNode.hashCode();
        result = 37 * result + toNode.hashCode();
        return result;
    }
}
