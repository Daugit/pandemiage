package fr.dauphine.ja.student.pandemiage.ui;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App {
	public static final String DEFAULT_AIJAR = "./pandemiage-1.0-SNAPSHOT-ai.jar";
	public static final String DEFAULT_CITYGRAPH_FILE = "./pandemic.graphml";
	public static final int DEFAULT_TURN_DURATION = 1; // in seconds
	public static final int DEFAULT_DIFFICULTY = 0; // Normal
	public static final int DEFAULT_HAND_SIZE = 9;

	private static String aijar = DEFAULT_AIJAR;
	private static String cityGraphFile = DEFAULT_CITYGRAPH_FILE;
	private static int difficulty = DEFAULT_DIFFICULTY;
	private static int turnDuration = DEFAULT_TURN_DURATION;
	private static int handSize = DEFAULT_HAND_SIZE;

	public static void main(String[] args) throws IOException, XMLStreamException {

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

		Gui gui = new Gui(aijar, cityGraphFile, difficulty, turnDuration, handSize);
		gui.initializeMainMenu();

	}

}
