package games.Sim.Gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
//import java.awt.Image;
//import java.io.File;
//import java.io.IOException;
//import javax.imageio.ImageIO;

import javax.swing.JFrame;
import javax.swing.JPanel;

import games.Sim.GameBoardSim;
import games.Sim.Node;
import tools.Types;

/**
 * This class does all the drawing jobs for {@link GameBoardSimGui}.
 * 
 * @author Percy Wuensch, Wolfgang Konen, TH Koeln, 2019-2020
 */
public class BoardPanel_OLD extends JPanel {
	
	Point[] circles;
	Point2[] lines;
	private Node[] nodes;			// do we really need a copy of the nodes in here?
	private int inputNode1, inputNode2;
	/**
	 * the nodes of a losing triangle (values in [1,...,K]). All -1 if no loosing triangle yet.
	 */
	private int[] lastNodes = {-1,-1,-1}; 
	GameBoardSim m_gb;
	int CIRCLE_RADIUS = 15;
	private boolean showValueOnGameBoard = false;
	private Color colTHK1 = new Color(183,29,13);	// dark red
	private Color colTHK2 = new Color(255,137,0);	// orange
	private Color colTHK3 = new Color(162,0,162);	// dark magenta
	private Color colLightGray = new Color(200,200,200);
	
	BoardPanel_OLD(Node [] nodes, GameBoardSim gb)
	{
		this.setBounds(0, 0, 600, 400);
		this.setBackground(Color.gray);
		this.m_gb = gb;
		
		setupNodes(nodes.length);
		setupCircles(nodes.length);
		setupLines(nodes.length);
		inputNode1 = -1;
		inputNode2 = -1;
	}
	
	public void clearLastNodes() {
		for (int i=0; i<lastNodes.length; i++) lastNodes[i]=-1; 
	}
	
	private void setupNodes(int size)
	{
		nodes = new Node[size];
		for(int i = 0; i < nodes.length; i++)
			nodes[i] = new Node(size, i+1);
	}
	
	private int calculateCirclePositionX(int radius, int degree, int posX, int posY)
	{
		double t = Math.toRadians((double) degree);
		
		return (int)(posX + radius * Math.cos(t));
	}
	
	private int calculateCirclePositionY(int radius, int degree, int posX, int posY)
	{
		double t = Math.toRadians((double) degree);
		
		return (int)((posX + radius * Math.sin(t)) - posY);
	}
	
	/**
	 * Create {@code size} circle positions. A circle position is defined by the lower left corner
	 * of its bounding rectangle. Its radius is defined by member {@link #CIRCLE_RADIUS}.
	 * @param size
	 */
	private void setupCircles(int size)
	{
		int CENTERX = 275;
		int CENTERY = 115;
		int radius = 150;
		int degree = 360 / size;
		circles = new Point[size];
		
		for(int i = 0; i < size; i++)
			circles[i] = new Point(calculateCirclePositionX(radius, degree * i, CENTERX, CENTERY),
								   calculateCirclePositionY(radius, degree * i, CENTERX, CENTERY));
		
	}
	
	/**
	 * Return true, if mouse click (x,y) is inside circle i.
	 */
	boolean isInsideCircle(int i, int x, int y) {
		if (i>=0 && i<circles.length) 
			if (x > circles[i].getX() && x < circles[i].getX() + 2*CIRCLE_RADIUS && 
				y > circles[i].getY() && y < circles[i].getY() + 2*CIRCLE_RADIUS) 
					return true;
		return false;
	}
	
	void markLosingTriangle(int[] lNodes) {
		lastNodes = lNodes.clone();
		// the actual marking of the nodes is deferred to doDrawing(), because we need g2d
	}
	
	private void setupLines(int size)
	{
		lines = new Point2[size*(size-1)/2];
		for(int i = 0, k = 0; i < size - 1; i++)
			for(int j = i + 1; j < size; j++,k++)
			{
				lines[k] = new Point2(new Point(circles[i].getX()+CIRCLE_RADIUS,circles[i].getY()+CIRCLE_RADIUS), 
						              new Point(circles[j].getX()+CIRCLE_RADIUS,circles[j].getY()+CIRCLE_RADIUS));
			}
	}
	
    /**
     * Calculate the color for a specific action value (borrowed from HexUtils.calculateTileColor). 
     * This is the color assigned to a link if {@link #showValueOnGameBoard}{@code ==true}.<br>
     * Uses three color stops: RED for value -1, YELLOW for value 0, GREEN for value +1.
     * Colors for values between -1 and 0 are interpolated between RED and YELLOW.
     * Colors for values between 0 and +1 are interpolated between YELLOW and GREEN.
     *
     * @param actionValue value of the link (the action), which is a number from [-1,1]
     * @return color for the link
     */
    private Color calculateLinkColor(double actionValue) {
        float percentage = (float) Math.abs(actionValue);
        float remainder = 1 - percentage;

        Color colorLow;
        Color colorHigh;
        Color colorNeutral = Color.YELLOW;
        int red, blue, green;

        if (actionValue < 0) {
            colorLow = colorNeutral;
            colorHigh = Color.RED;
        } else {
            colorLow = colorNeutral;
            colorHigh = Color.GREEN;
        }

        red = Math.min(Math.max(Math.round(colorLow.getRed() * remainder
                + colorHigh.getRed() * percentage), 0), 255);
        green = Math.min(Math.max(Math.round(colorLow.getGreen() * remainder
                + colorHigh.getGreen() * percentage), 0), 255);
        blue = Math.min(Math.max(Math.round(colorLow.getBlue() * remainder
                + colorHigh.getBlue() * percentage), 0), 255);

        return new Color(red, green, blue, 255);
    }

	private void doDrawing(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(colTHK2);
		g2d.setStroke(new BasicStroke(3));
		
		// draw lines: 
		// i) all lines in empty color (last move thick), 
		// ii) (optional) action value coloring (last move thick), 
		// iii) overlay occupied lines in player color (normal width)
		int lastMove = m_gb.m_so.getLastMove();
		g2d.setColor(colTHK2);
		for (int k=0; k<lines.length; k++) {
			drawLine(g2d,k);		
			if (k==lastMove) drawLineThick(g2d,k);
		}
		
		if (this.showValueOnGameBoard) 
			if(m_gb.m_so.getStoredValues() != null) {
				for (int i=0; i<m_gb.m_so.getStoredValues().length; i++) {
					Types.ACTIONS act = m_gb.m_so.getStoredAction(i);
					int k = act.toInt();
					g2d.setColor(calculateLinkColor(m_gb.m_so.getStoredValues()[i]));
					drawLine(g2d,k);
					if (k==lastMove) drawLineThick(g2d,k);
				}		
			}
		
//		int v = 0;
		for(int i = 0, k = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++,k++)
			{
				// This code is not exactly what we want, because in case PLAY
				// we have also an actionValue for the last move just taken by the player.
				// Its coloring would be missing.
//				if(nodes[i].getLinkPlayerPos(j) == 0)
//				{
//					if(actionValues != null && v < actionValues.length)
//					{
//						//g2d.setColor(getColor(actionValues[v]));
//						g2d.setColor(calculateLinkColor(actionValues[v]));
//						v++;
//					}
//						
//					else
//						g2d.setColor(colTHK2);
//			
//				}
				
				if(nodes[i].getLinkPlayerPos(j) > 0) {		// if line is occupied by one of the players
					if(nodes[i].getLinkPlayerPos(j) == 1)
						g2d.setColor(Types.GUI_PLAYER_COLOR[0]);
					else if(nodes[i].getLinkPlayerPos(j) == 2)
						g2d.setColor(Types.GUI_PLAYER_COLOR[1]);
					else if(nodes[i].getLinkPlayerPos(j) == 3)
						g2d.setColor(Types.GUI_PLAYER_COLOR[2]);

					drawLine(g2d,k);
				}
			}
		}
		
		//draw circles (& mark circle borders of a losing triangle, if any)
		for(int i = 0; i < circles.length; i++)
		{
			// note that lastNodes values are \in [1,..,K] while i \in [0,K-1] (!)
			if(i == lastNodes[0]-1 || i == lastNodes[1]-1 || i == lastNodes[2]-1) {
				// mark the circle borders of a losing triangle in dark red:
				g2d.setStroke(new BasicStroke(8));
				g2d.setColor(colTHK1);		// dark red
				//System.out.println("lastNode i:"+i+"  [lastNodes[2]="+lastNodes[2]+"]");
			} else {
				// the normal mark
				g2d.setStroke(new BasicStroke(2));
				//g2d.setColor(colTHK2);	// invisible border
				g2d.setColor(colLightGray);	// light gray border
			}
			g2d.drawOval(circles[i].getX(), circles[i].getY(), 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
			if(i == inputNode1 || i == inputNode2)
				g2d.setColor(Color.darkGray);
			else
				g2d.setColor(colTHK2);
			
			g2d.fillOval(circles[i].getX(), circles[i].getY(), 2*CIRCLE_RADIUS, 2*CIRCLE_RADIUS);
		}
		
	}
	
	private void drawLine(Graphics2D g2d, int k) {
		g2d.drawLine(lines[k].getP1().getX(), lines[k].getP1().getY(), 
				     lines[k].getP2().getX(), lines[k].getP2().getY());			
	}
	
	private void drawLineThick(Graphics2D g2d, int k) {
		// First we calculate with (dx,dy) the vector pointing from P1 to P2.
		// Then the vector (-dy,dx) is perpendicular to (dx,dy) and has length len. 
		// Then (ox,oy) goes OFFSETWIDTH pixels 'up' in this perpendicular direction.
		// We draw a thick line by drawing two lines with offset (ox,oy) and (-ox,-oy) side-to-side
		// to the original line.
		int OFFSETWIDTH = 3;
		int x1=lines[k].getP1().getX();
		int y1=lines[k].getP1().getY();
		int x2=lines[k].getP2().getX();
		int y2=lines[k].getP2().getY();
		int dx=x2-x1;
		int dy=y2-y1;
		double len=Math.sqrt(dx*dx+dy*dy);
		int ox=(int)(-dy*OFFSETWIDTH/len);
		int oy=(int)(+dx*OFFSETWIDTH/len);
				
		g2d.drawLine(x1+ox, y1+oy, x2+ox, y2+oy); 
		g2d.drawLine(x1-ox, y1-oy, x2-ox, y2-oy); 
	}
	
	@Override
	public void paintComponent(Graphics g) 
	{
		 super.paintComponent(g);
		 doDrawing(g);
	}
	 
	public void toFront() 
	{
		super.setVisible(true);
	}

	public void setNodesCopy(Node [] nodes) 
	{
		for(int i = 0; i < nodes.length; i++)
			this.nodes[i].setLinksCopy(nodes[i].getLinks());
	}
	
	public void setInputNode1(int i)
	{
		inputNode1 = i;
	}

	public void setInputNode2(int i)
	{
		inputNode2 = i;
	}
	
	public void resetInputNodes()
	{
		inputNode1 = -1;
		inputNode2 = -1;
	}
	
	void setShowValueOnGameBoard(boolean show) {
		this.showValueOnGameBoard = show;
	}
	
	
}
