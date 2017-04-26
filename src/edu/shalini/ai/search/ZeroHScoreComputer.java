package edu.shalini.ai.search;

import edu.shalini.ai.map.Node;

public class ZeroHScoreComputer implements HScoreComputer {
    @Override
    public double computeHScore(Node node, String prefix) {
        return 0;
    }
}
