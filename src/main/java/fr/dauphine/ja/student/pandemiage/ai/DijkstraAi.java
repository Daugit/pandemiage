package fr.dauphine.ja.student.pandemiage.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.dauphine.ja.pandemiage.common.AiInterface;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;
import fr.dauphine.ja.student.pandemiage.ui.Cli;

/**
 * <p>
 * This AI will find the shortest path to treat a disease, when there are more
 * than 2 infection blocks on a city
 * </p>
 */
public class DijkstraAi implements AiInterface {

	@Override
	public void playTurn(GameInterface g, PlayerInterface p) {
		for (int i = 0; i < 4; ++i) {
			try {
				List<PlayerCardInterface> cureCards = curableDisease(p.playerHand());
				p.discoverCure(cureCards);
				continue;
			} catch (UnauthorizedActionException e) {
			}

			Disease toTreat = infectedCity(g, p);
			if (toTreat != null) {
				try {
					p.treatDisease(toTreat);
					continue;
				} catch (UnauthorizedActionException e) {

				}
			}
			String cityToMove = bestChoiceCityToMove(g, p);
			try {
				p.moveTo(cityToMove);
			} catch (UnauthorizedActionException e) {
			}
		}
	}

	@Override
	public List<PlayerCardInterface> discard(GameInterface g, PlayerInterface p, int maxHandSize, int nbEpidemic) {
		List<PlayerCardInterface> discard = new ArrayList<>();
		List<PlayerCardInterface> hand = p.playerHand();
		int numdiscard = p.playerHand().size() - maxHandSize;

		// Disease mostDisease = mostRepresentedDiseaseNotCured(g, hand);
		for (int i = 0; i < numdiscard; i++) {
			PlayerCardInterface toDiscard = chooseCardToDiscard(g, hand);
			if (toDiscard != null) {
				discard.add(toDiscard);
				hand.remove(toDiscard);
				Cli.printoutln("Discarded card : " + toDiscard);
			} else {
				discard.add(hand.get(0));
				hand.remove(0);
			}
		}
		return discard;
	}

	// METHODS FOR PLAYTURN

	private String bestChoiceCityToMove(GameInterface g, PlayerInterface p) {
		String playerLocation = p.playerLocation();

		String cityChoice = dijkstraShortestPath(g, playerLocation);
		return cityChoice;
	}

	/**
	 * Return the nearest neighbourg to go or the cost (depending the parameter
	 * OPTION) of the shortest path to the nearest "curable city" (following
	 * Dijkstra algorithm). A city is curable if it has 2 or more infection blocks
	 * of the same disease.
	 */
	private String dijkstraShortestPath(GameInterface g, String from) {
		List<String> fixed = new ArrayList<>();
		fixed.add(from);

		// Initiation of map, giving for each city, its predecessor in the Dijkstra
		// algorithm
		Map<String, String> mapCityPredecessor = new HashMap<>();
		for (String string : g.allCityNames()) {
			mapCityPredecessor.put(string, null);
		}

		// Initiation of map, giving for each city, its cost in the Dijkstra
		// algorithm
		Map<String, Integer> mapCityValues = new HashMap<>();
		for (String string : g.allCityNames()) {
			mapCityValues.put(string, 999999);
		}
		mapCityValues.put(from, 0);

		// Apply Dijkstra algorithm, stop when passing on a city with 2 or more disease
		// block of the same disease
		String currentCity = from;
		Integer currentCityValue = mapCityValues.get(currentCity);
		List<String> neighbours;
		while (fixed.size() < 48 && !toCure(g, currentCity)) {
			neighbours = g.neighbours(currentCity);

			for (String city : neighbours) {
				if (!fixed.contains(city)) {
					if (mapCityValues.get(city) > currentCityValue + 1) {
						mapCityValues.put(city, currentCityValue + 1);
						mapCityPredecessor.put(city, currentCity);
					}
				}
			}

			currentCity = cityWithMinValue(mapCityValues, fixed);
			currentCityValue = mapCityValues.get(currentCity);
			fixed.add(currentCity);
		}

		// if the player is already on a city to cure
		if (from.equals(currentCity)) {
			return from;
		}

		// Retreive the first city of the path, from "from" to the city to cure
		String predecessor = currentCity;
		while (!mapCityPredecessor.get(predecessor).equals(from)) {
			predecessor = mapCityPredecessor.get(predecessor);
		}
		return predecessor;
	}

	/**
	 * Return a city that is not fixed, with the smallest value/cost
	 */
	private String cityWithMinValue(Map<String, Integer> mapCityValues, List<String> fixed) {
		Integer minValue = 999999;
		String minCity = null;

		for (Entry<String, Integer> cityValueEntry : mapCityValues.entrySet()) {
			if (!fixed.contains(cityValueEntry.getKey()) && cityValueEntry.getValue() < minValue) {
				minValue = cityValueEntry.getValue();
				minCity = cityValueEntry.getKey();
			}
		}
		return minCity;
	}

	/**
	 * Tell if a city has to be cured or not (if the number of disease blocks of the
	 * same disease >= 2)
	 */
	private boolean toCure(GameInterface g, String city) {
		for (Disease d : Disease.values()) {
			if (g.infectionLevel(city, d) >= 2) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If the current city (where the player is) has 1 or more infection blocks of
	 * the same disease, returns the disease. Return null otherwise.
	 */
	private Disease infectedCity(GameInterface g, PlayerInterface p) {
		for (Disease d : Disease.values()) {
			if (g.infectionLevel(p.playerLocation(), d) >= 1) {
				return d;
			}
		}
		return null;
	}

//METHODS FOR DISCARD

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

	/**
	 * Return a card that is not the most representated on the player hand. Try to
	 * discard a card which disease has been cured. Return null if all the cards on
	 * the player hand have the same disease
	 * 
	 * @param d a disease that isn't cured, that represents the most representative
	 *          disease in the player hand
	 */
	private PlayerCardInterface chooseCardToDiscard(GameInterface g, List<PlayerCardInterface> hand) {
		for (PlayerCardInterface playerCardInterface : hand) {
			if (g.isCured(playerCardInterface.getDisease()))
				return playerCardInterface;
		}
		Disease lessRepresentedDisease = lessRepresentedDisease(g, hand);
		for (PlayerCardInterface playerCardInterface : hand) {
			if (playerCardInterface.getDisease().equals(lessRepresentedDisease))
				return playerCardInterface;
		}

		return null;
	}

	/**
	 * Gives the disease that is the less represented in the player hand. The number
	 * of card with this disease has to been >0.
	 */
	private Disease lessRepresentedDisease(GameInterface g, List<PlayerCardInterface> hand) {
		Disease less = Disease.BLACK;
		int minCount = 999999;

		for (Disease d : Disease.values()) {
			int count = 0;
			for (PlayerCardInterface playerCard : hand) {
				if (d.equals(playerCard.getDisease())) {
					count++;
				}
			}
			if (count < minCount && count > 0) {
				minCount = count;
				less = d;
			}
		}

		return less;
	}
}
