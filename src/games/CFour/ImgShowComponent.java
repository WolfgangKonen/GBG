package games.CFour;

import java.awt.*;

import javax.swing.JPanel;

/**
 * Allows to create the graphics for single fields of the board and to replace
 * them by other images. Helper for {@link C4GameGui}
 * 
 * @author Markus Thill
 * 
 * @see C4GameGui
 */
public class ImgShowComponent extends JPanel {

	private static final long serialVersionUID = 1L;
	private Image img = null;
	private static Image[] imgArr = null;

	public static final int EMPTY0 = 0;
	public static final int EMPTY_M = 1;
	public static final int YELLOW_M = 2;
	public static final int RED_M = 3;
	public static final int EMPTY_R = 4;
	public static final int EMPTY1 = 5;
	public static final int YELLOW = 6;
	public static final int RED = 7;
	// the following image files are in GBG/resources/: 
	private static final String[] imgPath = { "empty.png", "empty_m.png",
			"yellow_m.png", "red_m.png", "empty_r.png", "empty.png",
			"yellow.png", "red.png" };
	private int index = -1;

	public ImgShowComponent(int index) {
		this.index = index;
		if (imgArr==null) {
			imgArr = new Image[imgPath.length];
			for (int i=0; i<imgPath.length; i++) {
				String str = imgPath[i];
				 // Load the images as resources from dir GBG/resources. To make this work, have 
				 //	   <classpathentry excluding="**/*.java" kind="src" path="resources"/>
				 // in your file .classpath (Eclipse) or have "resources" in your CLASSPATH environment variable
				java.net.URL url = getClass().getResource("/"+str);
				imgArr[i] = getToolkit().createImage(url);
				if (url==null){
					System.out.println("ImgShowComponent - ERROR :: could not load GUI images, did you set the resources folder correctly in buildpath?");
				}
			}
		}
		img = imgArr[index];
		//
		// --- seems to be not necessary anymore
		//
//		MediaTracker mt = new MediaTracker(this);
//		mt.addImage(img, 0); 
//		try {
//			mt.waitForAll();
//		} catch (InterruptedException ex) {
//		}
	}

	public static ImgShowComponent replaceImg(ImgShowComponent oldImg,int newIndex) {
		if (oldImg != null) {
			if (oldImg.index == newIndex)
				return oldImg;
			if ((oldImg.index == EMPTY0 || oldImg.index == EMPTY1)
					&& (newIndex == EMPTY0 || newIndex == EMPTY1))
				return oldImg;
		}
		return new ImgShowComponent(newIndex);
	}
	
	public ImgShowComponent replaceImg2(int newIndex) {
		img = imgArr[newIndex];
		return this;
	}
	
	public void paint(Graphics g) {
		g.drawImage(img, 0, 0, this);
	}

	public Dimension getPreferredSize() {
		return new Dimension(img.getWidth(this), img.getHeight(this));
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public int getIndex() {
		return index;
	}
}