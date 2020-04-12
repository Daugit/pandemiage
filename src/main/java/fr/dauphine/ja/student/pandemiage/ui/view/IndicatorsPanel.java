package fr.dauphine.ja.student.pandemiage.ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.dauphine.ja.pandemiage.Model.Board;

public class IndicatorsPanel extends JPanel {

	private Board gameBoard = null;
	private JLabel turnIndicator = null;

	public IndicatorsPanel(Board board) {
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(1700, 50));
		gameBoard = board;
		Font font = new Font("Serif", Font.BOLD, 20);
		JLabel indicationLabel = new JLabel(
				"             Notice : In each city there are 4 numbers corresponding to each disease block(s) in this order : BLUE YELLOW  BLACK RED");
		indicationLabel.setFont(font);
		indicationLabel.setBackground(new Color(92, 83, 92));
		indicationLabel.setForeground(new Color(54, 148, 87));
		indicationLabel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(3, 34, 2)));
		indicationLabel.setOpaque(true);
		indicationLabel.setPreferredSize(new Dimension(1200, 50));
		this.add(indicationLabel, BorderLayout.EAST);

		turnIndicator = new JLabel("");
		turnIndicator.setFont(font);
		turnIndicator.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, new Color(3, 34, 2)));
		turnIndicator.setBackground(new Color(68, 82, 122));
		turnIndicator.setForeground(Color.WHITE);
		turnIndicator.setPreferredSize(new Dimension(500, 50));
		turnIndicator.setOpaque(true);
		this.add(turnIndicator, BorderLayout.WEST);
		updateTurnLabel();
	}

	public void updateTurnLabel() {
		turnIndicator.setText("  Turn: " + String.valueOf(gameBoard.getTurn()));
		turnIndicator.validate();
	}

}
