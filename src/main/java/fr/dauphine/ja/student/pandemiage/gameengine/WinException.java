package fr.dauphine.ja.student.pandemiage.gameengine;

/**
 * <b>Exception thrown when game is won and it takes turn numbers</b>
 * 
 * @author avastTeam
 */
public class WinException extends Exception {
	private static final long serialVersionUID = 5818612515970836887L;

	public WinException(String nbTurn) {
		super(nbTurn);
	}

}
