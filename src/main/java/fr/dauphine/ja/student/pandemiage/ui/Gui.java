package fr.dauphine.ja.student.pandemiage.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import fr.dauphine.ja.pandemiage.Model.Board;
import fr.dauphine.ja.student.pandemiage.controller.GuiController;
import fr.dauphine.ja.student.pandemiage.gameengine.AiGameEngine;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;
import fr.dauphine.ja.student.pandemiage.gameengine.HumanGameEngine;
import fr.dauphine.ja.student.pandemiage.gameengine.LoseException;
import fr.dauphine.ja.student.pandemiage.gameengine.WinException;
import fr.dauphine.ja.student.pandemiage.ui.view.PandemiageGUI;

public class Gui extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String logoName = "logo.jpg";

	public static final boolean PRINTOUT = false;
	public static final int NUMBERCITY = 48;

	/* GUI component */
	private JPanel mainContener = null;

	private JPanel mainMenuContener = null;

	private PandemiageGUI mainGameContener = null;

	private InputStream logoFile = Gui.class.getResourceAsStream(logoName);

	private CardLayout card = null;

	/* OPTIONS */
	private String aijar;
	private String cityGraphFile;
	private int difficulty;
	private int turnDuration;
	private int handSize;

	private static String mode = "AI";

	public Gui(String aijar, String cityGraphFile, int difficulty, int turnDuration, int handSize) {

		this.aijar = aijar;
		this.cityGraphFile = cityGraphFile;
		this.difficulty = difficulty;
		this.turnDuration = turnDuration;
		this.handSize = handSize;
	}

	/* WELCOME INTERFACE */

	public void initializeMainMenu() throws IOException, XMLStreamException {
		Font labelFont = new Font("Serif", Font.PLAIN, 20);
		Font buttonFont = new Font("Serif", Font.BOLD, 16);
		Font listFont = new Font("Serif", Font.BOLD, 14);
		Color labelColor = new Color(68, 82, 122);
		// Gui2 is the frame with the cardLayout
		card = new CardLayout();
		mainContener = new JPanel();

		mainContener.setLayout(card);

		this.setLayout(new BorderLayout());
		this.setTitle("PANDEMIAGE");
		this.setSize(500, 500);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainMenuContener = new JPanel(new GridLayout(8, 1));
		mainMenuContener.setPreferredSize(new Dimension(500, 500));

		JLabel logoPanel = new JLabel();
		BufferedImage bi = ImageIO.read(logoFile);
		Image img = bi.getScaledInstance(500, 150, Image.SCALE_SMOOTH);

		logoPanel.setIcon(new ImageIcon(img));
		mainMenuContener.add(logoPanel);

		// Option AI jar
		JPanel aiPanel = new JPanel(new GridLayout(1, 2));

		JLabel aiLabel = new JLabel(" Import your AI : ");
		aiLabel.setBackground(labelColor);
		aiLabel.setFont(labelFont);
		aiLabel.setForeground(Color.WHITE);
		aiLabel.setOpaque(true);
		JButton aiButton = new JButton("Import");
		aiButton.setFont(buttonFont);
		aiButton.setBackground(new Color(150, 137, 60));
		aiButton.setForeground(new Color(102, 0, 0));
		aiButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser(System.getProperty("user.dir"));

				int returnValue = jfc.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					aijar = jfc.getSelectedFile().getAbsolutePath();
					aiLabel.setText("<html>" + aiLabel.getText() + "</br> </br> <br>Import of "
							+ jfc.getSelectedFile().getName() + " was successful !</html>");
				}
			}
		});

		aiPanel.add(aiLabel);
		aiPanel.add(aiButton);
		mainMenuContener.add(aiPanel);

		// Option graphe map
		JPanel graphePanel = new JPanel(new GridLayout(1, 2));

		JLabel grapheLabel = new JLabel(" Import the map file : ");
		grapheLabel.setBackground(labelColor);
		grapheLabel.setFont(labelFont);
		grapheLabel.setForeground(Color.WHITE);
		grapheLabel.setOpaque(true);
		JButton grapheButton = new JButton("Import");
		grapheButton.setFont(buttonFont);
		grapheButton.setBackground(new Color(150, 137, 60));
		grapheButton.setForeground(new Color(102, 0, 0));
		grapheButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser(System.getProperty("user.dir"));

				int returnValue = jfc.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					cityGraphFile = jfc.getSelectedFile().getAbsolutePath();
					grapheLabel.setText("<html>" + grapheLabel.getText() + "</br> </br> <br>Import of "
							+ jfc.getSelectedFile().getName() + " was successful !</html>");
				}
			}
		});

		graphePanel.add(grapheLabel);
		graphePanel.add(grapheButton);
		mainMenuContener.add(graphePanel);

		// Option difficulty
		JPanel difficultyPanel = new JPanel(new GridLayout(1, 2));
		JLabel difficultyLabel = new JLabel(" Difficulty : ");
		difficultyLabel.setBackground(labelColor);
		difficultyLabel.setFont(labelFont);
		difficultyLabel.setForeground(Color.WHITE);
		difficultyLabel.setOpaque(true);
		difficultyPanel.add(difficultyLabel);

		JComboBox<Integer> difficultyBox = new JComboBox<>(new Integer[] { 0, 1, 2 });
		difficultyBox.setSelectedItem(difficulty);
		difficultyBox.setFont(listFont);
		difficultyBox.setBackground(new Color(102, 0, 0));
		difficultyBox.setForeground((new Color(150, 137, 60)));

		difficultyBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				difficulty = (Integer) difficultyBox.getSelectedItem();
			}
		});
		difficultyPanel.add(difficultyBox);

		mainMenuContener.add(difficultyPanel);
		// Option turn duration
		JPanel turnDurationPanel = new JPanel(new GridLayout(1, 2));
		JLabel turnDurationLabel = new JLabel(" Turn duration (second) : ");
		turnDurationLabel.setBackground(labelColor);
		turnDurationLabel.setFont(labelFont);
		turnDurationLabel.setForeground(Color.WHITE);
		turnDurationLabel.setOpaque(true);
		turnDurationPanel.add(turnDurationLabel);

		JComboBox<Integer> turnDurationBox = new JComboBox<>(new Integer[] { 0, 1, 2, 3, 4, 5, 10, 100, 1000 });
		turnDurationBox.setSelectedItem(turnDuration);
		turnDurationBox.setFont(listFont);
		turnDurationBox.setBackground(new Color(102, 0, 0));
		turnDurationBox.setForeground((new Color(150, 137, 60)));
		turnDurationBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				turnDuration = (Integer) turnDurationBox.getSelectedItem();
			}
		});
		turnDurationPanel.add(turnDurationBox);
		mainMenuContener.add(turnDurationPanel);

		// Option Player hand size max
		JPanel handSizePanel = new JPanel(new GridLayout(1, 2));
		JLabel handSizeLabel = new JLabel(" Max hand size : ");
		handSizeLabel.setBackground(labelColor);
		handSizeLabel.setFont(labelFont);
		handSizeLabel.setForeground(Color.WHITE);
		handSizeLabel.setOpaque(true);
		handSizePanel.add(handSizeLabel);

		JComboBox<Integer> handSizeBox = new JComboBox<>(
				new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 20 });
		handSizeBox.setSelectedItem(handSize);
		handSizeBox.setFont(listFont);
		handSizeBox.setBackground(new Color(102, 0, 0));
		handSizeBox.setForeground((new Color(150, 137, 60)));
		handSizeBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handSize = (Integer) handSizeBox.getSelectedItem();
			}
		});
		handSizePanel.add(handSizeBox);

		mainMenuContener.add(handSizePanel);
		// Option Player or IA
		JPanel gameModePanel = new JPanel(new GridLayout(1, 3));
		JLabel gameModeLabel = new JLabel(" Game mode: ");
		gameModeLabel.setBackground(labelColor);
		gameModeLabel.setFont(labelFont);
		gameModeLabel.setForeground(Color.WHITE);
		gameModeLabel.setOpaque(true);
		gameModePanel.add(gameModeLabel);
		JButton playerModeButton = new JButton("Human");
		playerModeButton.setFont(buttonFont);
		playerModeButton.setBackground(new Color(150, 137, 60));
		playerModeButton.setForeground(new Color(102, 0, 0));
		playerModeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				mode = "HUMAN";
			}
		});
		JButton aiModeButton = new JButton("AI");
		aiModeButton.setFont(buttonFont);
		aiModeButton.setBackground(new Color(150, 137, 60));
		aiModeButton.setForeground(new Color(102, 0, 0));
		aiModeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				mode = "AI";
			}
		});
		gameModePanel.add(playerModeButton);
		gameModePanel.add(aiModeButton);
		mainMenuContener.add(gameModePanel);

		// START
		JPanel startPanel = new JPanel(new GridLayout(1, 3));

		JButton startButton = new JButton("START GAME");
		startButton.setFont(buttonFont);
		startButton.setBackground(Color.WHITE);
		startButton.setForeground(new Color(102, 0, 0));
		startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if ("AI".equals(mode)) {
					try {
						startAI();
					} catch (FileNotFoundException e1) {
						// do nothing
					} catch (XMLStreamException e1) {
						// do nothing
					}
				} else if ("HUMAN".equals(mode)) {
					try {
						startHuman();
					} catch (FileNotFoundException e1) {
						// do nothing
					} catch (XMLStreamException e1) {
						// do nothing
					}
				}
			}

		});
		startPanel.add(startButton);

		mainMenuContener.add(startPanel);

		// first element of the card layout
		mainContener.add(mainMenuContener, "menu");
		/* Initialisation game interface */
		mainGameContener = new PandemiageGUI();
		mainContener.add(mainGameContener, "game");

		this.setContentPane(mainContener);

		this.getRootPane().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
		this.setVisible(true);

	}

	private void startHuman() throws FileNotFoundException, XMLStreamException {
		// Change to Game GUI
		card.show(mainContener, "game");
		GameEngine g;
		Board board = new Board(cityGraphFile);
		g = new HumanGameEngine(board, aijar, difficulty, turnDuration, handSize, true);

		mainGameContener.setController(new GuiController(g, board.getPlayer()));

		board.addObserver(mainGameContener);
		mainGameContener.startGameGUI(board, "HUMAN");

		this.pack();
		this.setLocationRelativeTo(null);

		Thread gameThread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					g.loop();
				} catch (LoseException e) {
					backMenu();
				} catch (WinException e) {
					backMenu();
				} catch (InterruptedException e) {

				}

			}
		});
		gameThread.start();

	}

	private void startAI() throws FileNotFoundException, XMLStreamException {

		// Change to Game GUI
		card.show(mainContener, "game");
		GameEngine g;
		Board board = new Board(cityGraphFile);
		g = new AiGameEngine(board, aijar, difficulty, turnDuration, handSize, true);
		board.addObserver(mainGameContener);
		mainGameContener.startGameGUI(board, "IA");

		this.pack();
		this.setLocationRelativeTo(null);

		Thread gameThread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					g.loop();
				} catch (LoseException e) {
					backMenu();
				} catch (WinException e) {
					backMenu();
				} catch (InterruptedException e) {

				}

			}
		});
		gameThread.start();

	}

	private void backMenu() {
		// Back to menu
		card.show(mainContener, "menu");
		this.setSize(500, 500);
		this.setLocationRelativeTo(null);
	}
}
