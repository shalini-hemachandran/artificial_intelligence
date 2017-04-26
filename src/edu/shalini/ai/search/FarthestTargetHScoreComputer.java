package edu.shalini.ai.search;

import edu.shalini.ai.map.Node;
import edu.shalini.ai.map.WorldMap;

import java.util.HashSet;
import java.util.Set;

public class FarthestTargetHScoreComputer implements HScoreComputer {
    private final WorldMap worldMap;

    public FarthestTargetHScoreComputer(final WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    private Set<Node> getTargetNodesFromPrefix(final String prefix) {
        final String[] nodeNames = prefix.split("/");
        final Set<Node> targetNodes = new HashSet<>();

        if (prefix.isEmpty()) return targetNodes;

        for (final String nodeName : nodeNames) {
            targetNodes.add(worldMap.getNodeByName(nodeName));
        }
        return targetNodes;
    }

    @Override
    public double computeHScore(Node node, String prefix) {
        final Set<Node> targetNodes = getTargetNodesFromPrefix(prefix);

        double maxDistance = 0;
        for (final Node targetNode : targetNodes) {
            final double distance = node.getDistanceFrom(targetNode);
            maxDistance = Math.max(maxDistance, distance);
        }

        return maxDistance;
    }
}
