package fr.dauphine.ja.student.pandemiage.ui.view;

import java.util.List;

import fr.dauphine.ja.pandemiage.Model.BoardChange;

public interface Observer {
	public void update(String str, List<BoardChange> boardChanges);
}
