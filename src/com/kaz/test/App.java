package com.kaz.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
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
	private static final int GRID_NUMBER = 20;
	private Graphics2D graphics;
	private Point clickedPoint;
	private Point releasedPoint;
	private List<Point> nearestPoints;
	private int radii = 100;
	private int radius = radii / 2;
	private JButton circleButton;
	private JButton polygonButton;
	private boolean isPolygon;
	private boolean drawNow;
	private List<Integer> xPoints;
	private List<Integer> yPoints;
	private Polygon polygon;

	public WarpImageTest() {
		try {
			circleButton = new JButton("Circle");
			polygonButton = new JButton("Polygon");
			this.add(circleButton);
			this.add(polygonButton);
			inputImage = ImageIO.read(new File("D:/Image/image.jpg"));
			outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
			srcGrid = new WarpGrid(GRID_NUMBER, GRID_NUMBER, inputImage.getWidth(), inputImage.getHeight());
			dstGrid = new WarpGrid(GRID_NUMBER, GRID_NUMBER, inputImage.getWidth(), inputImage.getHeight());
			warpImage(1, 1, srcGrid.xGrid[1], srcGrid.yGrid[1]);
			drawLines(dstGrid, Color.BLACK);

		} catch (Exception e) {
			e.printStackTrace();
		}
		circleButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				isPolygon = false;
			}
		});
		polygonButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				isPolygon = true;
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				clickedPoint = new Point(e.getX(), e.getY());
				if (!isPolygon) {
					warpImage(1, 1, dstGrid.xGrid[1], dstGrid.yGrid[1]);
					Point circlePoint = new Point(e.getX() - radius, e.getY() - radius);
					drawCircle(circlePoint, radii, Color.WHITE);
					drawLines(dstGrid, Color.BLACK);
					repaint();
				} else {
					if (!drawNow) {
						xPoints = new ArrayList<Integer>();
						yPoints = new ArrayList<Integer>();
						xPoints.add(clickedPoint.x);
						yPoints.add(clickedPoint.y);
					} else {

					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				warpImage(1, 1, dstGrid.xGrid[1], dstGrid.yGrid[1]);
				inputImage = outputImage;
				outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				srcGrid = new WarpGrid(GRID_NUMBER, GRID_NUMBER, inputImage.getWidth(), inputImage.getHeight());
				dstGrid = new WarpGrid(GRID_NUMBER, GRID_NUMBER, inputImage.getWidth(), inputImage.getHeight());
				warpImage(1, 1, dstGrid.xGrid[1], dstGrid.yGrid[1]);
				drawLines(dstGrid, Color.BLACK);
				if (isPolygon) {
					if (!drawNow)
						drawPolygon();
					drawNow = !drawNow;
				}
				repaint();
			}

		});
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (isPolygon) {
					if (drawNow) {
						releasedPoint = new Point(e.getX(), e.getY());
						int deltaX = releasedPoint.x - clickedPoint.x;
						int deltaY = releasedPoint.y - clickedPoint.y;
						clickedPoint.x = releasedPoint.x;
						clickedPoint.y = releasedPoint.y;
						nearestPoints = new ArrayList<Point>();
						findPointsInsidePolygon();
						for (Point p : nearestPoints) {
							dstGrid.xGrid[p.x] = dstGrid.xGrid[p.x] + deltaX;
							dstGrid.yGrid[p.y] = dstGrid.yGrid[p.y] + deltaY;
						}
						warpImage(1, 1, dstGrid.xGrid[1], dstGrid.yGrid[1]);
						
					} else {
						releasedPoint = new Point(e.getX(), e.getY());
						xPoints.add(releasedPoint.x);
						yPoints.add(releasedPoint.y);
						
					}
					drawPolygon();
					drawLines(dstGrid, Color.BLACK);
					repaint();

				} else {
					releasedPoint = new Point(e.getX(), e.getY());
					Point circlePoint = new Point(e.getX() - radius, e.getY() - radius);
					Point point = findIndex(clickedPoint.x, clickedPoint.y);

					int deltaX = releasedPoint.x - clickedPoint.x;
					int deltaY = releasedPoint.y - clickedPoint.y;
					clickedPoint.x = releasedPoint.x;
					clickedPoint.y = releasedPoint.y;
					nearestPoints = new ArrayList<Point>();
					findAllNearestPoints(point, radius);
					for (Point p : nearestPoints) {
						dstGrid.xGrid[p.x] = dstGrid.xGrid[p.x] + deltaX;
						dstGrid.yGrid[p.y] = dstGrid.yGrid[p.y] + deltaY;
					}
					warpImage(1, 1, dstGrid.xGrid[1], dstGrid.yGrid[1]);
					drawLines(dstGrid, Color.BLACK);
					drawCircle(circlePoint, radii, Color.WHITE);
					repaint();
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {

				// warpImage(1, 1, dstGrid.xGrid[1], dstGrid.yGrid[1]);
				// drawCircle(new Point(e.getX() - radius, e.getY() - radius),
				// radii, Color.RED);
				// repaint();
			}
		});
	}

	public void drawCircle(Point point, int radius, Color color) {
		graphics = (Graphics2D) outputImage.createGraphics();
		graphics.setStroke(new BasicStroke(2));
		graphics.setColor(color);
		graphics.drawOval(point.x, point.y, radius, radius);
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
		// warpFilter.setMorphImage(morphImage);
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
			}

	}

	public void drawLine(Point p1, Point p2) {
		graphics = (Graphics2D) outputImage.getGraphics();
		graphics.setColor(Color.BLACK);
		graphics.setStroke(new BasicStroke(1));
		graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
	}

	public void drawPolygon() {
		graphics = (Graphics2D) outputImage.getGraphics();
		graphics.setColor(Color.BLACK);
		graphics.setStroke(new BasicStroke(1));
		int x[] = xPoints.stream().mapToInt(i -> i).toArray();
		int y[] = yPoints.stream().mapToInt(i -> i).toArray();

		graphics.drawPolyline(x, y, xPoints.size());
	}

	public Point findIndex(int x, int y) {
		Point point = new Point();
		int minDistance = 214748364;
		for (int i = 0; i < GRID_NUMBER * GRID_NUMBER; i++)
			for (int j = 0; j < GRID_NUMBER * GRID_NUMBER; j++) {
				int calculatedDistance = (int) Math.sqrt(Math.pow((x - dstGrid.xGrid[i]), 2)
						+ Math.pow((y - dstGrid.yGrid[j]), 2));
				if (calculatedDistance < minDistance && i == j) {
					minDistance = calculatedDistance;
					point.x = i;
					point.y = j;
				}
			}
		return point;
	}

	public void findAllNearestPoints(Point point, int distance) {
		int maxDistance = (int) Math.sqrt(distance);
		for (int j = 0 - maxDistance; j < maxDistance; j++) {
			for (int i = point.x - maxDistance; i <= point.x + maxDistance; i++) {
				int x = j * GRID_NUMBER + i;
				if (x >= 0 && x < GRID_NUMBER * GRID_NUMBER) {
					if ((Math.pow((dstGrid.xGrid[x] - clickedPoint.x), 2) + Math.pow(
							(dstGrid.yGrid[x] - clickedPoint.y), 2)) <= distance * distance) {
						nearestPoints.add(new Point(x, x));
					}
				}
			}
		}
	}

	public void findPointsInsidePolygon() {
		polygon = new Polygon(xPoints.stream().mapToInt(i -> i).toArray(), yPoints.stream().mapToInt(i -> i).toArray(),
				xPoints.size());
		for (int i = 0; i < GRID_NUMBER * GRID_NUMBER; i++) {
			Point p = new Point((int) dstGrid.xGrid[i], (int) dstGrid.yGrid[i]);
			if (polygon.contains(p)) {
				nearestPoints.add(new Point(i, i));
			}
		}
	}
}