package edu.shalini.ai.search;

import edu.shalini.ai.map.Node;

/**
 * Interface defining a computeHScore method
 */
public interface HScoreComputer {
    double computeHScore(Node node, String prefix);
}
