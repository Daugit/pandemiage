package fr.dauphine.ja.student.pandemiage.controller;

import java.util.ArrayList;
import java.util.List;

import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;

public class GuiController {
	private PlayerInterface player;
	private GameEngine game;

	public GuiController(GameEngine game, PlayerInterface player) {
		this.player = player;
		this.game = game;
	}

	public void moveTo(String name) {
		try {
			player.moveTo(name);
		} catch (UnauthorizedActionException e) {

		}

	}

	public void treatDisease() {
		try {
			player.treatDisease(this.infectedCity());
		} catch (UnauthorizedActionException e) {

		}
	}

	public void cureDisease() {
		try {
			player.discoverCure(this.curableDisease());
		} catch (UnauthorizedActionException e) {
		}
	}

	private List<PlayerCardInterface> curableDisease() {
		for (Disease d : Disease.values()) {
			List<PlayerCardInterface> cards = new ArrayList<>();
			for (PlayerCardInterface playerCard : player.playerHand()) {
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
	 * If the current city (where the player is) has 1 or more infection blocks of
	 * the same disease, returns the disease. Return null otherwise.
	 */
	private Disease infectedCity() {
		for (Disease d : Disease.values()) {
			if (game.infectionLevel(player.playerLocation(), d) >= 1) {
				return d;
			}
		}
		return null;
	}

	public String getLocation() {
		return player.playerLocation();
	}

	public void flyTo(String cityName) {
		try {
			player.flyTo(cityName);
		} catch (UnauthorizedActionException e) {

		}

	}

}
