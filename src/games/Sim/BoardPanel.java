package games.Sim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import tools.Types;

public class BoardPanel extends JPanel {
	
	Point[] circles;
	Point2[] lines;
	Point[] circleEdge;
	private Node[] nodes;
	private int inputNode1, inputNode2;
	Image img;
	double [] actionValues;
	
	BoardPanel(Node [] nodes)
	{
		this.setBounds(0, 0, 600, 400);
		this.setBackground(Color.white);
		
		setupNodes(nodes.length);
		setupCircles(nodes.length);
		setupLines(nodes.length);
		//getImages();
		inputNode1 = -1;
		inputNode2 = -1;
		
	}
	
	private void getImages()
	{
		try
		{
			img = ImageIO.read(new File("C:\\Users\\bashx\\Desktop\\green_btn.jpg"));
		}
		catch(IOException ex)
		{
			System.out.println("geht nicht lol");
		}
		
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
	
	private Color getColor(double d)
	{
		Color color;
		if(d <= 0.3)
			color = Color.red;
		if(d > 0.3 && d <= 0.7 )
			color = Color.yellow;
		else
			color = Color.green;
		
		return color;
	}
	
	private void doDrawing(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(3));
		
		
		// draw lines
		int k = 0;
		int v = 0;
		for(int i = 0; i < nodes.length -1 ; i++)
		{
			for(int j = 0; j < nodes.length - 1 - i; j++)
			{
				if(nodes[i].getLinkPlayerPos(j) == 0)
				{
					if(actionValues != null && actionValues[v] != Double.NaN)
						g2d.setColor(getColor(actionValues[v]));
					else
						g2d.setColor(Color.black);
					v++;
				}
				else if(nodes[i].getLinkPlayerPos(j) == 1)
					g2d.setColor(new Color(250,8,250));
				else if(nodes[i].getLinkPlayerPos(j) == 2)
					g2d.setColor(Color.blue);
				else
					g2d.setColor(new Color(8,222,250));
				
				g2d.drawLine(lines[k].getP1().getX(), lines[k].getP1().getY(), lines[k].getP2().getX(), lines[k].getP2().getY());
				k++;
			}
		}
		
		//draw circles
		g2d.setStroke(new BasicStroke(0));
		for(int i = 0; i < circles.length; i++)
		{
			if(i == inputNode1 || i == inputNode2)
				g2d.setColor(Color.GRAY);
			else
				g2d.setColor(Color.BLACK);
			
			g2d.fillOval(circles[i].getX(), circles[i].getY(), 30, 30);
			//g2d.drawImage(img,circles[i].getX(),circles[i].getY(),30,30,this);
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
	
	void setActionValues(double [] values)
	{
		actionValues = values;
	}
}
