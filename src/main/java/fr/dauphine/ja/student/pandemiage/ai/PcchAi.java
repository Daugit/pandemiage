package fr.dauphine.ja.student.pandemiage.ai;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import fr.dauphine.ja.pandemiage.Model.Board;
import fr.dauphine.ja.pandemiage.common.AiInterface;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;
import fr.dauphine.ja.student.pandemiage.gameengine.AiGameEngine;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;
import fr.dauphine.ja.student.pandemiage.ui.Cli;

public class PcchAi implements AiInterface {

	@Override
	public void playTurn(GameInterface g, PlayerInterface p) {
		for (int i = 0; i < 4; ++i) {
			while (true) {
				try {
					List<PlayerCardInterface> cureCards = curableDisease(p.playerHand());
					p.discoverCure(cureCards);
					break;
				} catch (UnauthorizedActionException e) {
				}

				String cityToMove = bestChoiceCityToMove(g, p);
				try {
					p.moveTo(cityToMove);
					break;
				} catch (UnauthorizedActionException e) {
				}
			}
		}
	}

	private String bestChoiceCityToMove(GameInterface g, PlayerInterface p) {
		String playerLocation = p.playerLocation();
		String nearestCityWithOutbreak = nearestCityWithOutbreak();

		String cityChoice = shortestPath(g, playerLocation, nearestCityWithOutbreak);
		return cityChoice;
	}

	private String shortestPath(GameInterface g, String from, String to) {
		List<String> neighbours = g.neighbours(from);
		int nbMinMove = 100;
		String bestCity = neighbours.get(0);

		List<String> visited = new ArrayList<>();
		visited.add(from);

		for (String city : neighbours) {
			visited.add(city);

			int nbMove = 1 + move(g, city, to, visited);
			visited.remove(city);

			if (nbMove < nbMinMove) {
				nbMinMove = nbMove;
				bestCity = city;
			}
		}
		return bestCity;
	}

	private int move(GameInterface g, String from, String to, List<String> visited) {

		List<String> neighbours = g.neighbours(from);
		int nbMinMove = 100;

		for (String city : neighbours) {
			if (!visited.contains(city)) {
				if (city.equals(to)) {
					return 1;
				}
				visited.add(city);
				int nbMoveCity = 1 + move(g, city, to, visited);
				visited.remove(city);

				if (nbMoveCity < nbMinMove) {
					nbMinMove = nbMoveCity;
				}
			}
		}

		return nbMinMove;
	}

	public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
		String aijar = Cli.DEFAULT_AIJAR;
		String cityGraphFile = "./pandemic.graphml";
		GameInterface g = new AiGameEngine(new Board(cityGraphFile), aijar, 0, 1, 9, false);
		PcchAi ai = new PcchAi();

		List<String> str = new ArrayList<>();
		str.add("Chicago");
		System.out.println(ai.move(g, "Chicago", "Essen", str));

	}

	private String nearestCityWithOutbreak() {
		// TODO Auto-generated method stub
		return null;
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
