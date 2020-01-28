package tools;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;

import controllers.PlayAgent;
import controllers.TD.ntuple2.TDNTuple2Agt;
import games.Evaluator;
import games.XArenaButtons;


/**
 * Class DeviationWeightsChart is a convenient wrapper for some JFreeChart functionality. 
 * It extends YIntervalSeriesCollection and as such YIntervalSeries can be added to it. When points are 
 * added to this YIntervalSeries objects, the LineChart will automatically change. 
 * Multiple lines and multiple markers are supported. 
 * 
 * Example usage:
 * 		DeviationWeightsChart wChart=new DeviationWeightsChart(...);
 * 
 * 		wChart.clear();
 *		YIntervalSeries series = new YIntervalSeries("Train X");		// "Train X" is the key of the YIntervalSeries object
 *		wChart.addSeries(series);
 *
 *		double x,y;
 *		series.add(x,y);
 *		wChart.plot;
 * 
 * @author Wolfgang Konen /06/2018
 *
 */
public class DeviationWeightsChart extends YIntervalSeriesCollection
{
	JFreeChart chart;
	ChartFrame frame;
	int plotwMode = 1;		// 0: no plot, 1: plot LUT weight distribution, 2: plot tcFactorArray distribution 
	String [] PLOTWTITLE = {"weight distribution", "TC factor distribution"};
	
	public DeviationWeightsChart(String title, String xLab, String yLab, 
					 		boolean hasLines, boolean hasSymbols) 
	{
		init(title, xLab, yLab, hasLines, hasSymbols, "DeviationWeightsChart");	
	}
	
	public DeviationWeightsChart(String title, String xLab, String yLab, 
			 				boolean hasLines, boolean hasSymbols, String frameTitle) 
	{
		init(title, xLab, yLab, hasLines, hasSymbols, frameTitle);	
	}
	
	private void init(String title, String xLab, String yLab, 
				 	  boolean hasLines, boolean hasSymbols, String frameTitle)	
	{
		chart = ChartFactory.createXYLineChart(
				title,xLab,yLab,
				this,
				PlotOrientation.VERTICAL,				// x-axis is horizontal
				true, // legend?
				true, // tooltips?
				false // URLs?
			);
		frame = new ChartFrame(frameTitle, chart);
		
        // get a reference to the plot for further customization...
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        DeviationRenderer renderer = new DeviationRenderer(true, false);
        renderer.setSeriesStroke(0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        renderer.setSeriesStroke(1, new BasicStroke(3.0f));
        renderer.setSeriesFillPaint(0, new Color(200, 200, 255));	// fill color for series0-deviation
        renderer.setSeriesFillPaint(1, new Color(255, 255, 255));	// fill color for series1-deviation
        															// (colors are plotted with transparency)
//		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
//		for (int i=0; i<4; i++) {
//			renderer.setSeriesLinesVisible(i, hasLines);
//			renderer.setSeriesShapesVisible(i, hasSymbols);
//		}
		chart.getXYPlot().setRenderer(renderer);
		frame.setVisible(false);
		this.clear();
	}	
	
	public void initializeChartPlot(XArenaButtons xab, PlayAgent pa, int plotWeightMode) {
		this.plotwMode = plotWeightMode;
		if (plotwMode>0 && (pa instanceof TDNTuple2Agt)) {
			chart.setTitle(PLOTWTITLE[plotwMode-1]);
			//chart.setSubtitles(List("s.th. with per")); // TODO
			this.clearAndSetXY(xab);
			// "YI0" is the key of the series object, accessible as wChart.getSeries(0):
			this.addSeries(new YIntervalSeries("YI0"));
			// "YI1" is the key of the series object, accessible as wChart.getSeries(1):
			this.addSeries(new YIntervalSeries("YI1"));
		} else {
			frame.setVisible(false);
		}
	}

	public void updateChartPlot(int gameNum, PlayAgent pa, double[] per) {
		if (plotwMode>0 && (pa instanceof TDNTuple2Agt)) {
			double[][] res = ((TDNTuple2Agt) pa).weightAnalysis(per);
			this.getSeries(0).add(gameNum, res[plotwMode][2], res[plotwMode][1], res[plotwMode][3]);
			this.getSeries(1).add(gameNum, res[plotwMode][2], res[plotwMode][0], res[plotwMode][4]);
			this.plot();
		}
	}
	
	/**
	 * Reset the line chart such that the x-axis and y-axis will adjust to the new data
	 * 
	 * (Without reset, a prior training with maxGameNum=2000 would cause a new training with 
	 * maxGameNum=1000 to only overwrite the first half of points - the points from 1000 to 
	 * 2000 would not change (!))
	 */
	public void clear() {
		this.removeAllSeries();
	}
	public void clearAndSetXY(XArenaButtons xab) {
		this.removeAllSeries();
		
		int x = xab.m_arena.m_ArenaFrame.getX() + xab.m_arena.m_ArenaFrame.getWidth() + 1;
		int y = xab.m_arena.m_tabs.getY() + xab.m_arena.m_tabs.getHeight() +1;
		this.setLocation(x, y);  
	}
	
	public void plot()
	{
		//create and display a frame...
		frame.pack();
		frame.setVisible(true);	
	}
	public void setLocation(int x, int y) {
		frame.setLocation(x, y);
	}
	
	public void setYAxisLabel(String yLab) {
		ValueAxis axis = chart.getXYPlot().getRangeAxis();
		axis.setLabel(yLab);
		chart.getXYPlot().setRangeAxis(axis);
	}
}


