package fr.dauphine.ja.student.pandemiage.ui.view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import fr.dauphine.ja.pandemiage.common.Disease;

/**
 * <b>CardLabel represents player's hand card with city name and city disease
 * color</b>
 * 
 * @author avastTeam
 *
 */
public class CardLabel extends JLabel {

	private static final long serialVersionUID = 3522478962492191999L;

	public CardLabel() {
		this.setCard("", Color.WHITE);
	}

	public CardLabel(String city, Disease disease) {
		Color color = null;
		switch (disease) {
		case BLUE:
			color = new Color(107, 112, 184);
			break;
		case YELLOW:
			color = new Color(242, 255, 0);
			break;
		case BLACK:
			color = new Color(153, 153, 153);
			break;
		case RED:
			color = new Color(153, 18, 21);
			break;
		}
		this.setCard(city, color);
	}

	public void setCard(String city, Color color) {
		this.setPreferredSize(new Dimension(75, 300));
		this.setBackground(color);
		this.setForeground(Color.BLACK);
		this.setOpaque(true);
		this.setText("<html><div style='text-align: center;'>" + city + "</div></html>");
		this.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

	}
}
