package fr.dauphine.ja.pandemiage.Model;

import java.util.List;

import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;
import fr.dauphine.ja.student.pandemiage.ui.Cli;

public class Scientist extends Player {

	public static final int NBCARDCURE = 4;

	public Scientist(GameInterface game, Board board) {
		super(game, board);
	}

	@Override
	public void discoverCure(List<PlayerCardInterface> cardNames) throws UnauthorizedActionException {
		if (board.getActionLeft() > 0) {
			if (cardNames.size() == NBCARDCURE) {
				Disease toTreat = cardNames.get(0).getDisease();
				if (board.getCuredDiseases().contains(toTreat)) {
					throw new UnauthorizedActionException();
				}
				for (PlayerCardInterface playerCardInterface : cardNames) {
					if (!playerCardInterface.getDisease().equals(toTreat)) {
						throw new UnauthorizedActionException();
					}
				}
				board.getCuredDiseases().add(toTreat);
				handleDiscardPlayerCards(cardNames);

				Cli.printoutln("=================  CURED DISEASE: " + toTreat + " =========================");
			} else {
				throw new UnauthorizedActionException();
			}
		} else {
			throw new UnauthorizedActionException();
		}

	}

}
