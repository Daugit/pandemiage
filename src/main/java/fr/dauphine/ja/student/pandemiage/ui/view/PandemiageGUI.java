package fr.dauphine.ja.student.pandemiage.ui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import fr.dauphine.ja.pandemiage.Model.Board;
import fr.dauphine.ja.pandemiage.Model.BoardChange;
import fr.dauphine.ja.student.pandemiage.controller.GuiController;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;

public class PandemiageGUI extends JPanel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 634115709786462633L;

	private Board board = null;

	private IndicatorsPanel indicatorsPanel = null;

	private GameMap gameMap = null;

	private GameStats gameStats = null;

	private GuiController controller = null;

	private ImageIcon iconVictory = new ImageIcon(getClass().getResource("victoryIcon.png"));
	private ImageIcon iconDefeat = new ImageIcon(getClass().getResource("defeatIcon.png"));

	public PandemiageGUI(GuiController controller) throws FileNotFoundException, XMLStreamException {
		this.setSize(new Dimension(1700, 1000));
		this.setLayout(new BorderLayout());
		this.controller = controller;
	}

	public PandemiageGUI() {
		this.setSize(new Dimension(1700, 1000));
		this.setLayout(new BorderLayout());
	}

	public void startGameGUI(Board gameBoard, String gameMode) throws FileNotFoundException, XMLStreamException {
		board = gameBoard;

		indicatorsPanel = new IndicatorsPanel(board);
		this.add(indicatorsPanel, BorderLayout.NORTH);
		gameMap = new GameMap(board, gameMode);
		this.add(gameMap, BorderLayout.CENTER);
		gameStats = new GameStats(controller, board, gameMode);
		this.add(gameStats, BorderLayout.SOUTH);
	}

	@Override
	public void update(String str, List<BoardChange> boardChanges) {
		for (BoardChange boardChange : boardChanges) {
			switch (boardChange) {
			case DRAW_CARD:
				gameStats.updateStatsPanel();
				break;
			case UPDATEFLY:
				gameStats.updateFlyToList();
				break;
			case ADD_CARD:
				gameStats.updatePlayerHand();
				gameStats.updateLog("Player drawn city " + str);
				break;
			case INCR_INFECTIONRATE:
				this.showConsole("EPIDEMY!");
				gameStats.updateStatsPanel();
				gameStats.updateLog("EPIDEMY Card drawn");
				if (str.length() > 0) {
					gameStats.updateLog("Infection rate increased to " + str);
					this.showConsole("Infection rate increased, " + str + " infection cards will be drawn now !");

				}
				break;
			case OUTBREAK:
				gameStats.updateStatsPanel();
				this.showConsole("OUTBREAK on " + str);
				gameStats.updateLog("OUTBREAK on " + str);
				break;
			case INFECTION:
				gameMap.updateMapPanel();
				gameStats.updateStatsPanel();
				gameStats.updateLog(str + " infected");
				break;
			case MOVE_TO:
				gameMap.updateMapPanel();
				gameStats.updateLog("Player moved to " + str);
				gameStats.updateMoveList();
				break;
			case WAIT_USER:
				gameStats.waitUser();
				break;
			case TREAT_DISEASE:
				gameMap.updateMapPanel();
				gameStats.updateLog("Player treated disease at " + str);
				break;
			case INCR_BLOCK_DISEASE:
				gameStats.updateStatsPanel();
				break;
			case CURE_DISEASE:
				gameStats.updateStatsPanel();
				gameStats.updateLog("Player cured " + str + " disease !");
				this.showConsole("Player cured " + str + " disease !");
				break;
			case UPDATE_MAP:
				gameStats.updatePlayerHand();
				break;
			case HUMAN_DISCARD:
				gameStats.updatePlayerHand();
				gameStats.updateDiscardList();
				gameStats.updateLog("Player discarded " + str);
				break;
			case CARD_TO_DISCARD:
				gameStats.updateDiscardList();
				break;
			case USE_CARD:
				gameStats.updatePlayerHand();
				gameStats.updateFlyToList();
				gameStats.updateLog("Player used " + str);
				break;
			case DISCARD:
				gameStats.updatePlayerHand();
				gameStats.updateLog("Player discarded " + str);
				break;
			case NO_DISCARD:
				gameStats.disableDiscard();
				break;
			case NEW_TURN:
				indicatorsPanel.updateTurnLabel();
				gameStats.updateLog(">>Turn number " + str + " begins<<<");
				break;
			case DEFEAT:
				showDefeat(str);
				break;
			case VICTORY:
				showVictory();
				break;
			default:
				break;
			}
		}

	}

	private void showVictory() {
		JOptionPane.showMessageDialog(null,
				"You saved the world !!! \nInfection-rate: "
						+ GameEngine.INFECTIONRATETAB[board.getInfectionRateIndex()] + "\nCured diseases : "
						+ board.getCured() + "\nNb-outbreaks: " + board.getNbOutBreaks() + "\nNb-player-cards-left: "
						+ board.getNbPlayerCardsLeft(),
				"WIN", JOptionPane.PLAIN_MESSAGE, iconVictory);
	}

	private void showDefeat(String reason) {
		JOptionPane.showMessageDialog(null,
				"You failed saving the world : " + reason + "\nInfection-rate: "
						+ GameEngine.INFECTIONRATETAB[board.getInfectionRateIndex()] + "\nCured diseases : "
						+ board.getCured() + "\nNb-outbreaks: " + board.getNbOutBreaks() + "\nNb-player-cards-left: "
						+ board.getNbPlayerCardsLeft(),
				"GAME OVER", JOptionPane.PLAIN_MESSAGE, iconDefeat);
	}

	private void showConsole(String msg) {
		JOptionPane.showMessageDialog(null, msg);
	}

	public void setController(GuiController controller) {
		this.controller = controller;
	}

}