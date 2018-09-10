package TournamentSystem.tools;

import TournamentSystem.TSAgentManager;

import javax.swing.*;

/**
 * This class stores all heatmap data required for multi player tournament visualization.
 * An instance of this class gets passed from {@link TSAgentManager} to {@link TournamentSystem.TSResultWindow}
 * to hand over the processed data for rendering.
 *
 * @author Felix Barsnick, University of Applied Sciences Cologne, 2018
 */
public class TSHeatmapDataTransfer {
    /**
     * heatmap with agent scores sorted by agent ID
     */
    public ImageIcon scoreHeatmap;
    /**
     * advanced analysis heatmap #1, is Wab = Wba true?
     */
    public ImageIcon scoreHeatmapA1;
    /**
     * advanced analysis heatmap #2, is Wab = 1-Wba true?
     */
    public ImageIcon scoreHeatmapA2;
    /**
     * advanced analysis heatmap #3, are both Wab = Wba and Wab = 1-Wba true?
     */
    public ImageIcon scoreHeatmapA3;
    /**
     * heatmap with agent scores sorted by agent score
     */
    public ImageIcon scoreHeatmapSorted;
}
