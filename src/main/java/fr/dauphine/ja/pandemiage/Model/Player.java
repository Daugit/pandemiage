package fr.dauphine.ja.pandemiage.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;
import fr.dauphine.ja.student.pandemiage.ui.Cli;

/**
 * <b>This class represents a game player (pawn in irl) and its actions on
 * board</b>
 * 
 * @author avastTeam
 */
public class Player implements PlayerInterface {

	// GAME OBJECTS
	private GameInterface game;
	protected Board board;

	/**
	 * player's location corresponds to a city name
	 */
	protected String location;

	// ACTIONS

	public Player(GameInterface game, Board board) {

		this.game = Objects.requireNonNull(game);
		this.board = Objects.requireNonNull(board);
	}

	@Override
	public void moveTo(String cityName) throws UnauthorizedActionException {
		if (board.getActionLeft() > 0) {
			if (game.neighbours(board.getLocation()).contains(cityName) && board.getActionLeft() > 0) {
				Cli.printoutln("Player moved to : " + cityName);
				board.setLocation(cityName);
				board.decrementActionLeft();
				board.notifyWait();
			} else {
				throw new UnauthorizedActionException();
			}
		} else {
			throw new UnauthorizedActionException();

		}

	}

	@Override
	public void flyTo(String cityName) throws UnauthorizedActionException {
		if (board.getActionLeft() > 0) {
			for (PlayerCardInterface playerCardInterface : board.getHand()) {
				if (playerCardInterface.getCityName().equals(cityName)) {
					handleDiscardPlayerCards(playerCardInterface);
					board.setLocation(cityName);
					board.decrementActionLeft();
					board.notifyWait();
					break;
				}
			}
			throw new UnauthorizedActionException();

		} else {
			throw new UnauthorizedActionException();
		}
	}

	@Override
	public void flyToCharter(String cityName) throws UnauthorizedActionException {
		if (board.getActionLeft() > 0) {
			for (PlayerCardInterface playerCardInterface : playerHandCopie()) {
				if (playerCardInterface.getCityName().equals(board.getLocation())) {
					handleDiscardPlayerCards(playerCardInterface);
					board.setLocation(cityName);
					board.decrementActionLeft();
					board.notifyWait();

					return;
				}
			}
			throw new UnauthorizedActionException();

		} else {
			throw new UnauthorizedActionException();
		}
	}

	@Override
	public void skipTurn() {
		board.setActionLeft(0);
	}

	@Override
	public void treatDisease(Disease d) throws UnauthorizedActionException {
		if (board.getActionLeft() > 0) {
			List<Disease> diseases = getInfections(board.getLocation());
			if (diseases.contains(d)) {
				board.decrementActionLeft();
				if (game.isCured(d)) {
					handleDiseaseClear(d, diseases.size());
					Cli.printoutln("Treat disease : " + d);
				} else {
					handleDiseaseDeletion(d, diseases);
					Cli.printoutln("Treat disease : " + d);
				}
				board.notifyWait();

			} else {
				throw new UnauthorizedActionException();
			}
		} else {
			throw new UnauthorizedActionException();
		}

	}

	@Override
	public void discoverCure(List<PlayerCardInterface> cardNames) throws UnauthorizedActionException {
		if (board.getActionLeft() > 0) {
			if (cardNames.size() == GameEngine.NBCARDCURE) {
				Disease toTreat = cardNames.get(0).getDisease();
				if (board.getCuredDiseases().contains(toTreat)) {
					throw new UnauthorizedActionException();
				}
				for (PlayerCardInterface playerCardInterface : cardNames) {
					if (!playerCardInterface.getDisease().equals(toTreat)) {
						throw new UnauthorizedActionException();
					}
				}
				board.addCuredDiseases(toTreat);
				handleDiscardPlayerCards(cardNames);

				Cli.printoutln("=================  CURED DISEASE: " + toTreat + " =========================");
				if (board.getCuredDiseases().size() == 4) {
					board.setActionLeft(0);
				}
				board.notifyWait();

			} else {
				throw new UnauthorizedActionException();
			}
		} else {
			throw new UnauthorizedActionException();
		}

	}

	@Override
	public String playerLocation() {
		return board.getLocation();
	}

	@Override
	public List<PlayerCardInterface> playerHand() {
		return board.getHand();
	}

	private List<PlayerCardInterface> playerHandCopie() {
		List<PlayerCardInterface> result = new LinkedList<>();
		Collections.copy(result, board.getHand());
		return result;
	}

	private void handleDiscardPlayerCards(PlayerCardInterface playerCardInterface) {
		board.notifyUsedCard(playerCardInterface);
		board.getDiscardPlayerDeck().add(playerCardInterface);
	}

	protected void handleDiscardPlayerCards(List<PlayerCardInterface> playerCards) {
		board.removeHand(playerCards);
		board.getDiscardPlayerDeck().addAll(playerCards);
	}

	protected List<Disease> getInfections(String city) {
		return board.getMapInfections().get(city);
	}

	/**
	 * Remove all the diseases from a list of disease and add the disease blocks to
	 * the mapDiseaseBlocks
	 * 
	 * @param d
	 * @param diseases the list of disease (reference) attached to the city we want
	 *                 to delete a disease block
	 */
	protected void handleDiseaseClear(Disease d, int nbDisease) {
		board.setDiseaseBlocks(d, board.getDiseaseBlocks().get(d) + nbDisease);
		board.setCityDisease(board.getLocation(), new ArrayList<>());
	}

	/**
	 * Remove one disease from a list of disease and add the disease block to the
	 * mapDiseaseBlocks
	 * 
	 * @param d
	 * @param diseases the list of disease (reference) attached to the city we want
	 *                 to delete a disease block
	 */
	protected void handleDiseaseDeletion(Disease d, List<Disease> diseases) {
		diseases.remove(d);
		board.setCityDisease(board.getLocation(), diseases);
		board.setDiseaseBlocks(d, board.getDiseaseBlocks().get(d) + 1);
	}
}
