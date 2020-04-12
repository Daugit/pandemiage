package fr.dauphine.ja.student.pandemiage.gameengine;

import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import fr.dauphine.ja.pandemiage.Model.Board;
import fr.dauphine.ja.pandemiage.common.AiInterface;
import fr.dauphine.ja.pandemiage.common.AiLoader;
import fr.dauphine.ja.pandemiage.common.GameStatus;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.student.pandemiage.ui.Cli;

public class AiGameEngine extends GameEngine {

	public AiGameEngine(Board board, String aiJar, int difficulty, int turnDuration, int handSize, boolean hasGUI)
			throws FileNotFoundException, XMLStreamException {
		super(board, aiJar, difficulty, turnDuration, handSize, hasGUI);
	}

	@Override
	public void loop() throws LoseException, WinException, InterruptedException {
		AiInterface ai = AiLoader.loadAi(aiJar);
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

			ai.playTurn(this, player);

			testVictory();
			board.resetActionLeft();

			int nbEpidemicCards = drawPlayerCardPhase();
			Cli.printoutln("\nHand after drawing: \n" + printPlayerHand(player));
			board.notifyWait();
			List<PlayerCardInterface> discarded = ai.discard(this, player, MAXPLAYERCARD, nbEpidemicCards);
			if (discarded.size() > 0) {
				board.notifyDiscard(discarded);
				board.notifyWait();
			}

			handleDrawInfectionCard();
			printInfectedCity();

		}
	}
}
