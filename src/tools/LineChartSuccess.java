package tools;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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
		this.clear();
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
		
		int x = xab.m_game.m_TicFrame.getX() + xab.m_game.m_TicFrame.getWidth() + 1;
		int y = xab.m_game.m_tabs.getY() + xab.m_game.m_tabs.getHeight() +1;
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
}


