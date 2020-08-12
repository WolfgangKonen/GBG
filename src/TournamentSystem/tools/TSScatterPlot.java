package TournamentSystem.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import TournamentSystem.TSAgent;
import TournamentSystem.TSAgentManager;
import TournamentSystem.TSResultStorage;
import tools.Types;
import tools.Utils;

/**
 * Class with three static methods to create the scatter plot "AgentScore vs Time". 
 * <p>
 * This class was separated from TSAgentManager to make the dependencies in the code clearer.
 */
public class TSScatterPlot  {
	
    // https://www.boraji.com/jfreechart-scatter-chart-example
	
    // Create dataset
    public static XYSeriesCollection createDataset(TSAgentManager agtManager, TSResultStorage results) {
    	XYSeriesCollection dataset = new XYSeriesCollection();

        int[] selectedAgents2 = agtManager.getIDAgentsSelected();
        for (int i=0; i<selectedAgents2.length; i++) {
            ArrayList<Double> medianTimes = new ArrayList<>();

            for (int gms=0; gms<results.timeStorage.length; gms++) { // spiele
                for (int cpl=0; cpl<results.timeStorage[0].length; cpl++) { // hin+rückrunde
                    for (int agt=0; agt<agtManager.getNumPlayers(); agt++) { // agent 1+2
                        if (results.gamePlan[gms][cpl] == selectedAgents2[i]) {
                            if (agtManager.getNumPlayers() > 1) {
                                double medianRoundTimeMS = results.timeStorage[gms][cpl].getMedianRoundTimeMS();
                                if (medianRoundTimeMS > -1) // avoid missing measurements marked with -1 value
                                    medianTimes.add(medianRoundTimeMS);
                            } else {
                                medianTimes.add(results.timeStorage[gms][cpl].getMedianTimeForGameMS());
                            }
                        }
                    }
                }
            }

            double median = Utils.calculateMedian(medianTimes);

            TSAgent tsAgt = results.mAgents.get(selectedAgents2[i]);
            //XYSeries series1 = new XYSeries(tsAgt.getName());
            XYSeries series1 = new XYSeries(agtManager.getNamesAgentsSelected()[i]);
            if (agtManager.getNumPlayers()==1)
                series1.add(median, tsAgt.getAverageSinglePlayScore());
            else
                series1.add(median, tsAgt.getAgentScore());
            dataset.addSeries(series1);
        } // for
        
        return dataset;
    }

    public static JFreeChart createScatterPlot(XYSeriesCollection dataset, boolean hasLogarithmicX, int numPlayers) {
        String scXAxis = ""; // X Axis Label
        String scYAxis = ""; // Y Axis Label
    	switch (numPlayers) {
    	case 1: 
            scXAxis = "Median Move Time [ms]";
            scYAxis = "average Agent Game Score";
            break;
    	case 2: 
            scXAxis = "Median Episode Time [ms]"; // X Axis Label
            scYAxis = "Agent Score [WTL]"; // Y Axis Label
    		break;
    	default:
    		throw new RuntimeException("Case numPlayers = "+numPlayers+
    				" not handled in createScatterPlot()");
    	}

        JFreeChart scatterPlot = ChartFactory.createScatterPlot("", scXAxis, scYAxis, dataset);

        //Changes background color
        XYPlot plot = (XYPlot)scatterPlot.getPlot();
        plot.setBackgroundPaint(new Color(180, 180, 180));
        Font font = new Font("Arial", Font.BOLD, (int)(1.2*Types.GUI_HELPFONTSIZE));
        
        // Code for larger shapes (markers), adapted from http://www.jfree.org/forum/viewtopic.php?t=16014 
        // and http://www.jfree.org/forum/viewtopic.php?t=29751 :
        //
        int isize = 12;		// *** adjust this one parameter isize to change the size of all markers. ***
        					//     isize should be even (!)
        Shape[] shapes = createStandardSeriesShapes(isize);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        for (int i=0; i<shapes.length; i++) {
        	renderer.setSeriesShapesVisible( i, true );
            renderer.setSeriesShape( i, shapes[i] );        	
        }

        if (hasLogarithmicX) {
        	final NumberAxis domainAxis = new LogarithmicAxis(scXAxis);
        	domainAxis.setLabelFont(font);
        	plot.setDomainAxis(domainAxis);
        } else {
            //To change the lower bound of X-axis (for linear axis, to see the low-time symbols)
            NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
            xAxis.setLabelFont(font);
            //xAxis.setLowerBound(-1); // show x axis from -1 to move marker away from axis
            xAxis.setLowerBound(-1*dataset.getDomainUpperBound(false)*0.02); 
            // draw x axis from -2% to the left of total width to move marker at 0 away from y axis        	
        }
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setLabelFont(font);
        
        return scatterPlot;	
	}
    
    /**
     * Creates an array of standard shapes to display for the items in series on charts.
     * <p>
     * Adapted from  <a href="http://www.jfree.org/forum/viewtopic.php?t=16014">
     * 					http://www.jfree.org/forum/viewtopic.php?t=16014</a>.
     * 
     * @param isize a common size parameter for all shapes. Should be even for best results.
     * @return The array of shapes.
     */
    public static Shape[] createStandardSeriesShapes(int isize) {

        Shape[] result = new Shape[10];

        double size = (double) isize;
        double delta = size / 2.0;
        int idelta = (int) delta; 
        Polygon po;

        // square
        result[0] = new Rectangle2D.Double(-delta, -delta, size, size);
        
        // circle
        result[1] = new Ellipse2D.Double(-delta, -delta, size, size);

        // up-pointing triangle
        po = new Polygon();
        po.addPoint(0, -idelta); po.addPoint(idelta, idelta); po.addPoint(-idelta, idelta);
        result[2] = po;

        // diamond
        po = new Polygon();
        po.addPoint(0, -idelta); po.addPoint(idelta, 0); po.addPoint(0, idelta); po.addPoint(-idelta, 0);
        result[3] = po;

        // horizontal rectangle
        result[4] = new Rectangle2D.Double(-delta, -delta / 2, size, size / 2);

        // down-pointing triangle
        po = new Polygon();
        po.addPoint(-idelta, -idelta); po.addPoint(idelta, -idelta); po.addPoint(0, idelta); 
        result[5] = po;

        // horizontal ellipse
        result[6] = new Ellipse2D.Double(-delta, -delta / 2, size, size / 2);

        // right-pointing triangle
        po = new Polygon();
        po.addPoint(-idelta, -idelta); po.addPoint(idelta, 0); po.addPoint(-idelta, idelta); 
        result[7] = po;

        // vertical rectangle
        result[8] = new Rectangle2D.Double(-delta / 2, -delta, size / 2, size);

        // left-pointing triangle
        po = new Polygon();
        po.addPoint(-idelta, 0); po.addPoint(idelta, -idelta); po.addPoint(idelta, idelta); 
        result[9] = po;

        return result;
    }    
}
