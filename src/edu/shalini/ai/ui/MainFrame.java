package edu.shalini.ai.ui;

import edu.shalini.ai.Constants;
import edu.shalini.ai.map.WorldMap;
import edu.shalini.ai.search.Search;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private static final int MIN_NUMBER_OF_NODES = 2;
    private static final int MAX_NUMBER_OF_NODES = 500;
    private static final double DEFAULT_MIN_DISTANCE = 25;

    private JComboBox<Integer> noOfNodesSelector;
    private JTextField minDistanceField;
    private WorldMapRendererPanel worldMapRendererPanel;
    private JLabel instructionLabel;
    private JComboBox<String> algoBox;
    private JButton simulateButton;
    private JLabel numExpansionsLabel;
    private JLabel maxWaitingTimeLabel;

    /**
     * Instantiates the main frame which contains the map and user selections
     */
    public MainFrame() {
        super();

        setTitle("AI Project - Shalini Hemachandran");

        simulateButton = new JButton("Simulate");
        numExpansionsLabel = new JLabel("");
        maxWaitingTimeLabel = new JLabel("");

        //creates the map panel
        worldMapRendererPanel = new WorldMapRendererPanel(this);
        worldMapRendererPanel.replaceWorldMap(WorldMap.generateMap(MAX_NUMBER_OF_NODES, DEFAULT_MIN_DISTANCE,
                Constants.WORLD_MAP_RENDER_PANEL_WIDTH - 1, Constants.WORLD_MAP_RENDER_PANEL_HEIGHT - 1));

        setLayout(new FlowLayout());

        //creates the user selection panel
        final JPanel leftPanel = new JPanel();

        final JPanel generateMapPanel = new JPanel();
        generateMapPanel.add(new JLabel("(1)"));
        generateMapPanel.add(createOptionsPanel());
        JButton generateMapButton = createGenerateMapButton();
        generateMapPanel.add(generateMapButton);
        generateMapPanel.setLayout(new GridLayout(3, 1, 0, 10));

        leftPanel.add(generateMapPanel);
        leftPanel.add(createInstructionsPanel());
        leftPanel.add(createSimulatePanel());

        leftPanel.setLayout(new GridLayout(3, 1, 0, 50));
        add(leftPanel);

        add(worldMapRendererPanel);
        pack();

        setSelectSourceNodeMode();
    }

    /**
     * creates options for user selection
     * @return
     */
    private JPanel createOptionsPanel() {
        final JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(2, 2, 5, 10));
        optionsPanel.add(new JLabel("Number of nodes:"));

        noOfNodesSelector = new JComboBox<>();
        for (int i = MIN_NUMBER_OF_NODES; i <= MAX_NUMBER_OF_NODES; ++i) {
            noOfNodesSelector.addItem(i);
        }
        noOfNodesSelector.setSelectedIndex(noOfNodesSelector.getItemCount() - 1);
        optionsPanel.add(noOfNodesSelector);

        optionsPanel.add(new JLabel("Minimum distance between two nodes:"));

        minDistanceField = new JTextField();
        minDistanceField.setText(String.valueOf(DEFAULT_MIN_DISTANCE));
        optionsPanel.add(minDistanceField);

        return optionsPanel;
    }

    /**
     * on clicking the Generate map button, the user's inputs of number of nodes are supplied to the map generator panel
     * and a map is generated according to the inputs
     * @return
     */
    private JButton createGenerateMapButton() {
        final JButton generateMapButton = new JButton("Generate Map!");

        generateMapButton.addActionListener(e -> {
            clearResultLabels();
            final int noOfNodes = noOfNodesSelector.getItemAt(noOfNodesSelector.getSelectedIndex());
            double minDistance = 0;
            try {
                final String minDistanceFieldText = minDistanceField.getText();
                minDistance = Double.parseDouble(minDistanceFieldText);
            } catch(final NumberFormatException ignored) {
                JOptionPane.showMessageDialog(null, "Failed to parse the minimum distance value!",
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            worldMapRendererPanel.replaceWorldMap(WorldMap.generateMap(noOfNodes, minDistance,
                    Constants.WORLD_MAP_RENDER_PANEL_WIDTH - 1, Constants.WORLD_MAP_RENDER_PANEL_HEIGHT - 1));
            setSelectSourceNodeMode();
        });

        return generateMapButton;
    }

    private JPanel createInstructionsPanel() {
        final JPanel instructionsPanel = new JPanel();

        instructionLabel = new JLabel("(2) Select the source node by clicking on it.");
        instructionsPanel.add(instructionLabel);

        instructionsPanel.setLayout(new GridLayout(1, 1, 0, 10));

        return instructionsPanel;
    }

    private JPanel createSimulatePanel() {
        final JPanel algoSelectorPanel = new JPanel();
        algoSelectorPanel.setLayout(new GridLayout(1, 2, 5, 0));
        algoSelectorPanel.add(new JLabel("Select a heuristic function:"));
        algoBox = new JComboBox<>();
        for (final String algo : Constants.algos) {
            algoBox.addItem(algo);
        }
        algoSelectorPanel.add(algoBox);

        final JPanel simulatePanel = new JPanel();
        simulatePanel.setLayout(new GridLayout(5, 1, 0, 0));
        simulatePanel.add(new JLabel("(3)"));
        simulatePanel.add(algoSelectorPanel);

        //calls the required method to render the shortest pat once the simulate button is clicked
        simulateButton.addActionListener(e -> {
            worldMapRendererPanel.restoreWorldMap();
            try {
                final Search.Result result = worldMapRendererPanel.paintShortestPath(algoBox.getSelectedIndex());
                setResultLabels(result);
            } catch (IllegalArgumentException ignored) {
                JOptionPane.showMessageDialog(null, "At least one destination is not reachable!",
                        "Destination not reachable", JOptionPane.WARNING_MESSAGE);
            }
        });
        simulatePanel.add(simulateButton);
        simulatePanel.add(numExpansionsLabel);
        simulatePanel.add(maxWaitingTimeLabel);

        return simulatePanel;
    }

    public void setResultLabels(final Search.Result result) {
        numExpansionsLabel.setText("Number of expansions: " + result.getNumExpansions());
        maxWaitingTimeLabel.setText("Maximum Waiting Time (s): " + result.getShortestPathCost());
    }

    public void clearResultLabels() {
        numExpansionsLabel.setText("");
        maxWaitingTimeLabel.setText("");
    }

    public void setSelectSourceNodeMode() {
        instructionLabel.setText("(2) Select the source node by clicking on it.");
        simulateButton.setEnabled(false);
    }

    public void setSelectTargetNodesMode(int noOfTargetNodes) {
        instructionLabel.setText("(2) Add target nodes by clicking on them.");
        if (noOfTargetNodes > 0) {
            simulateButton.setEnabled(true);
        }
    }
}
