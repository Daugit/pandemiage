package fr.dauphine.ja.student.pandemiage.gameengine;

import java.io.FileNotFoundException;

import javax.xml.stream.XMLStreamException;

import fr.dauphine.ja.pandemiage.Model.Board;
import fr.dauphine.ja.pandemiage.common.GameStatus;
import fr.dauphine.ja.student.pandemiage.ui.Cli;

public class HumanGameEngine extends GameEngine {

	public HumanGameEngine(Board board, String aiJar, int difficulty, int turnDuration, int handSize, boolean hasGUI)
			throws FileNotFoundException, XMLStreamException {
		super(board, aiJar, difficulty, turnDuration, handSize, hasGUI);
	}

	@Override
	public void loop() throws LoseException, WinException, InterruptedException {
		drawInitialPlayerCards();
		Cli.printoutln("Initial Hand : " + printPlayerHand(player));

		board.addInitalEpidemyCards();

		initialInfection();
		Cli.printout("Initial Infection City: ");

		printInfectedCity();

		while (gameStatus == GameStatus.ONGOING) {

			board.incrementTurn();
			printTurn(board.getTurn());
			board.notifyWait();
			board.updateFlyToList();
			for (int i = 0; i < ACTIONPERTURN; i++) {

				int actionLeft = board.getActionLeft();
				while (board.getActionLeft() == actionLeft) {
					Thread.currentThread().sleep(100);
					if (board.getActionLeft() == 0)
						break;
				}
			}

			testVictory();
			board.resetActionLeft();

			drawPlayerCardPhase();
			Cli.printoutln("\nHand after drawing: \n" + printPlayerHand(player));
			board.notifyWait();
			if (board.getHand().size() <= MAXPLAYERCARD) {
				board.noNecessaryDiscard();
			} else {
				while (board.getHand().size() > MAXPLAYERCARD) {
					Thread.currentThread().sleep(100);
				}
			}
			handleDrawInfectionCard();
			printInfectedCity();

		}
	}
}
