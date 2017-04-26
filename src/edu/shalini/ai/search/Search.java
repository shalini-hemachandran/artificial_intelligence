package edu.shalini.ai.search;

import edu.shalini.ai.map.Edge;
import edu.shalini.ai.map.Node;

import java.util.*;

public class Search {
    public class Result {
        private final List<Node> shortestPath;
        private final  List<Double> shortestPathGScores;
        private final double shortestPathCost;
        private final int numExpansions;
        private final Set<Node> expandedNodes;

        public Result(final List<Node> shortestPath, final List<Double> shortestPathGScores, double shortestPathCost,
                      final int numExpansions, final Set<Node> expandedNodes) {
            this.shortestPath = shortestPath;
            this.shortestPathGScores = shortestPathGScores;
            this.shortestPathCost = shortestPathCost;
            this.numExpansions = numExpansions;
            this.expandedNodes = expandedNodes;
        }

        public List<Node> getShortestPath() {
            return shortestPath;
        }

        public List<Double> getShortestPathGScores() {
            return this.shortestPathGScores;
        }

        public double getShortestPathCost() {
            return shortestPathCost;
        }

        public int getNumExpansions() {
            return numExpansions;
        }

        public Set<Node> getExpandedNodes() {
            return expandedNodes;
        }
    }

    private String getPrefix(final Collection<Node> targetNodes) {
        final SortedSet<String> targetNodeNames = new TreeSet<>();
        for (final Node node : targetNodes) {
            targetNodeNames.add(node.getName());
        }

        final StringBuffer prefix = new StringBuffer("");
        for (final String nodeName : targetNodeNames) {
            if (prefix.length() > 0) {
                prefix.append("/");
            }
            prefix.append(nodeName);
        }
        return prefix.toString();
    }

    public Result findShortestPath(final Node sourceNode, final Collection<Node> targetNodes,
                                   final HScoreComputer computer) {
        final String sourcePrefix = getPrefix(targetNodes);
        final Queue<Node> unvisitedNodes = new PriorityQueue<>();
        int numExpansions = 0;
        final Set<Node> expandedNodes = new HashSet<>();
        sourceNode.createSourceState(sourcePrefix, computer);
        unvisitedNodes.add(sourceNode);

        while(!unvisitedNodes.isEmpty()) {
            final Node nodeWithLowestFScore = unvisitedNodes.poll();
            final Node.State stateWithLowestFScore = nodeWithLowestFScore.getMinFScoreState();
            if (stateWithLowestFScore.prefix.isEmpty()) { // Empty prefix implies that all target nodes were visited.
                final List<Node> shortestPath = reconstructPath(nodeWithLowestFScore, stateWithLowestFScore);
                final List<Double> shortestPathGScores =
                        reconstructShortestPathGScores(nodeWithLowestFScore, stateWithLowestFScore);
                return new Result(shortestPath, shortestPathGScores,
                        getShortestPathScore(nodeWithLowestFScore, stateWithLowestFScore), numExpansions,
                        expandedNodes);
            }

            stateWithLowestFScore.visited = true;
            ++numExpansions;
            expandedNodes.add(nodeWithLowestFScore);
            for (final Edge neighborEdge : nodeWithLowestFScore.getIncidentEdges()) {
                final Node neighborNode = neighborEdge.getToNode();
                final String neighborPrefix = neighborNode.computePrefix(stateWithLowestFScore.prefix);
                final Node.State neighborState = neighborNode.getStateByPrefix(neighborPrefix);
                if (neighborState == null || !neighborState.visited) {
                    neighborNode.updateGScoreFromPrevNode(nodeWithLowestFScore, stateWithLowestFScore.prefix, computer);
                    unvisitedNodes.remove(neighborNode);
                    unvisitedNodes.add(neighborNode);
                }
            }

            // Add the node back as it may have other valid unvisited states.
            if (nodeWithLowestFScore.getMinFScoreState() != null) {
                unvisitedNodes.add(nodeWithLowestFScore);
            }
        }

        throw new IllegalArgumentException("Destinations not reachable from " + sourceNode.getName());
    }

    private double getShortestPathScore(final Node node, final Node.State nodeState) {
        return nodeState.getFScore();
    }

    private List<Node> reconstructPath(final Node node, final Node.State nodeState) {
        final List<Node> path = new ArrayList<>();

        Node currNode = node;
        Node.State currState = nodeState;
        while(currNode != null) {
            path.add(0, currNode);
            currNode = currState.prevNode;
            currState = (currNode == null ? null : currNode.getStateByPrefix(currState.prevNodePrefix));
        }

        return path;
    }

    private List<Double> reconstructShortestPathGScores(final Node node, final Node.State nodeState) {
        final List<Double> shortestPathGScores = new ArrayList<>();

        Node currNode = node;
        Node.State currState = nodeState;
        while(currNode != null) {
            shortestPathGScores.add(0, currState.gScore);
            currNode = currState.prevNode;
            currState = (currNode == null ? null : currNode.getStateByPrefix(currState.prevNodePrefix));
        }

        return shortestPathGScores;
    }
}
