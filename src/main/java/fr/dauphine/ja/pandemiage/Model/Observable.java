package fr.dauphine.ja.pandemiage.Model;

import java.util.List;

import fr.dauphine.ja.student.pandemiage.ui.view.Observer;

/**
 * <b>Observable object notifies its observers about the changes in its
 * state</b>
 * 
 * @author avastTeam
 */
public interface Observable {
	public void addObserver(Observer obs);

	public void removeObserver();

	/**
	 * @param msg          a message for observers
	 * @param boardChanges corresponding to types of changes to notify
	 */
	public void notifyObserver(String msg, List<BoardChange> boardChanges);
}
