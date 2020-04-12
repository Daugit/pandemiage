package fr.dauphine.ja.student.pandemiage.ai;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.dauphine.ja.pandemiage.common.AiInterface;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;

/**
 * Ajouter le temps max de réflexion
 *
 */
public class StupidAi implements AiInterface {
	private final static Logger LOGGER = LoggerFactory.getLogger(StupidAi.class);

	@Override
	public void playTurn(GameInterface g, PlayerInterface p) {
		for (int i = 0; i < 4; ++i) {
			try {
				List<PlayerCardInterface> cureCards = curableDisease(p.playerHand());
				p.discoverCure(cureCards);
			} catch (UnauthorizedActionException e) {
				LOGGER.info(e.getMessage());
			}
		}
	}

	@Override
	public List<PlayerCardInterface> discard(GameInterface g, PlayerInterface p, int maxHandSize, int nbEpidemic) {
		List<PlayerCardInterface> discard = new ArrayList<>();
		List<PlayerCardInterface> hand = p.playerHand();
		int numdiscard = p.playerHand().size() - maxHandSize;

		Disease mostDisease = mostDisease(hand);
		for (int i = 0; i < numdiscard; i++) {
			PlayerCardInterface toDiscard = containsOtherThan(mostDisease, hand);
			if (toDiscard != null) {
				discard.add(toDiscard);
				hand.remove(toDiscard);
			} else {
				discard.add(hand.get(0));
				hand.remove(0);
			}
		}
		return discard;
	}

	private List<PlayerCardInterface> curableDisease(List<PlayerCardInterface> playerHand) {
		for (Disease d : Disease.values()) {
			List<PlayerCardInterface> cards = new ArrayList<>();
			for (PlayerCardInterface playerCard : playerHand) {
				if (d.equals(playerCard.getDisease())) {
					cards.add(playerCard);
				}
			}
			if (cards.size() >= GameEngine.NBCARDCURE) {
				while (cards.size() > GameEngine.NBCARDCURE) {
					cards.remove(cards.size() - 1);
				}
				return cards;
			}
		}
		return new ArrayList<>();
	}

	private PlayerCardInterface containsOtherThan(Disease d, List<PlayerCardInterface> hand) {
		for (PlayerCardInterface playerCardInterface : hand) {
			if (!playerCardInterface.getDisease().equals(d))
				return playerCardInterface;
		}
		return null;
	}

	private Disease mostDisease(List<PlayerCardInterface> hand) {
		Disease most = Disease.BLACK;
		int maxCount = 0;

		for (Disease d : Disease.values()) {
			int count = 0;
			for (PlayerCardInterface playerCard : hand) {
				if (d.equals(playerCard.getDisease())) {
					count++;
				}
			}
			if (count > maxCount) {
				maxCount = count;
				most = d;
			}
		}

		return most;
	}

}
