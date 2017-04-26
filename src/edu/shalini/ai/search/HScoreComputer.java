package edu.shalini.ai.search;

import edu.shalini.ai.map.Node;

public interface HScoreComputer {
    double computeHScore(Node node, String prefix);
}
