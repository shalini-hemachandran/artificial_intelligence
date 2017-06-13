package edu.shalini.ai.map;

import edu.shalini.ai.search.HScoreComputer;

import java.util.*;

/**
 * Template class defining the node
 */
public class Node implements Comparable<Node> {
    private final String name;
    private final int x;
    private final int y;
    private final Set<Edge> incidentEdges;
    private final Map<String, State> stateByPrefix;
    private double alreadyWaited;

    public class State {
        public String prefix;
        public double gScore = Double.MAX_VALUE;
        public double maxWaitingTime = 0.0;
        public double hScore = Double.MAX_VALUE;
        public boolean visited = false;
        public Node prevNode = null;
        public String prevNodePrefix = "";

        public double getFScore() {
            return Math.max(gScore + hScore, maxWaitingTime);
        }
    }

    public Node(final String name, final int x, final int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.incidentEdges = new HashSet<>();
        this.stateByPrefix = new HashMap<>();
        this.alreadyWaited = 0.0;
    }

    public void clearStates() {
        this.stateByPrefix.clear();
    }

    public void addAlreadyWaited(final double alreadyWaited) {
        this.alreadyWaited += alreadyWaited;
    }

    public double getAlreadyWaited() {
        return this.alreadyWaited;
    }

    public void clearAlreadyWaited() {
        this.alreadyWaited = 0.0;
    }

    public void createSourceState(final String prefix, final HScoreComputer computer) {
        final State newState = new State();
        newState.prefix = prefix;
        newState.gScore = 0;
        newState.maxWaitingTime = alreadyWaited;

        newState.hScore = computer.computeHScore(this, prefix);
        newState.prevNode = null;
        newState.prevNodePrefix = "";
        stateByPrefix.put(prefix, newState);
    }

    public void addEdge(final Edge edge) {
        if (edge.getFromNode() == this) {
            for (final Edge existingEdge : incidentEdges) {
                if (existingEdge.getToNode() == edge.getToNode()) {
                    // Edge already present.
                    return;
                }
            }
            incidentEdges.add(edge);
        } else {
            throw new IllegalArgumentException("Node '" + name + "' is not the fromNode on edge.");
        }
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getDistanceFrom(final Node node) {
        final double xDiff = getX() - node.getX();
        final double yDiff = getY() - node.getY();
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    public double getDistanceFrom(final int x, final int y) {
        final double xDiff = getX() - x;
        final double yDiff = getY() - y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    public Set<Edge> getIncidentEdges() {
        return incidentEdges;
    }

    public State getStateByPrefix(final String prefix) {
        return stateByPrefix.get(prefix);
    }

    public Node getPrevNode(final String prefix) {
        if (stateByPrefix.containsKey(prefix)) {
            return stateByPrefix.get(prefix).prevNode;
        } else {
            return null;
        }
    }

    public String getPrevNodePrefix(final String prefix) {
        if (stateByPrefix.containsKey(prefix)) {
            return stateByPrefix.get(prefix).prevNodePrefix;
        } else {
            return "";
        }
    }

    public void setVisited(final String prefix, final boolean visited) {
        final State currState = stateByPrefix.get(prefix);
        if (currState != null) {
            currState.visited = visited;
        } else {
            throw new IllegalArgumentException("Cannot set visited for node not in queue.");
        }
    }

    public boolean isVisited(final String prefix) {
        final State currState = stateByPrefix.get(prefix);
        if (currState != null) {
            return currState.visited;
        } else {
            return false;
        }
    }

    public String computePrefix(final String prevPrefix) {
        final String[] prefixParts = prevPrefix.split("/");
        final StringBuilder prefix = new StringBuilder("");
        for (final String part : prefixParts) {
            if (!part.equals(name)) {
                if (prefix.length() > 0) {
                    prefix.append("/");
                }
                prefix.append(part);
            }
        }
        return prefix.toString();
    }

    public void updateGScoreFromPrevNode(final Node prevNode, final String prevPrefix, final HScoreComputer computer) {
        final String prefix = computePrefix(prevPrefix);
        final State prevState = prevNode.getStateByPrefix(prevPrefix);
        double gScoreFromPrevNode = prevState.gScore + getDistanceFrom(prevNode);
        final State currState = stateByPrefix.get(prefix);
        if (currState != null && gScoreFromPrevNode < currState.gScore) {
            currState.gScore = gScoreFromPrevNode;
            currState.maxWaitingTime = Math.max(currState.gScore + alreadyWaited, prevState.maxWaitingTime);
            currState.prevNode = prevNode;
            currState.prevNodePrefix = prevPrefix;
        } else if (currState == null) {
            final State newState = new State();
            newState.prefix = prefix;
            newState.gScore = gScoreFromPrevNode;
            newState.maxWaitingTime = Math.max(newState.gScore + alreadyWaited, prevState.maxWaitingTime);
            newState.hScore = computer.computeHScore(this, prefix);
            newState.prevNode = prevNode;
            newState.prevNodePrefix = prevPrefix;
            stateByPrefix.put(prefix, newState);
        }
    }

    public State getMinFScoreState() {
        State minFScoreState = null;
        for (final State state : stateByPrefix.values()) {
            if (!state.visited && (minFScoreState == null || state.getFScore() < minFScoreState.getFScore())) {
                minFScoreState = state;
            }
        }
        return minFScoreState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;
        return name.equals(node.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(Node o) {
        final State thisState = getMinFScoreState();
        final State otherState = o.getMinFScoreState();
        final double diff = thisState.getFScore() - otherState.getFScore();
        if (diff == 0.0) {
            return 0;
        } else if (diff < 0) {
            return -1;
        } else {
            return 1;
        }
    }
}
