package fr.dauphine.ja.student.pandemiage.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import fr.dauphine.ja.pandemiage.Model.Board;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.student.pandemiage.controller.GuiController;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;
import fr.dauphine.ja.student.pandemiage.ui.Gui;

public class GameStats extends JPanel {
	public final static Font font = new Font("Serif", Font.PLAIN, 20);
	public final static Font font2 = new Font("Serif", Font.BOLD, 20);

	private static final long serialVersionUID = -7842278393722074776L;
	private Board board = null;
	private JPanel playerHandPanel = null;
	private JTextArea log = null;

	private JPanel boardPanel = null;
	private JPanel actionPanel = null;
	private JComboBox<String> moveList = null;
	private JComboBox<String> flyToList = null;
	private JButton flyToButton = null;
	private JComboBox<String> discardList = null;
	private JButton discardButton = null;
	private JPanel statsPanel = null;

	private JScrollPane scroll = null;
	private JButton buttonContinue = null;

	// Waiting for user to click on continue button,
	private boolean continue_game = false;

	private GuiController controller;

	private String gameMode = null;

	public GameStats(GuiController controller, Board board, String gameMode) {

		this.board = board;
		this.gameMode = gameMode;
		this.controller = controller;
		this.setPreferredSize(new Dimension(1700, 300));
		this.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(3, 34, 2)));
		this.setLayout(new BorderLayout());

		playerHandPanel = new JPanel(new GridLayout(1, 11));
		playerHandPanel.setPreferredSize(new Dimension(700, 300));

		updatePlayerHand();
		this.add(playerHandPanel, BorderLayout.WEST);

		log = new JTextArea();
		log.setBackground(new Color(92, 83, 92));
		log.setForeground(new Color(54, 148, 87));
		log.setFont(font);

		log.setLineWrap(true);
		log.setEditable(false);

		scroll = new JScrollPane(log);
		scroll.setPreferredSize(new Dimension(500, 300));

		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(scroll, BorderLayout.CENTER);

		updateLog("Game started");

		boardPanel = new JPanel(new GridLayout(1, 2));
		boardPanel.setPreferredSize(new Dimension(500, 300));
		// boardPanel.setBackground(new Color(68, 82, 122));

		// STATS
		statsPanel = new JPanel(new GridLayout(6, 1));
		boardPanel.add(statsPanel);
		updateStatsPanel();

		// ACTIONS
		actionPanel = new JPanel(new GridLayout(6, 1));
		if (gameMode.equals("HUMAN"))
			boardPanel.add(actionPanel);

		// MoveTo
		JPanel moveToPanel = new JPanel(new GridLayout(1, 2));

		moveList = new JComboBox<String>();
		for (String cityName : board.getNeighboursOfLocation()) {
			moveList.addItem(cityName);
		}
		JButton moveToButton = new JButton("Move");
		moveToButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						controller.moveTo(String.valueOf(moveList.getSelectedItem()));
					}
				}).start();
			}
		});
		moveToPanel.add(moveList);
		moveToPanel.add(moveToButton);
		actionPanel.add(moveToPanel);

		// FlyTo
		JPanel flyToPanel = new JPanel(new GridLayout(1, 2));

		flyToList = new JComboBox<String>();
		for (PlayerCardInterface card : board.getHand()) {
			flyToList.addItem(card.getCityName());
		}
		flyToButton = new JButton("Fly");
		flyToButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						controller.flyTo(String.valueOf(flyToList.getSelectedItem()));
					}
				}).start();
			}
		});
		flyToPanel.add(flyToList);
		flyToPanel.add(flyToButton);
		actionPanel.add(flyToPanel);

		// Treat disease
		JButton treatButton = new JButton("Treat disease");
		treatButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {

					@Override
					public void run() {

						controller.treatDisease();
					}
				}).start();
			}
		});
		actionPanel.add(treatButton);

		// Discover Cure
		JButton cureButton = new JButton("Discover cure");
		cureButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {

					@Override
					public void run() {

						controller.cureDisease();
					}
				}).start();
			}
		});
		actionPanel.add(cureButton);

		// Discard

		JPanel discardPanel = new JPanel(new GridLayout(1, 2));

		discardList = new JComboBox<String>();
		discardButton = new JButton("Discard");
		discardButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						board.removeHandByName(String.valueOf(discardList.getSelectedItem()));
					}
				}).start();
			}
		});
		disableDiscard();
		discardPanel.add(discardList);
		discardPanel.add(discardButton);
		actionPanel.add(discardPanel);

		this.add(boardPanel, BorderLayout.EAST);

		if ("IA".equals(gameMode)) {
			actionPanel.removeAll();

		}
	}

	/**
	 * 
	 */
	public void updateStatsPanel() {
		statsPanel.removeAll();

		JLabel jlabel1 = new JLabel(
				"Player Deck:  " + board.getNbPlayerCardsLeft() + "/" + (Gui.NUMBERCITY + board.getNbEpidemyCards()));
		statsPanel.add(jlabel1);

		JLabel jlabel2 = new JLabel("Infection Rate:  " + GameEngine.INFECTIONRATETAB[board.getInfectionRateIndex()]);
		statsPanel.add(jlabel2);

		JLabel jlabel3 = new JLabel("Out break(s):  " + board.getNbOutBreaks() + "/" + GameEngine.MAXOUTBREAKS);
		statsPanel.add(jlabel3);

		int[] diseaseBlocksLeft = board.getDiseaseBlocksLeft();
		JLabel jlabel4 = new JLabel("Block(s) left:  " + diseaseBlocksLeft[0] + " " + diseaseBlocksLeft[1] + " "
				+ diseaseBlocksLeft[2] + " " + diseaseBlocksLeft[3]);
		statsPanel.add(jlabel4);

		JLabel jlabel5 = new JLabel("Cured Disease(s):  " + board.getCured());
		statsPanel.add(jlabel5);

		buttonContinue = new JButton("Continue");
		buttonContinue.setBackground(new Color(150, 137, 60));
		buttonContinue.setFont(font2);
		buttonContinue.setForeground(new Color(102, 0, 0));
		buttonContinue.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				continue_game = true;
				buttonContinue.setEnabled(false);

			}
		});
		buttonContinue.setEnabled(false);

//		for (int i = 0; i < stats.length; i++) {
//			stats[i].setFont(font2);
//			stats[i].setForeground(Color.WHITE);
//			stats[i].setHorizontalAlignment(JLabel.CENTER);
//			statsPanel.add(stats[i]);
//	}

		statsPanel.add(buttonContinue);

		statsPanel.validate();

	}

	public void updateLog(String msg) {
		log.append(">  " + msg + "\n");
		log.validate();
		JScrollBar vertical = scroll.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}

	public void updatePlayerHand() {
		playerHandPanel.removeAll();
		CardLabel card = null;
		List<PlayerCardInterface> hand = board.getHand();

		for (PlayerCardInterface playerCardInterface : hand) {
			card = new CardLabel(playerCardInterface.getCityName(), playerCardInterface.getDisease());
			playerHandPanel.add(card);
		}
		playerHandPanel.validate();

	}

	public void waitUser() {
		if (gameMode.equals("IA")) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {

			}
			buttonContinue.setEnabled(true);
			while (!continue_game) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			continue_game = false;
		} else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {

			}
		}

	}

	public void updateMoveList() {
		if (gameMode.equals("HUMAN")) {
			moveList.removeAllItems();
			for (String cityName : board.getNeighboursOfLocation()) {
				moveList.addItem(cityName);
			}
		}
	}

	public void disableDiscard() {
		discardList.setEnabled(false);
		discardButton.setEnabled(false);
	}

	public void updateDiscardList() {
		if (gameMode.equals("HUMAN")) {
			discardList.setEnabled(true);
			discardButton.setEnabled(true);
			discardList.removeAllItems();
			for (PlayerCardInterface card : board.getHand()) {
				discardList.addItem(card.getCityName());
			}
		}
	}

	public void updateFlyToList() {
		if (gameMode.equals("HUMAN")) {
			flyToList.removeAllItems();
			for (PlayerCardInterface card : board.getHand()) {
				flyToList.addItem(card.getCityName());
			}
		}

	}

}
