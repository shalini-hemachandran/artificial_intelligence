package edu.shalini.ai.map;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class WorldMap {
    private static final int MAX_RETRIES = 500;
    private static final double EDGE_DISTANCE_MULTIPLIER = 1.5;

    private final Map<String, Node> nodesByName;

    private WorldMap() {
        this.nodesByName = new HashMap<>();
    }

    public void clearStates() {
        for (final Node node : nodesByName.values()) {
            node.clearStates();
        }
    }

    public void addNode(final Node node) {
        nodesByName.put(node.getName(), node);
    }

    public Node getNodeByName(final String name) {
        return nodesByName.get(name);
    }

    public Collection<Node> getNodes() {
        return nodesByName.values();
    }

    public static WorldMap createEmptyWorldMap() {
        return new WorldMap();
    }

    public static WorldMap generateMap(final int noOfNodes, final double minDistance, final int maxX, final int maxY) {
        final WorldMap generatedMap = new WorldMap();

        // Add nodes.
        for (int i = 0; i < noOfNodes; ++i) {
            for (int j = 0; j < MAX_RETRIES; ++j) {
                int x = ThreadLocalRandom.current().nextInt(0, maxX + 1);
                int y = ThreadLocalRandom.current().nextInt(0, maxY + 1);
                final Node randomNode = new Node(String.valueOf(i + 1), x, y);
                boolean foundTooClose = false;
                for (final Node existingNode : generatedMap.getNodes()) {
                    if (randomNode.getDistanceFrom(existingNode) < minDistance) {
                        foundTooClose = true;
                        break;
                    }
                }
                if (!foundTooClose) {
                    generatedMap.addNode(randomNode);
                    break;
                }
            }
        }

        if (generatedMap.getNodes().size() < 2) {
            return generatedMap;
        }

        // Add edges.
        for (final Node node : generatedMap.getNodes()) {
            Node closestNode = null;
            double closestNodeDistance = 0.0;

            for (final Node otherNode : generatedMap.getNodes()) {
                if (otherNode == node) {
                    continue;
                }

                final double otherNodeDistance = node.getDistanceFrom(otherNode);
                if (closestNode == null || otherNodeDistance < closestNodeDistance) {
                    closestNode = otherNode;
                    closestNodeDistance = otherNodeDistance;
                }
            }

            final double maxEdgeWeight = closestNodeDistance * EDGE_DISTANCE_MULTIPLIER;
            for (final Node otherNode : generatedMap.getNodes()) {
                if (otherNode == node) {
                    continue;
                }

                if (node.getDistanceFrom(otherNode) <= maxEdgeWeight) {
                    node.addEdge(new Edge(node, otherNode));
                    otherNode.addEdge(new Edge(otherNode, node));
                }
            }
        }

        return generatedMap;
    }
}
