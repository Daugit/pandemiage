package fr.dauphine.ja.student.pandemiage.ui.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * <b> This class represents a city as a square with its name, color and disease
 * blocks</b>
 * 
 * @author avastTeam
 *
 */
public class CitySquare extends JPanel {
	private static final long serialVersionUID = -9128626315928716149L;
	private int id = 0;
	private int x = 0;
	private int y = 0;
	private Point[] middles = null;
	private Color color = Color.WHITE;
	private String name = "";
	private JLabel cityBlocks = null;
	private JLabel cityName = null;
	private int[] blocks = null;

	public CitySquare(int idP, double xP, double yP, Color colorP, String nameP, int[] blocksP) {
		super(new GridLayout(2, 1));
		id = idP;
		if (xP < 0)
			xP = (int) (Math.abs(Math.round(Math.abs(xP) * 2) - 2000) / 2);

		else
			xP = (int) (Math.round(xP + 2000) / 2);

		yP = (int) ((Math.round(Math.abs(yP - 1100))) / 2);

		this.x = (int) xP;
		this.y = (int) yP;
		this.color = colorP;
		this.name = nameP;
		this.blocks = blocksP;
		setMiddles();
		initConfigs();
	}

	private void initConfigs() {
		this.setPreferredSize(new Dimension(65, 40));
		this.setBackground(color);
		this.setBorder(BorderFactory.createLineBorder(Color.WHITE));

		cityName = new JLabel(" " + name);
		Font font = new Font("Courier", Font.BOLD, 10);
		Font fontBlocks = new Font("Courier", Font.BOLD, 12);

		cityName.setFont(font);
		cityName.setForeground(Color.BLACK);
		cityName.setPreferredSize(new Dimension(65, 20));
		cityName.setHorizontalAlignment(JLabel.CENTER);
		this.add(cityName);

		cityBlocks = new JLabel(" " + blocks[0] + " " + blocks[1] + " " + blocks[2] + " " + blocks[3]);
		cityBlocks.setFont(fontBlocks);
		cityBlocks.setForeground(Color.BLACK);
		cityBlocks.setPreferredSize(new Dimension(65, 20));
		cityBlocks.setHorizontalAlignment(JLabel.CENTER);
		this.add(cityBlocks);

		this.setLocation(x, y);
	}

	@Override
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public Point[] getMiddles() {
		return middles;
	}

	private void setMiddles() {
		middles = new Point[4];
		// from top square side to left side
		middles[0] = new Point(x + 65 / 2, y);
		middles[1] = new Point(x + 65, y + 40 / 2);
		middles[2] = new Point(x + 65 / 2, y + 40);
		middles[3] = new Point(x, y + 40 / 2);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
