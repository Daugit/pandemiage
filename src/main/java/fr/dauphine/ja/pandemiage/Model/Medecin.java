package fr.dauphine.ja.pandemiage.Model;

import java.util.List;

import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;
import fr.dauphine.ja.student.pandemiage.ui.Cli;

/**
 * This player has the ability to treat all the disease block on a city with
 * only one action
 *
 */
public class Medecin extends Player {

	public Medecin(GameInterface game, Board board) {
		super(game, board);
	}

	@Override
	public void treatDisease(Disease d) throws UnauthorizedActionException {
		if (board.getActionLeft() > 0) {
			List<Disease> diseases = getInfections(location);
			if (diseases.contains(d)) {
				board.decrementActionLeft();
				handleDiseaseClear(d, diseases.size());
				Cli.printoutln("Treat disease : " + d);
			} else {
				throw new UnauthorizedActionException();
			}
		} else {
			throw new UnauthorizedActionException();
		}

	}
}
