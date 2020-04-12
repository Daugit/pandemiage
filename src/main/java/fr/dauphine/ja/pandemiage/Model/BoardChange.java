package fr.dauphine.ja.pandemiage.Model;

import fr.dauphine.ja.student.pandemiage.ui.view.PandemiageGUI;

/**
 * <b>Each enum represents a board state change, it helps the observable to
 * notify the observer who will update targeted view elements</b>
 * 
 * @see Board
 * @see PandemiageGUI
 * @author avastTeam
 */
public enum BoardChange {
	DRAW_CARD, ADD_CARD, INCR_INFECTIONRATE, OUTBREAK, INFECTION, MOVE_TO, WAIT_USER, TREAT_DISEASE, INCR_BLOCK_DISEASE,
	CURE_DISEASE, UPDATE_MAP, DISCARD, NEW_TURN, DEFEAT, VICTORY, HUMAN_DISCARD, CARD_TO_DISCARD, NO_DISCARD, USE_CARD,
	UPDATEFLY

}
