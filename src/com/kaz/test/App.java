package com.kaz.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jhlabs.image.WarpFilter;
import com.jhlabs.image.WarpGrid;

public class App {

	public static void main(String[] args) throws IOException {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				createAndShowGUI();
			}
		});

	}

	private static void createAndShowGUI() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(1, 1));
		frame.getContentPane().add(new WarpImageTest());
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}

class WarpImageTest extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage inputImage;
	private BufferedImage outputImage;
	private WarpGrid srcGrid;
	private WarpGrid dstGrid;
	private WarpFilter warpFilter;
	private static final int GRID_NUMBER = 10;
	private Graphics2D graphics;
	private int imageWidth;
	private int imageHeight;

	public WarpImageTest() {
		try {
			inputImage = ImageIO.read(new File("D:/Image/cats.png"));
			outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
			imageHeight = inputImage.getHeight();
			imageWidth = inputImage.getWidth();
			srcGrid = new WarpGrid(GRID_NUMBER, GRID_NUMBER, inputImage.getWidth(), inputImage.getHeight());
			dstGrid = new WarpGrid(GRID_NUMBER, GRID_NUMBER, inputImage.getWidth(), inputImage.getHeight());
			//
			// dstGrid.xGrid[6] += imageWidth / (2 * GRID_NUMBER);
			// dstGrid.yGrid[6] += imageHeight / (2 * GRID_NUMBER);
			//
			// srcGrid.xGrid[1] += imageWidth / (2 * GRID_NUMBER);
			// srcGrid.yGrid[1] += imageHeight / (2 * GRID_NUMBER);
			warpImage(1, 1, srcGrid.xGrid[1], srcGrid.yGrid[1]);
			drawLines(srcGrid, Color.BLACK);

		} catch (Exception e) {
			e.printStackTrace();
		}
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent paramMouseEvent) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				Point point = findIndex(e.getX(), e.getY());
				System.out.println("Clicked Point: " + e.getX() + ", " + e.getY());
				System.out.println("Grid index: " + point.x + " , Point2: " + point.y);
				if (point.x == GRID_NUMBER * GRID_NUMBER)
					point.x--;
				if (point.y == GRID_NUMBER * GRID_NUMBER)
					point.y--;
				int dx = imageWidth / GRID_NUMBER;
				int dy = imageHeight / GRID_NUMBER;
				System.out.println("Original: Grid Point: " + srcGrid.xGrid[point.x] + " " + srcGrid.yGrid[point.y]);

				// dstGrid.xGrid[point.x] = (int) srcGrid.xGrid[point.x] + dx /
				// 2;
				// dstGrid.yGrid[point.y] = (int) srcGrid.yGrid[point.y] + dy /
				// 2;
				System.out.println("Changed:");
				System.out.println(dstGrid.xGrid[point.x] + " " + dstGrid.yGrid[point.y]);
				warpImage(point.x, point.y, e.getX(), e.getY());
				drawLines(dstGrid, Color.BLUE);
				
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}
		});
	}

	@Override
	protected void paintComponent(Graphics g) {
		graphics = (Graphics2D) g;
		if (outputImage != null) {
			g.drawImage(outputImage, 0, 0, outputImage.getWidth(), outputImage.getHeight(), null);
		}
	}

	public void warpImage(int x, int y, float xVal, float yVal) {
		dstGrid.xGrid[x] = xVal;
		dstGrid.yGrid[y] = yVal;
		warpFilter = new WarpFilter(srcGrid, dstGrid);
		warpFilter.filter(inputImage, outputImage);
	}

	public Dimension getPreferredSize() {
		return new Dimension(inputImage.getWidth(), inputImage.getHeight());
	}

	public void drawLines(WarpGrid srcGrid, Color color) {
		graphics = (Graphics2D) outputImage.getGraphics();
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(1));
		int index = 0;
		for (int i = 0; i < GRID_NUMBER; i++)
			for (int j = 0; j < GRID_NUMBER - 1; j++) {
				index = i * GRID_NUMBER + j;
				int x1 = (int) srcGrid.xGrid[index];
				int y1 = (int) srcGrid.yGrid[index];
				int x2 = (int) srcGrid.xGrid[index + 1];
				int y2 = (int) srcGrid.yGrid[index + 1];
				graphics.drawLine(x1, y1, x2, y2);
				//System.out.println("x1: " + x1 + ", y1: " + y1 + ", x2: " + x2 + ", y2: " + y2);
			}
		index = 0;
		for (int i = 0; i < GRID_NUMBER; i++)
			for (int j = 0; j < GRID_NUMBER - 1; j++) {
				index = j * GRID_NUMBER + i;
				int x1 = (int) srcGrid.xGrid[index];
				int y1 = (int) srcGrid.yGrid[index];
				int x2 = (int) srcGrid.xGrid[index + GRID_NUMBER];
				int y2 = (int) srcGrid.yGrid[index + GRID_NUMBER];
				graphics.drawLine(x1, y1, x2, y2);
				// System.out.println("x1: " + x1 + ", y1: " + y1 + ", x2: " +
				// x2 + ", y2: " + y2);
			}

	}

	public Point findIndex(int x, int y) {
		Point point = new Point();
		int minDistance = 214748364;
		for (int i = 0; i < GRID_NUMBER * GRID_NUMBER; i++)
			for (int j = 0; j < GRID_NUMBER * GRID_NUMBER; j++) {
				int calculatedDistance = (int) Math.sqrt(Math.pow((x - srcGrid.xGrid[i]), 2)
						+ Math.pow((y - srcGrid.yGrid[j]), 2));
				if (calculatedDistance < minDistance && i == j) {
					minDistance = calculatedDistance;
					point.x = i;
					point.y = j;
				}
			}
		return point;
	}
}