package games.Sim;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import tools.Types;

public class BoardPanel extends JPanel {
	
	Point[] circles;
	Point2[] lines;
	Point[] circleEdge;
	private Node[] nodes;
	
	BoardPanel(Node [] nodes)
	{
		this.setBounds(0, 30, 600, 400);
		this.setBackground(Color.white);
		setupNodes(nodes.length);
		//this.nodes = nodes;
		
		setupCircles(nodes.length);
		//setupCircleEdges();
		setupLines(nodes.length);
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
	
	
	private void setupCircles(int size)
	{
		int radius = 150;
		int degree = 360 / size;
		circles = new Point[size];
		
		for(int i = 0; i < size; i++)
			circles[i] = new Point(calculateCirclePositionX(radius, degree * i, 280, 100),calculateCirclePositionY(radius, degree * i, 280, 100));
		
	}
	
	private void setupCircleEdges(int size)
	{
		circleEdge = new Point[size];

	}
	
	private int sum(int size)
	{
		int sum = 0;
		for(int i = 1; i < size; i++)
			sum += i;
		
		return sum;
	}
	
	private void setupLines(int size)
	{
		lines = new Point2[sum(size)];
		int i = 0;
		for(int j = 0; j < size - 1; j++)
			for(int k = j + 1; k < size; k++)
			{
				lines[i] = new Point2(new Point(circles[j].getX()+ 15,circles[j].getY()+15), new Point(circles[k].getX()+15,circles[k].getY()+15));
				i++;
			}
		
	}
	
	private void doDrawing(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);
		
		for(int i = 0; i < circles.length; i++)
		{
			g2d.drawOval(circles[i].getX(), circles[i].getY(), 30, 30);
			g2d.drawString(Integer.toString(i+1), circles[i].getX() + 12, circles[i].getY() + 20);
		}
		
	
		
		int k = 0;
		
		for(int i = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++)
			{
				if(nodes[i].getLinkPlayerPos(j) == 0)
					g2d.setColor(Color.black);
				else if(nodes[i].getLinkPlayerPos(j) == 1)
					g2d.setColor(Color.green);
				else if(nodes[i].getLinkPlayerPos(j) == 2)
					g2d.setColor(Color.blue);
				else
					g2d.setColor(Color.red);
				
				g2d.drawLine(lines[k].getP1().getX(), lines[k].getP1().getY(), lines[k].getP2().getX(), lines[k].getP2().getY());
				k++;
			}
		}
		
		
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

	public Node[] getNodes() {
		return nodes;
	}

	public void setNodes(Node[] nodes) {
		this.nodes = nodes;
	}

	public void setNodesCopy(Node [] nodes) 
	{
		for(int i = 0; i < nodes.length; i++)
			this.nodes[i].setLinksCopy(nodes[i].getLinks());
	}
	

}
