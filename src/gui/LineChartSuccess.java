package gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import games.Evaluator;
import games.XArenaButtons;


/**
 * Class LineChartSuccess is a convenient wrapper for some JFreeChart functionality. 
 * It extends XYSeriesCollection and as such XYSeries can be added to it. When points are 
 * added to this XYSeries objects, the LineChart will automatically change. 
 * Multiple lines and multiple markers are supported. 
 * 
 * Example usage:
 * 		LineChartSuccess lChart=new LineChartSuccess(...);
 * 
 * 		lChart.clear();
 *		XYSeries series = new XYSeries("Train X");		// "Train X" is the key of the XYSeries object
 *		lChart.addSeries(series);
 *
 *		double x,y;
 *		series.add(x,y);
 *		lChart.plot;
 * 
 * @author Wolfgang Konen /02/2015
 *
 */
public class LineChartSuccess extends XYSeriesCollection
{
	//XYSeriesCollection dat; 		// extends XYDataset
	JFreeChart chart;
	ChartFrame frame;
	boolean PLOTTRAINEVAL=true;
	boolean firstUpdate=true;
	
	public LineChartSuccess(String title, String xLab, String yLab, 
					 		boolean hasLines, boolean hasSymbols) 
	{
		init(title, xLab, yLab, hasLines, hasSymbols, "LineChartSuccess");	
	}
	
	public LineChartSuccess(String title, String xLab, String yLab, 
			 				boolean hasLines, boolean hasSymbols, String frameTitle) 
	{
		init(title, xLab, yLab, hasLines, hasSymbols, frameTitle);	
	}
	
	private void init(String title, String xLab, String yLab, 
				 	  boolean hasLines, boolean hasSymbols, String frameTitle)	
	{
		//dat = new XYSeriesCollection();  
		chart = ChartFactory.createXYLineChart(
				title,xLab,yLab,
				this,
				PlotOrientation.VERTICAL,				// x-axis is horizontal
				true, // legend?
				true, // tooltips?
				false // URLs?
			);
		frame = new ChartFrame(frameTitle, chart);
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		for (int i=0; i<4; i++) {
			renderer.setSeriesLinesVisible(i, hasLines);
			renderer.setSeriesShapesVisible(i, hasSymbols);
		}
		chart.getXYPlot().setRenderer(renderer);
		frame.setVisible(false);
		this.clear();
	}	
	
	public void initializeChartPlot(XArenaButtons xab, 
			Evaluator m_evaluatorQ, boolean doTrainEvaluation) {
		this.clearAndSetXY(xab);
		// "Q Eval" is the key of the XYSeries object, accessible as lChart.getSeries(0):
		this.addSeries(new XYSeries("Q Eval"));
        // set Y-axis of existing lChart according to the current Quick Eval Mode:
		this.setYAxisLabel(m_evaluatorQ.getPlotTitle());
        if (doTrainEvaluation && PLOTTRAINEVAL) {
			// "T Eval" is the key of the XYSeries object, accessible as lChart.getSeries(1):
        	this.addSeries(new XYSeries("T Eval"));	       	
        }
		
	}
	
	/**
	 * 
	 * @param gameNum
	 * @param m_evaluatorQ
	 * @param m_evaluatorT
	 * @param doTrainEvaluation
	 * @param visibleFlag	true: make window visible on every call; false: only on first call
	 */
	public void updateChartPlot(int gameNum, Evaluator m_evaluatorQ, 
			Evaluator m_evaluatorT, boolean doTrainEvaluation, boolean visibleFlag) {
		this.getSeries(0).add((double)gameNum, m_evaluatorQ.getLastResult());
		if (doTrainEvaluation && PLOTTRAINEVAL) {
			this.getSeries(1).add((double)gameNum, m_evaluatorT.getLastResult());
		}
		
		// this little logic allows with visibleFlag==false, that the plot window is only made 
		// visible on first update, but later it remains 'silent' and does NOT request the focus 
		// on every update during train:
		if (visibleFlag || firstUpdate) {
			this.plot();		
			firstUpdate=false;
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
		int y = +1; 
		if (xab.m_arena.m_tabs!=null) 
			y += xab.m_arena.m_tabs.getY() + xab.m_arena.m_tabs.getHeight() ;
		this.setLocation(x, y);  
	}
//	void addSeries(XYSeries series) {
//		dat.addSeries(series);
//	}
	
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
	
	public void destroy() {
		frame.setVisible(false);
		frame.dispose();
	}

}


