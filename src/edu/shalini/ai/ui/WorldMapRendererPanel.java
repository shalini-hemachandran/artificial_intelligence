package edu.shalini.ai.ui;

import edu.shalini.ai.Constants;
import edu.shalini.ai.map.Edge;
import edu.shalini.ai.map.Node;
import edu.shalini.ai.map.WorldMap;
import edu.shalini.ai.search.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

public class WorldMapRendererPanel extends JPanel implements MouseListener {
    private static final int NODE_DIAMETER = 10;
    private static final Color NODE_COLOR = Color.BLACK;
    private static final Color SOURCE_NODE_COLOR = Color.GREEN;
    private static final Color TARGET_NODE_COLOR = Color.RED;
    private static final Color PATH_EDGE_COLOR = Color.BLUE;
    private static final Color EXPANDED_NODE_COLOR = Color.ORANGE;

    private WorldMap worldMap;
    private String sourceNodeName;
    private final Set<String> targetNodeNames;
    private List<Node> shortestPath;
    private List<Double> shortestPathGScores;
    private Set<Node> expandedNodes;
    private final MainFrame parentFrame;

    public WorldMapRendererPanel(final MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.worldMap = WorldMap.createEmptyWorldMap();
        this.sourceNodeName = null;
        this.targetNodeNames = new HashSet<>();
        this.shortestPath = new ArrayList<>();
        this.shortestPathGScores = new ArrayList<>();
        this.expandedNodes = new HashSet<>();

        setPreferredSize(
                new Dimension(Constants.WORLD_MAP_RENDER_PANEL_WIDTH, Constants.WORLD_MAP_RENDER_PANEL_HEIGHT));
        setBorder(BorderFactory.createLineBorder(Color.BLUE));
        addMouseListener(this);
    }

    public void replaceWorldMap(final WorldMap worldMap) {
        this.worldMap = worldMap;
        this.sourceNodeName = null;
        this.targetNodeNames.clear();
        this.shortestPath.clear();
        this.shortestPathGScores.clear();
        this.expandedNodes.clear();
        repaint();
    }

    public void restoreWorldMap() {
        this.shortestPath.clear();
        this.shortestPathGScores.clear();
        this.expandedNodes.clear();
        repaint();
    }

    public Search.Result paintShortestPath(final int algoIndex) {
        worldMap.clearStates();
        final Search search = new Search();

        HScoreComputer computer;
        switch(algoIndex) {
            case 0:
                computer = new ZeroHScoreComputer();
                break;
            case 1:
                computer = new FarthestTargetHScoreComputer(worldMap);
                break;
            case 2:
                computer = new NearestTargetHScoreComputer(worldMap);
                break;
            default:
                throw new IllegalArgumentException("Invalid algo selection.");
        }

        final Set<Node> targetNodes = new HashSet<>();
        for (final String targetNodeName : targetNodeNames) {
            targetNodes.add(worldMap.getNodeByName(targetNodeName));
        }

        final Search.Result result = search.findShortestPath(worldMap.getNodeByName(sourceNodeName), targetNodes,
                computer);
        this.shortestPath = new ArrayList<>(result.getShortestPath());
        this.shortestPathGScores = new ArrayList<>(result.getShortestPathGScores());
        this.expandedNodes = new HashSet<>(result.getExpandedNodes());

        repaint();

        return result;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(1));
        final int radius = NODE_DIAMETER / 2;

        // Paint the nodes.
        g2d.setColor(NODE_COLOR);
        for (final Node node : worldMap.getNodes()) {
            g2d.fillOval(node.getX() - radius, node.getY() - radius, NODE_DIAMETER, NODE_DIAMETER);
            // Draw an "alreadyWaited indicator" if the node has a value set for it.
            if (node.getAlreadyWaited() > 0) {
                g2d.drawString("+" + node.getAlreadyWaited() + "s", node.getX() - radius, node.getY() - 2*radius);
            }
            for (final Edge edge : node.getIncidentEdges()) {
                final Node fromNode = edge.getFromNode();
                final Node toNode = edge.getToNode();
                g2d.drawLine(fromNode.getX(), fromNode.getY(), toNode.getX(), toNode.getY());
            }
        }

        // Paint the expanded nodes.
        g2d.setColor(EXPANDED_NODE_COLOR);
        for (final Node node : expandedNodes) {
            g2d.fillOval(node.getX() - radius, node.getY() - radius, NODE_DIAMETER, NODE_DIAMETER);
        }

        // Paint the source node.
        g2d.setColor(SOURCE_NODE_COLOR);
        if (this.sourceNodeName != null) {
            final Node sourceNode = worldMap.getNodeByName(sourceNodeName);
            g2d.fillOval(sourceNode.getX() - radius, sourceNode.getY() - radius,
                    NODE_DIAMETER, NODE_DIAMETER);
        }

        // Paint the target nodes.
        g2d.setColor(TARGET_NODE_COLOR);
        for (final String targetNodeName : this.targetNodeNames) {
            final Node targetNode = worldMap.getNodeByName(targetNodeName);
            g2d.fillOval(targetNode.getX() - radius, targetNode.getY() - radius, NODE_DIAMETER, NODE_DIAMETER);
        }

        // Paint the shortest path.
        g2d.setColor(PATH_EDGE_COLOR);
        g2d.setStroke(new BasicStroke(5));
        Node prevNode = null;
        for (final Node currNode : this.shortestPath) {
            if (prevNode != null) {
                g2d.drawLine(prevNode.getX(), prevNode.getY(), currNode.getX(), currNode.getY());
            }
            prevNode = currNode;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (this.shortestPath.isEmpty()) {
            handleInitialClicks(e);
        } else {
            handlePostClicks(e);
        }
    }

    private void handleInitialClicks(MouseEvent e) {
        final Node clickedNode = getClickedNode(e);

        if (clickedNode != null) {
            if (this.sourceNodeName == null) {
                this.sourceNodeName = clickedNode.getName();
                this.parentFrame.setSelectTargetNodesMode(0);
            } else if (!this.sourceNodeName.equals(clickedNode.getName())
                    && !this.targetNodeNames.contains(clickedNode.getName())) {
                this.targetNodeNames.add(clickedNode.getName());
                this.parentFrame.setSelectTargetNodesMode(targetNodeNames.size());
            }
        }

        repaint();
    }

    private void handlePostClicks(MouseEvent e) {
        final Node clickedNode = getClickedNode(e);
        if (clickedNode != null) {
            int clickPathIndex = -1;
            for (int i = 0; i < this.shortestPath.size(); ++i) {
                final Node pathNode = this.shortestPath.get(i);
                if (clickedNode.getName().equals(pathNode.getName())) {
                    clickPathIndex = i;
                    break;
                }
            }

            if (clickPathIndex != -1) {
                // If the last node in the path was clicked, ignore since there would be no target nodes otherwise.
                if (clickPathIndex < this.shortestPath.size() - 1) {
                    this.sourceNodeName = this.shortestPath.get(clickPathIndex).getName();

                    // Clear the alreadyWaited value for the soon-to-be non-targets.
                    for (int i = 0; i <= clickPathIndex; ++i) {
                        if (this.targetNodeNames.contains(this.shortestPath.get(i).getName())) {
                            worldMap.getNodeByName(this.shortestPath.get(i).getName()).clearAlreadyWaited();
                        }
                    }

                    final Set<String> newTargetNodeNames = new HashSet<>();
                    for (int i = clickPathIndex + 1; i < this.shortestPath.size(); ++i) {
                        if (this.targetNodeNames.contains(this.shortestPath.get(i).getName())) {
                            newTargetNodeNames.add(this.shortestPath.get(i).getName());
                        }
                    }
                    this.targetNodeNames.clear();
                    this.targetNodeNames.addAll(newTargetNodeNames);

                    // Set the alreadyWaited time.
                    final double alreadyWaited = this.shortestPathGScores.get(clickPathIndex);
                    for (final String targetNodeName : this.targetNodeNames) {
                        worldMap.getNodeByName(targetNodeName).addAlreadyWaited(alreadyWaited);
                    }

                    restoreWorldMap();
                    repaint();
                }
            } else {
                handleInitialClicks(e);
            }
        }
    }

    private Node getClickedNode(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // Find the node that was clicked.
        Node clickedNode = null;
        int radius = NODE_DIAMETER / 2;
        for (final Node node : worldMap.getNodes()) {
            if (node.getDistanceFrom(x, y) <= radius) {
                clickedNode = node;
                break;
            }
        }

        return clickedNode;
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
