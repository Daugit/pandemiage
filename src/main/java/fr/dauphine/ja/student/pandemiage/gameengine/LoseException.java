package fr.dauphine.ja.student.pandemiage.gameengine;

/**
 * <b>Exception thrown when game is lost and it takes turn numbers</b>
 * 
 * @author avastTeam
 */
public class LoseException extends Exception {
	private static final long serialVersionUID = 5818612515970836887L;

	public LoseException(String nbTurn) {
		super(nbTurn);
	}

}
