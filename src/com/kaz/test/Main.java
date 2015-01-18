package com.kaz.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jhlabs.image.WarpFilter;
import com.jhlabs.image.WarpGrid;

public class Main {

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
	private BufferedImage inputImage;
	private BufferedImage outputImage;
	private WarpGrid srcGrid;
	private WarpGrid dstGrid;
	private WarpFilter warpFilter;
	private static final int GRID_NUMBER = 10;
	private Graphics2D graphics;
	private int imageWidth;
	private int imageHeight;
	private int pointX;
	private int pointY;
	private float xVal;
	private float yVal;

	public WarpImageTest() {
		try {
			inputImage = ImageIO.read(new File("D:/Image/cats.png"));
			outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
			imageHeight = inputImage.getHeight();
			imageWidth = inputImage.getWidth();
			warpImage(51, 42, 100, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent paramMouseEvent) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				xVal = srcGrid.xGrid[getXValue(e.getX())];
				yVal = srcGrid.yGrid[getYValue(e.getY())];
				// System.out.println(srcGrid.xGrid[getXValue(e.getX())] + " " +
				// srcGrid.yGrid[getYValue(e.getY())]);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				System.out.println(getXValue(e.getX()) + " " + getYValue(e.getY()));
				if (getXValue(e.getX()) >= 0 && getXValue(e.getX()) < GRID_NUMBER * GRID_NUMBER
						&& getYValue(e.getY()) >= 0 && getYValue(e.getY()) < GRID_NUMBER * GRID_NUMBER) {
					warpImage(getYValue(e.getY()), getXValue(e.getX()), xVal, yVal);
					repaint();
				}

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
		srcGrid = new WarpGrid(GRID_NUMBER, GRID_NUMBER, inputImage.getWidth(), inputImage.getHeight());
		dstGrid = new WarpGrid(GRID_NUMBER, GRID_NUMBER, inputImage.getWidth(), inputImage.getHeight());
		// System.out.println("Old:\n\tX: " + dstGrid.xGrid[x] + ", Y: " +
		// dstGrid.yGrid[y]);
		// System.out.println("New:\n\tX: " + xVal + ", Y: " + yVal);
		dstGrid.xGrid[x] = xVal;
		dstGrid.yGrid[y] = yVal;
		warpFilter = new WarpFilter(srcGrid, dstGrid);
		warpFilter.filter(inputImage, outputImage);
		drawLines();
	}

	public Dimension getPreferredSize() {
		return new Dimension(inputImage.getWidth(), inputImage.getHeight());
	}

	public void drawLines() {
		graphics = outputImage.createGraphics();
		graphics.setColor(Color.BLACK);
		graphics.setStroke(new BasicStroke(1));
		for (int i = 0; i < GRID_NUMBER + 1; i++) {
			graphics.drawLine((imageWidth + 1) / GRID_NUMBER * i, 0, (imageWidth + 1) / GRID_NUMBER * i, imageHeight);
			graphics.drawLine(0, (imageHeight + 1) / GRID_NUMBER * i, imageWidth, (imageHeight + 1) / GRID_NUMBER * i);
		}

	}

	public int getXValue(int value) {
		return (int) ((float) value / ((float) imageWidth / (GRID_NUMBER * GRID_NUMBER)));
	}

	public int getYValue(int value) {
		return (int) ((float) value / ((float) imageHeight / (GRID_NUMBER * GRID_NUMBER)));
	}
}