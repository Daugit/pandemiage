package fr.dauphine.ja.student.pandemiage.gameengine;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import fr.dauphine.ja.pandemiage.Model.Board;
import fr.dauphine.ja.pandemiage.Model.BoardChange;
import fr.dauphine.ja.pandemiage.Model.Card;
import fr.dauphine.ja.pandemiage.Model.City;
import fr.dauphine.ja.pandemiage.Model.Player;
import fr.dauphine.ja.pandemiage.common.DefeatReason;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.GameStatus;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.student.pandemiage.ui.Cli;

/**
 * <b>If the Board is the model's body then the GameEngin is its heart</b>
 * <p>
 * This class set up the game rules (settings) and contains the main game loop
 * </p>
 * 
 * @see Board
 * @author avastTeam
 *
 */
public abstract class GameEngine implements GameInterface {

	/**
	 * Indicates if the launched game has a gui or not
	 */
	private static boolean hasGUI = false;

	// Rules settings

	public static final String STARTCITY = "Atlanta";
	public static final int[] INFECTIONRATETAB = { 2, 2, 2, 3, 3, 4, 4 };
	public static final int MAXOUTBREAKS = 8;
	public static final int NBINITIALCARDS = 5;
	public static final int NBINFECTIONBLOCK = 24;
	public static final int NBPLAYERCARDSPERTURN = 2;
	public static final int NBBLOCKEPIDEMY = 3;

	/**
	 * Number of cards of the same disease necessary to have a cure
	 */
	public static final int NBCARDCURE = 5;
	public static final int ACTIONPERTURN = 4;

	/**
	 * Max duration of a turn (in second)
	 */
	public int TURNDURATION;

	/**
	 * Max cards number in player's hand
	 */
	public int MAXPLAYERCARD;

	// Game objects

	/**
	 * AI jar file
	 */
	protected final String aiJar;

	/**
	 * Contains all the dynamic game objects that a player could access
	 */
	protected final Board board;

	protected final PlayerInterface player;

	// GAME STATUS

	/**
	 * Indicates the state of the running game
	 */
	protected GameStatus gameStatus;

	/**
	 * Gives the list of cities that already detected an Outbreak
	 */
	private List<String> citiesOutbroken;

	/**
	 * This deck is necessary for infection phase
	 */
	private LinkedList<PlayerCardInterface> infectionDeck;

	private LinkedList<PlayerCardInterface> discardInfectionDeck;

	/**
	 * @param handSize     the maximum hand size
	 * @param turnDuration the duration of a turn for the IA/player
	 * @param difficulty   the difficulty of the game (see rules)
	 */
	public GameEngine(Board board, String aiJar, int difficulty, int turnDuration, int handSize, boolean hasGUI)
			throws FileNotFoundException, XMLStreamException {
		this.aiJar = aiJar;
		this.gameStatus = GameStatus.ONGOING;
		this.board = board;
		this.TURNDURATION = turnDuration;
		this.MAXPLAYERCARD = handSize;
		GameEngine.hasGUI = hasGUI;
		infectionDeck = getInitialInfectionDeck();
		discardInfectionDeck = new LinkedList<>();
		citiesOutbroken = new ArrayList<>();

		player = new Player(this, board);
		board.setPlayer(player);

		switch (difficulty) {
		case 0:
			board.setNbEpidemyCards(4);
			break;
		case 1:
			board.setNbEpidemyCards(5);
			break;
		case 2:
			board.setNbEpidemyCards(6);
			break;
		default:
			board.setNbEpidemyCards(4);
		}
	}

	// MAIN METHODS

	/**
	 * <p>
	 * Main game loop which loads the AI, sets up the board and run game turns until
	 * win or lose
	 * </p>
	 * 
	 * @throws LoseException
	 * @throws WinException
	 * @throws InterruptedException
	 */
	public abstract void loop() throws LoseException, WinException, InterruptedException;

	protected String printPlayerHand(PlayerInterface player) {
		List<PlayerCardInterface> playerHand = player.playerHand();
		playerHand.sort(new Comparator<PlayerCardInterface>() {

			@Override
			public int compare(PlayerCardInterface o1, PlayerCardInterface o2) {
				return o1.getCityName().compareTo(o2.getCityName());
			}
		});

		StringBuilder result = new StringBuilder();

		for (PlayerCardInterface playerCardInterface : playerHand) {
			result.append((playerCardInterface.getCityName() + "|" + playerCardInterface.getDisease() + "\n"));
		}
		return result.toString();
	}

	/**
	 * TODO : Optimiser l'affichage pour avoir une vue d'ensemble
	 */
	protected void printInfectedCity() {
		Cli.printoutln("Infected city: " + toStringCityInfection(infectedCityMapInfection(board.getMapInfections())));
	}

	// PRINTOUT

	private StringBuilder toStringCityInfection(Map<String, List<Disease>> infectedCityMapInfection) {
		StringBuilder str = new StringBuilder("City	 |	Infections \n");

		for (Entry<String, List<Disease>> entry : infectedCityMapInfection.entrySet()) {
			str.append(entry.getKey() + "  |  " + entry.getValue() + "\n");
		}
		return str;
	}

	protected void printTurn(int turn) {

		Cli.printoutln("\n\n================ Turn " + turn + " ===================\n");
	}

	private Map<String, List<Disease>> infectedCityMapInfection(Map<String, List<Disease>> mapInfections) {
		Map<String, List<Disease>> map = new TreeMap<>();

		Set<Entry<String, List<Disease>>> set = mapInfections.entrySet();

		for (Entry<String, List<Disease>> entry : set) {
			if (entry.getValue().size() != 0) {
				map.put(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}

	/**
	 * Draw player cards and update the player card hand. If there are epidemic
	 * card, apply the corresponding actions (see rules).
	 * 
	 * @throws LoseException
	 */
	protected int drawPlayerCardPhase() throws LoseException {
		int nbepidemiccard = 0;
		for (int i = 0; i < NBPLAYERCARDSPERTURN; i++) {
			nbepidemiccard += handleDrawPlayerCard();
		}
		return nbepidemiccard;
	}

	/**
	 * Infect the city with the disease, adding nbBlocks, and clear at the end the
	 * list of citiesOutbroken
	 */
	private void handleInfection(Disease disease, int nbBlocks, String cityName) throws LoseException {
		infect(disease, nbBlocks, cityName);
		citiesOutbroken.clear();
	}

	/**
	 * Put nbBlocks of a disease d into city CityName. Handles outbreaks if the
	 * total of blocks of this disease > 3.
	 * 
	 * @throws LoseException
	 */
	private void infect(Disease disease, int nbBlocks, String cityName) throws LoseException {
		// Update on GUI
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.INFECTION);
		board.notifyObserver(cityName, b);

		int nbBlocksDisease = infectionLevel(cityName, disease);
		if (nbBlocksDisease + nbBlocks > 3) {
			increaseOutBreaks(cityName);
			List<String> neighbours = board.getMapNeighbourgs().get(cityName);

			citiesOutbroken.add(cityName);
			int nbToAdd = (nbBlocksDisease == 1) ? 2 : ((nbBlocksDisease == 2) ? 1 : 0);

			for (int i = 0; i < nbToAdd; i++) {
				decreaseDiseaseBlocks(disease, 1);
				board.getMapInfections().get(cityName).add(disease);
			}
			Cli.printerrln("================ INFECT CITY : " + cityName + " ; List disease: "
					+ board.getMapInfections().get(cityName) + "   =====================");
			Cli.printerrln("============== OUTBREAK: city=" + cityName + " | disease=" + disease + " | neighbours="
					+ neighbours + " ==================");

			for (String neighbour : neighbours) {
				if (!citiesOutbroken.contains(neighbour)) {
					infect(disease, 1, neighbour);
				}
			}

		} else {
			for (int i = 0; i < nbBlocks; i++) {
				board.getMapInfections().get(cityName).add(disease);
			}
			decreaseDiseaseBlocks(disease, nbBlocks);
			Cli.printerrln("================ INFECT CITY : " + cityName + " ; List disease: "
					+ board.getMapInfections().get(cityName) + "   =====================");
		}

	}

	private void increaseOutBreaks(String city) throws LoseException {
		board.incrementNbOutBreaks(city);
		checkTooManyOutbreaks();

	}

	private void decreaseDiseaseBlocks(Disease disease, int nb) throws LoseException {
		checkNoMoreBlocks(disease, nb);
		board.getDiseaseBlocks().put(disease, board.getDiseaseBlocks().get(disease) - nb);
	}

	// INITIALISATION

	/**
	 * Draw infection cards and infect cities following rules
	 * 
	 * @throws LoseException
	 * 
	 */
	protected void initialInfection() throws LoseException {

		for (int i = 0; i < 3; i++) {
			PlayerCardInterface toInfect = drawInfectionCard();
			handleInfection(toInfect.getDisease(), 3, toInfect.getCityName());
			discardInfectionCard(toInfect);
		}
		for (int i = 0; i < 3; i++) {
			PlayerCardInterface toInfect = drawInfectionCard();
			handleInfection(toInfect.getDisease(), 2, toInfect.getCityName());
			discardInfectionCard(toInfect);
		}
		for (int i = 0; i < 3; i++) {
			PlayerCardInterface toInfect = drawInfectionCard();
			handleInfection(toInfect.getDisease(), 1, toInfect.getCityName());
			discardInfectionCard(toInfect);
		}

	}

	/**
	 * @return the initial deck of infection cards
	 */
	private LinkedList<PlayerCardInterface> getInitialInfectionDeck() {
		LinkedList<PlayerCardInterface> infectionDeck = new LinkedList<>();
		for (City city : board.getCities()) {
			PlayerCardInterface playerCard = new Card(city);
			infectionDeck.add(playerCard);
		}
		Collections.shuffle(infectionDeck);
		return infectionDeck;
	}

	/**
	 * Draw NBINITIALCARDS cards and add to player's hand
	 * 
	 * @throws LoseException
	 */
	protected void drawInitialPlayerCards() throws LoseException {
		for (int i = 0; i < NBINITIALCARDS; i++) {
			handleDrawPlayerCard();
		}
	}

	/**
	 * Return true if all neighbourgs have 3 infection blocks (outbroken)
	 */
	public boolean outbreakEnd(List<String> neighbours) {
		for (String neighbour : neighbours) {
			if (!citiesOutbroken.contains(neighbour))
				return false;
		}
		return true;
	}

	private void increaseInfectionRateIndex() throws LoseException {
		if (board.getInfectionRateIndex() < GameEngine.INFECTIONRATETAB.length - 1)
			board.incrementInfectionRateIndex();
	}

	// DRAW METHODS

	/**
	 * Draw a card, and add to the player's hand if it is not an epidemy card. If
	 * so, apply the corresponding actions. If no more card are remaining in the
	 * player card deck, handle the lose status.
	 * 
	 * @throws LoseException
	 */
	private int handleDrawPlayerCard() throws LoseException {
		PlayerCardInterface card = board.drawPlayerCard();
		if (card == null) {
			checkNoMorePlayerCards();
		}
		if (card.getCityName().equals(Card.EPIDEMYCARD)) {
			Cli.printerrln("=================  EPIDEMY  =========================");
			applyEpidemyActions(card);

			return 1;
		} else {
			addPlayerHand(card);
			Cli.printoutln("Card taken : " + card);
		}
		return 0;
	}

	/**
	 * Discard the epidemy card, draw an infection card and put 3 infection blocks
	 * on the infection card city. Then discards the card, shuffles the discard
	 * infection deck and add it at the top of the infection deck. Remove also all
	 * the cards in the discard infection deck.
	 * 
	 * @throws LoseException
	 * 
	 */
	private void applyEpidemyActions(PlayerCardInterface card) throws LoseException {
		increaseInfectionRateIndex();
		discardPlayerCard(card);

		PlayerCardInterface toInfect = drawInfectionCard();
		handleInfection(toInfect.getDisease(), NBBLOCKEPIDEMY, toInfect.getCityName());
		discardInfectionCard(toInfect);

		// take the discard infection deck, shuffle it, and add to the top of the
		// infection deck

		Collections.shuffle(discardInfectionDeck);
		infectionDeck.addAll(0, discardInfectionDeck);

		discardInfectionDeck.clear();
	}

	/**
	 * Draw infections cards (the number of cards depends on the infection rate).
	 * Apply the corresponding actions with the drawn cards(see rules)
	 * 
	 * @throws LoseException
	 */
	protected void handleDrawInfectionCard() throws LoseException {
		for (int i = 0; i < INFECTIONRATETAB[board.getInfectionRateIndex()]; i++) {
			PlayerCardInterface toInfect = drawInfectionCard();
			if (!isEradicated(toInfect.getDisease())) {
				handleInfection(toInfect.getDisease(), 1, toInfect.getCityName());
			}
			Cli.printout("\n\n");
			discardInfectionCard(toInfect);
		}
	}

	/**
	 * Add a card to the player's hand
	 */
	private void addPlayerHand(PlayerCardInterface card) {
		board.addHand(card);
	}

	/**
	 * Discard the infection card to the discard infection deck
	 */
	private void discardInfectionCard(PlayerCardInterface toInfect) {
		discardInfectionDeck.add(toInfect);
	}

	/**
	 * Draw the first infection card from the infection deck
	 */
	private PlayerCardInterface drawInfectionCard() {
		return infectionDeck.poll();
	}

	/**
	 * Discard the player card to the discard player deck
	 */
	private void discardPlayerCard(PlayerCardInterface card) {
		board.getDiscardPlayerDeck().add(card);

	}

	// LOSE REASONS

	private void checkNoMoreBlocks(Disease disease, int nbBlocks) throws LoseException {
		if (board.getDiseaseBlocks().get(disease) <= nbBlocks)
			isLose(DefeatReason.NO_MORE_BLOCKS);
	}

	private void checkTooManyOutbreaks() throws LoseException {
		if (board.getNbOutBreaks() == 8)
			isLose(DefeatReason.TOO_MANY_OUTBREAKS);
	}

	private void checkNoMorePlayerCards() throws LoseException {
		if (board.getPlayerDeck().peek() == null)
			isLose(DefeatReason.NO_MORE_PLAYER_CARDS);
	}

	/**
	 * Set the correct defeat reason and stop the game
	 * 
	 * @param type : the type of lose
	 * 
	 */
	public void isLose(DefeatReason type) throws LoseException {
		switch (type) {
		case NO_MORE_BLOCKS:
			setDefeated("U lost, no more blocks !", type);
		case NO_MORE_PLAYER_CARDS:
			setDefeated("U lost, no more card to draw !", type);
		case TOO_MANY_OUTBREAKS:
			setDefeated("U lost, maximum outbreaks reached", type);
		default:
			break;
		}

	}

	/**
	 * Test if the game is won (one case)
	 * 
	 * @throws WinException
	 * 
	 * 
	 */
	public void testVictory() throws WinException {
		if (board.getCuredDiseases().size() == 4)
			setVictorious();
	}

	@Override
	public List<String> allCityNames() {
		if (board.getCityNames() != null) {
			return board.getCityNames();
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> neighbours(String cityName) {
		List<String> neighbours = board.getMapNeighbourgs().get(cityName);
		if (neighbours != null)
			return neighbours;
		throw new UnsupportedOperationException();
	}

	@Override
	public int infectionLevel(String cityName, Disease d) {
		List<Disease> infections = board.getMapInfections().get(cityName);
		if (infections == null)
			throw new UnsupportedOperationException();

		int count = 0;

		for (Disease disease : infections) {
			if (disease.equals(d))
				count++;
		}
		return count;
	}

	@Override
	public boolean isCured(Disease d) {
		if (board.getCuredDiseases() == null)
			throw new UnsupportedOperationException();

		return board.getCuredDiseases().contains(d);

	}

	@Override
	public int infectionRate() {
		return INFECTIONRATETAB[board.getInfectionRateIndex()];
	}

	@Override
	public GameStatus gameStatus() {
		return gameStatus;
	}

	@Override
	public int turnDuration() {
		return TURNDURATION;
	}

	@Override
	public boolean isEradicated(Disease d) {
		if (board.getMapInfections() == null)
			throw new UnsupportedOperationException();

		if (isCured(d)) {
			for (List<Disease> diseases : board.getMapInfections().values()) {
				for (Disease disease : diseases) {
					if (disease.equals(d)) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int getNbOutbreaks() {
		return board.getNbOutBreaks();
	}

	@Override
	public int getNbPlayerCardsLeft() {
		return board.getNbPlayerCardsLeft();
	}

//	// Do not change!
	private void setDefeated(String msg, DefeatReason dr) throws LoseException {
		gameStatus = GameStatus.DEFEATED;
		if (hasGUI) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Cli.printerrln("Player(s) have been defeated: " + msg);
		Cli.printerrln("Result: " + gameStatus);
		Cli.printerrln("Reason: " + dr);
		printGameStats();
		board.setDefeated(dr.toString());
		throw new LoseException(dr.toString());
	}

//	// Do not change!
	private void setVictorious() throws WinException {
		gameStatus = GameStatus.VICTORIOUS;
		if (hasGUI) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Cli.printerrln("Player(s) have won.");
		Cli.printerrln("Result: " + gameStatus);
		printGameStats();
		board.setVictorious();
		throw new WinException("");
	}

	// Do not change!
//	private void setDefeated(String msg, DefeatReason dr) throws LoseException {
//		gameStatus = GameStatus.DEFEATED;
//		Cli.printerrln("Player(s) have been defeated: " + msg);
//		Cli.printerrln("Result: " + gameStatus);
//		Cli.printerrln("Reason: " + dr);
//		printGameStats();
//		throw new LoseException(dr.toString());
//	}
//
//	// Do not change!
//	private void setVictorious() throws WinException {
//		gameStatus = GameStatus.VICTORIOUS;
//		Cli.printerrln("Player(s) have won.");
//		Cli.printerrln("Result: " + gameStatus);
//		printGameStats();
//		throw new WinException("");
//	}

	// Do not change!
	private void printGameStats() {
		Map<Disease, Integer> blocks = new HashMap<>();
		for (String city : allCityNames()) {
			for (Disease d : Disease.values()) {
				blocks.put(d, blocks.getOrDefault(d, 0) + infectionLevel(city, d));

			}
		}
		Cli.printerrln(blocks.toString());
		Cli.printerrln("Infection-rate:" + infectionRate());
		for (Disease d : Disease.values()) {
			Cli.printerrln("Cured-" + d + ":" + isCured(d));
		}
		Cli.printerrln("Nb-outbreaks:" + getNbOutbreaks());
		Cli.printerrln("Nb-player-cards-left:" + getNbPlayerCardsLeft());
	}

	@Override
	public List<String> getDiscardedInfectionCards() {
		List<String> result = new ArrayList<>();
		for (PlayerCardInterface card : discardInfectionDeck) {
			result.add(card.getCityName());
		}
		return result;
	}

}
