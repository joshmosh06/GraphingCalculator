
//ECE 309  March 26th, 2018  Team Project: Josh Hofmann, Iason Katsaros, Randy Paluszkiewicz, Brian Cuthrell

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class RefreshGraphPanel extends JPanel implements MouseListener {

	// GUI Globals
	JFrame graphWindow = new JFrame();
	double[] xCoords;
	double[] yCoords;
    private static DecimalFormat df2 = new DecimalFormat(".##");
	int xPixelCoords[] = new int[11];
	int yPixelCoords[] = new int[11];
	int yPixelFixedCoords[] = new int[11];
	boolean drawPointValues = false;
	Point selectedPoint = new Point();
	int pointNum = 0;
	public RefreshGraphPanel(GraphingCalculator gc, String expression, double[] xValues, double[] yValues)
			throws IllegalArgumentException {

		// Instance Variables
		String workingExpression = expression;
		xCoords = xValues;
		yCoords = yValues;

		String[] xString = new String[xValues.length];
		String[] yString = new String[yValues.length];

		for (int i = 0; i < xValues.length; i++) {
			xString[i] = String.valueOf(xValues[i]);
			yString[i] = String.valueOf(yValues[i]);
			System.out.println("(" + xString[i] + "," + yString[i] + ")");
		}

		// Create the Graph GUI
		// ===================================================================================
		graphWindow.getContentPane().add(this, "Center");
		graphWindow.addMouseListener(this);
		graphWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		graphWindow.setTitle(workingExpression + "   hold down the mouse on a point to see its x and y value");
		graphWindow.setLocation(800, 0); // xLocation, yLocation
		graphWindow.setSize(1000, 1000); // width, height
		graphWindow.setVisible(true); // show it
		// ===================================================================================
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

		@Override
	public void mousePressed(MouseEvent arg0) {
		int x = arg0.getX() ;
		int y = arg0.getY()-20 ;
		int i = 0;
		for (int xloc : xPixelCoords) {
			if(x - 20 <= xloc && x+20 >= xloc) {
				int yloc = yPixelFixedCoords[i];
				if(y - 20 <= yloc && y + 20  >= yloc) {
					selectedPoint.x = xloc;
					selectedPoint.y = yloc;
					drawPointValues = true;
					pointNum = i;
					repaint();
					break;
				}
			}
			i++;
		}

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		drawPointValues =false;
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		
		int windowWidth = graphWindow.getWidth();
		int windowHeight = graphWindow.getHeight();
		g.clearRect(0, 0, windowWidth, windowHeight);
		int xScale = (windowWidth - 200) / 10;
		int x = 0;
		for (int i = 100; i < (100 + xScale * 11); i += xScale) {
			g.drawString("|", i, windowHeight - 63);
			g.drawString(Double.toString(xCoords[x]), i - 10, windowHeight - 50);
			xPixelCoords[x] = i;
			x++;
		}
		
		int yPixelRange = windowHeight - 200;
		int yPixelsPerTick = (yPixelRange) / 10;
		double yValueRange = getMaxValue(yCoords) - getMinValue(yCoords);
		double yValuesPerTick = (yValueRange) / 10;
		for (int i = 0; i < 11; i++) {
			g.drawString("--", 90, windowHeight - 100 - yPixelsPerTick*i);
			g.drawString(df2.format(getMinValue(yCoords) + (yValuesPerTick*i)), 40, (windowHeight - 100 - yPixelsPerTick*i));
		}
		
		double yValueToPixelScale = yPixelRange / yValueRange;
		int yPixelCoords[] = new int[11];
		for (int i = 0; i < 11; i++) {
			yPixelCoords[i] = (int) (yCoords[i] * yValueToPixelScale);
		}
		int fixYPixel = 100 - getMinValue(yPixelCoords);
		for (int i = 0; i < 11; i++) {
			g.drawOval(xPixelCoords[i], (windowHeight - yPixelCoords[i] - fixYPixel), 4, 4);
			yPixelFixedCoords[i]= windowHeight - yPixelCoords[i] - fixYPixel;
		}
      	for (int i = 0; i<10; i++)
		{
			g.drawLine(xPixelCoords[i], windowHeight - yPixelCoords[i] - fixYPixel, xPixelCoords[i+1], windowHeight - yPixelCoords[i+1] - fixYPixel);
		}
		//code to implement drawing values to screen
		if(drawPointValues) {
			g.drawString("X : " + xCoords[pointNum] + " , Y : " + yCoords[pointNum], (int) Math.round(selectedPoint.getX() + 20), (int) Math.round(selectedPoint.getY()));
		}
	}

	public double getMaxValue(double[] numbers) {
		double maxValue = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] > maxValue) {
				maxValue = numbers[i];
			}
		}
		return maxValue;
	}
	
	public int getMaxValue(int[] numbers) {
		int maxValue = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] > maxValue) {
				maxValue = numbers[i];
			}
		}
		return maxValue;
	}

	public double getMinValue(double[] numbers) {
		double minValue = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] < minValue) {
				minValue = numbers[i];
			}
		}
		return minValue;
	}
	
	public int getMinValue(int[] numbers) {
		int minValue = numbers[0];
		for (int i = 1; i < numbers.length; i++) {
			if (numbers[i] < minValue) {
				minValue = numbers[i];
			}
		}
		return minValue;
	}
}
