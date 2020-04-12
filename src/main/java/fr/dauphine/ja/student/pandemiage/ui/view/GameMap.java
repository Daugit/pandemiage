package fr.dauphine.ja.student.pandemiage.ui.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import fr.dauphine.ja.pandemiage.Model.Board;
import fr.dauphine.ja.pandemiage.Model.City;
import fr.dauphine.ja.pandemiage.Model.Edge;

public class GameMap extends JPanel {

	private static final Object monitor = new Object();
	/**
	 * 
	 */
	private static final long serialVersionUID = 6111073952356349531L;

	public static final String WORLDMAP_FILE = "worldMap.png";
	public static final String GUY_FILE = "playerImg.png";

	private static URL WORLDMAP_FILE_URL = GameMap.class.getResource(WORLDMAP_FILE);
	private static URL GUY_FILE_URL = GameMap.class.getResource(GUY_FILE);

	private ArrayList<CitySquare> cities = new ArrayList<>();
	private Board board = null;

	public static int firstTime = 1;

	public GameMap(Board board, String gameMode) throws FileNotFoundException, XMLStreamException {
		this.setPreferredSize(new Dimension(1700, 600));
		this.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(3, 34, 2)));
		this.board = board;

		ArrayList<Double> xList = new ArrayList<Double>();
		ArrayList<Double> yList = new ArrayList<Double>();
		CitySquare citySquare = null;

		for (City city : board.getCities()) {
			xList.add(city.getX());
			yList.add(city.getY());
			citySquare = new CitySquare(city.getId(), city.getX(), city.getY(),
					new Color(city.getR(), city.getG(), city.getB()), city.getLabel(),
					board.getBlocksOnCity(city.getLabel()));

			synchronized (monitor) {
				cities.add(citySquare);
			}
			this.add(citySquare);
		}

	}

	public CitySquare getCityByID(int id) {
		synchronized (monitor) {
			for (CitySquare citySquare : cities) {
				if (citySquare.getId() == id)
					return citySquare;
			}
		}
		return null;
	}

	public synchronized City getCityByName(String name) {
		for (City city : board.getCities()) {
			if (city.getLabel().equals(name))
				return city;
		}
		return null;
	}

	public Point[] getClosestMiddlesBetweenCities(CitySquare c1, CitySquare c2) {
		Point[] positions = new Point[2];
		Point[] c1Middles = c1.getMiddles();
		Point[] c2Middles = c2.getMiddles();
		Point a = c1.getMiddles()[0];
		Point b = c2.getMiddles()[0];
		double dMin = a.distance(b);
		for (int i = 0; i < c1Middles.length; i++) {
			for (int j = 0; j < c2Middles.length; j++) {
				if (c1Middles[i].distance(c2Middles[j]) < dMin) {
					a = c1Middles[i];
					b = c2Middles[j];
					dMin = c1Middles[i].distance(c2Middles[j]);
				}
			}
		}
		positions[0] = a;
		positions[1] = b;
		return positions;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		g2.setStroke(new BasicStroke(2));

		g2.drawImage(Toolkit.getDefaultToolkit().getImage(WORLDMAP_FILE_URL), 0, 0, this.getWidth(), this.getHeight(),
				this);

		// Draw player
		String locationPlayer = board.getLocation();
		City city = getCityByName(locationPlayer);
		int xGuy = 0;
		int yGuy = 0;

		if (city.getX() < 0)
			xGuy = (int) (Math.abs(Math.round(Math.abs(city.getX()) * 2) - 2000) / 2);

		else
			xGuy = (int) (Math.round(city.getX() + 2000) / 2);

		yGuy = (int) ((Math.round(Math.abs(city.getY() - 1100))) / 2) - 50;

		g2.drawImage(Toolkit.getDefaultToolkit().getImage(GUY_FILE_URL), xGuy, yGuy, 60, 60, this);

		Iterator<Edge> iterator = board.getEdges().iterator();
		Edge firstEdge = board.getEdges().get(0);
		Point[] positions = new Point[2];
		while (iterator.hasNext()) {

			Edge edge = iterator.next();
			if (edge.equals(firstEdge) && firstTime == 1) {
				firstTime = 2;
				if (Math.abs(getCityByID(edge.getSource()).getX() - getCityByID(edge.getTarget()).getX()) > 850) {
					if (getCityByID(edge.getSource()).getX() < getCityByID(edge.getTarget()).getX()) {
						Point a = getCityByID(edge.getSource()).getMiddles()[3];
						Point b = getCityByID(edge.getTarget()).getMiddles()[1];
						g2.drawLine(a.x, a.y, 0, b.y);
						g2.drawLine(1700, a.y, b.x, b.y);
					} else {
						Point a = getCityByID(edge.getSource()).getMiddles()[1];
						Point b = getCityByID(edge.getTarget()).getMiddles()[3];
						g2.drawLine(a.x, a.y, 1700, b.y);
						g2.drawLine(0, a.y, b.x, b.y);
					}
				} else {
					positions = getClosestMiddlesBetweenCities(getCityByID(edge.getSource()),
							getCityByID(edge.getTarget()));
					g2.drawLine(positions[0].x, positions[0].y, positions[1].x, positions[1].y);
				}
			} else if (edge.equals(firstEdge) && firstTime == 2)
				break;

			else if (!edge.equals(firstEdge)) {
				if (Math.abs(getCityByID(edge.getSource()).getX() - getCityByID(edge.getTarget()).getX()) > 850) {
					if (getCityByID(edge.getSource()).getX() < getCityByID(edge.getTarget()).getX()) {
						Point a = getCityByID(edge.getSource()).getMiddles()[3];
						Point b = getCityByID(edge.getTarget()).getMiddles()[1];
						g2.drawLine(a.x, a.y, 0, b.y);
						g2.drawLine(1700, a.y, b.x, b.y);
					} else {
						Point a = getCityByID(edge.getSource()).getMiddles()[1];
						Point b = getCityByID(edge.getTarget()).getMiddles()[3];
						g2.drawLine(a.x, a.y, 1700, b.y);
						g2.drawLine(0, a.y, b.x, b.y);
					}
				} else {
					positions = getClosestMiddlesBetweenCities(getCityByID(edge.getSource()),
							getCityByID(edge.getTarget()));
					g2.drawLine(positions[0].x, positions[0].y, positions[1].x, positions[1].y);
				}

			}
		}
		firstTime = 1;

	}

	public void updateMapPanel() {
		this.removeAll();
		ArrayList<Double> xList = new ArrayList<Double>();
		ArrayList<Double> yList = new ArrayList<Double>();
		CitySquare citySquare = null;

		for (City city : board.getCities()) {
			xList.add(city.getX());
			yList.add(city.getY());
			citySquare = new CitySquare(city.getId(), city.getX(), city.getY(),
					new Color(city.getR(), city.getG(), city.getB()), city.getLabel(),
					board.getBlocksOnCity(city.getLabel()));
			synchronized (monitor) {
				cities.add(citySquare);
			}
			this.add(citySquare);
		}
		this.validate();
		this.repaint();

	}

}
