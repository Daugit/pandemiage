package fr.dauphine.ja.student.pandemiage.gameengine;

import fr.dauphine.ja.pandemiage.common.AiInterface;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;

public class AiThread extends Thread {
	private GameInterface game;
	private PlayerInterface player;
	private AiInterface ai;

	public AiThread(GameInterface game, PlayerInterface player, AiInterface ai) {
		super();
		this.game = game;
		this.player = player;
		this.ai = ai;
	}

	@Override
	public void run() {
		ai.playTurn(game, player);
	}

}
