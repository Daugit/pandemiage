package fr.dauphine.ja.student.pandemiage.ui;

import java.io.FileNotFoundException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.dauphine.ja.pandemiage.Model.Board;
import fr.dauphine.ja.pandemiage.common.DefeatReason;
import fr.dauphine.ja.student.pandemiage.gameengine.AiGameEngine;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;
import fr.dauphine.ja.student.pandemiage.gameengine.LoseException;
import fr.dauphine.ja.student.pandemiage.gameengine.WinException;

public class Cli {
	private final static Logger LOGGER = LoggerFactory.getLogger(Cli.class);
	public static final boolean PRINTOUT = false;
	public static final int REPETITION = 10000;
	public static final int NUMBERCITY = 48;

	public static final String DEFAULT_AIJAR = "./pandemiage-1.0-SNAPSHOT-ai.jar";
	public static final String DEFAULT_CITYGRAPH_FILE = "./pandemic.graphml";
	public static final int DEFAULT_TURN_DURATION = 1; // in seconds
	public static final int DEFAULT_DIFFICULTY = 0; // Normal
	public static final int DEFAULT_HAND_SIZE = 9;

	public static void main(String[] args) throws FileNotFoundException, XMLStreamException {

		String aijar = DEFAULT_AIJAR;
		String cityGraphFile = DEFAULT_CITYGRAPH_FILE;
		int difficulty = DEFAULT_DIFFICULTY;
		int turnDuration = DEFAULT_TURN_DURATION;
		int handSize = DEFAULT_HAND_SIZE;

		Options options = new Options();
		CommandLineParser parser = new DefaultParser();

		options.addOption("a", "aijar", true, "use <FILE> as player Ai.");
		options.addOption("d", "difficulty", true, "Difficulty level. 0 (Introduction), 1 (Normal) or 3 (Heroic).");
		options.addOption("c", "citygraph", true, "City graph filename.");
		options.addOption("t", "turnduration", true, "Number of seconds allowed to play a turn.");
		options.addOption("s", "handsize", true, "Maximum size of a player hand.");
		options.addOption("h", "help", false, "Display this help");

		try {
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("a")) {
				aijar = cmd.getOptionValue("a");
			}

			if (cmd.hasOption("c")) {
				cityGraphFile = cmd.getOptionValue("c");
			}

			if (cmd.hasOption("d")) {
				difficulty = Integer.parseInt(cmd.getOptionValue("d"));
			}

			if (cmd.hasOption("t")) {
				turnDuration = Integer.parseInt(cmd.getOptionValue("t"));
			}
			if (cmd.hasOption("s")) {
				handSize = Integer.parseInt(cmd.getOptionValue("s"));
			}

			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("pandemiage", options);
				System.exit(0);
			}

		} catch (ParseException e) {
			System.err.println("Error: invalid command line format.");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pandemiage", options);
			System.exit(1);
		}

		GameEngine g;
		int sumOutBreaks = 0;
		int sumNoMoreBlocks = 0;
		int sumNoMorePlayerCards = 0;

		int sumVictory = 0;
		int sumDefeat = 0;

		Board board = new Board(cityGraphFile);

		for (int i = 0; i < REPETITION; i++) {
			try {
				g = new AiGameEngine(new Board(board), aijar, difficulty, turnDuration, handSize, false);
				try {
					g.loop();
				} catch (LoseException e) {
					sumDefeat++;
					String defeatReason = e.getMessage();
					if (defeatReason.equals(DefeatReason.NO_MORE_BLOCKS.toString())) {
						sumNoMoreBlocks++;
					}
					if (defeatReason.equals(DefeatReason.TOO_MANY_OUTBREAKS.toString())) {
						sumOutBreaks++;
					}
					if (defeatReason.equals(DefeatReason.NO_MORE_PLAYER_CARDS.toString())) {
						sumNoMorePlayerCards++;
					}

				} catch (WinException e) {
					sumVictory++;
				} catch (InterruptedException e) {

				}
			} catch (FileNotFoundException e) {
				LOGGER.error("XML city graph file not found, please select a valid file");
			} catch (XMLStreamException e) {
				LOGGER.error("XML city graph file not valid, please verify the XML format");
			}
		}

		System.out.println("Wins : " + sumVictory + "; Defeats: " + sumDefeat + "\n NO_MORE_BLOCKS : " + sumNoMoreBlocks
				+ "; TOO_MANY_OUTBREAKS : " + sumOutBreaks + "; NO_MORE_PLAYER_CARDS : " + sumNoMorePlayerCards);
	}

	public static void printoutln(String msg) {
		if (Cli.PRINTOUT) {
			System.out.println(msg);
		}
	}

	public static void printout(String msg) {
		if (Cli.PRINTOUT) {
			System.out.print(msg);
		}
	}

	public static void printerrln(String msg) {
		if (Cli.PRINTOUT) {
			System.err.println(msg);
		}
	}
}
