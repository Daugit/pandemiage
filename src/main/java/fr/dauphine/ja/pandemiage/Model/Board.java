package fr.dauphine.ja.pandemiage.Model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.google.common.base.Preconditions;

import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;
import fr.dauphine.ja.student.pandemiage.ui.view.Observer;
import fr.dauphine.ja.student.pandemiage.ui.view.PandemiageGUI;

/**
 * <b>Board includes all datas of the game and their informations</b>
 * <p>
 * It's also the model class that will inform the view when a state change
 * occurs. This observable class notifies with a message and a BoardChange to
 * better target the view.
 * </p>
 *
 * @see PandemiageGUI
 * @see BoardChange
 * @author avastTeam
 */
public class Board implements Observable {
	// STATIC GAME OBJECTS

	private final List<String> cityNames;

	private List<City> cities;
	private List<Edge> edges;

	/**
	 * Gives the neighbours for each city
	 */
	private final Map<String, List<String>> mapNeighbours;

	// DYNAMIC GAME OBJECTS

	/**
	 * Give the number of blocks left for a disease
	 */
	private Map<Disease, Integer> diseaseBlocks;

	/**
	 * Gives the list of infections (diseases) for each city
	 */
	private Map<String, List<Disease>> mapInfections;

	/**
	 * All the cured diseases
	 */
	private List<Disease> curedDiseases;

	/**
	 * Action left for the player for the current turn
	 */
	private int actionLeft = GameEngine.ACTIONPERTURN;

	/**
	 * Number of epidemy cards
	 */
	private int nbEpidemyCards;

	/**
	 * Player deck
	 */
	private LinkedList<PlayerCardInterface> playerDeck;

	/**
	 * The discard deck is modified by the player and the gameEngine
	 */
	private LinkedList<PlayerCardInterface> discardPlayerDeck;

	/**
	 * Gives the index of the infection Rate in infectionRateTab
	 */
	private int infectionRateIndex;

	/**
	 * Actual number of outbreaks
	 */
	private int nbOutBreaks;

	/**
	 * Player's location on map and hand.
	 */
	private String location;
	private List<PlayerCardInterface> hand;

	/**
	 * Observers of this class
	 */
	private ArrayList<Observer> listObserver = new ArrayList<Observer>();

	/**
	 * Turn indicator
	 */
	private short turn;

	private PlayerInterface player;

	/**
	 * Board constructor.
	 * <p>
	 * Extracts all cities and edges from a graphml file and initializes all datas.
	 * </P>
	 * 
	 * @param cityGraphFilename
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	public Board(String cityGraphFilename) throws FileNotFoundException, XMLStreamException {
		cities = new ArrayList<>();
		edges = new ArrayList<>();
		getMapFromGraph(cityGraphFilename);
		cityNames = allCitiesName();

		mapNeighbours = getMapNeighbours();
		diseaseBlocks = getInitialMapDiseaseBlock();
		mapInfections = getInitialMapInfections();
		curedDiseases = new ArrayList<>();

		discardPlayerDeck = new LinkedList<>();
		infectionRateIndex = 0;
		nbOutBreaks = 0;

		location = GameEngine.STARTCITY;
		hand = new LinkedList<>();
		playerDeck = getInitialPlayerDeck();
		this.turn = 0;
	}

	/**
	 * For tests only : don't parse the XML again
	 */
	public Board(Board b) throws FileNotFoundException, XMLStreamException {
		cities = b.getCities();
		edges = b.getEdges();
		cityNames = b.getCityNames();

		mapNeighbours = b.getMapNeighbourgs();
		diseaseBlocks = getInitialMapDiseaseBlock();
		mapInfections = getInitialMapInfections();
		curedDiseases = new ArrayList<>();
		discardPlayerDeck = new LinkedList<>();
		infectionRateIndex = 0;
		nbOutBreaks = 0;

		location = GameEngine.STARTCITY;
		hand = new LinkedList<>();
		playerDeck = getInitialPlayerDeck();
		this.turn = 0;

	}

	/**
	 * @return map : (City name -> empty Disease list) for each city
	 */
	private Map<String, List<Disease>> getInitialMapInfections() {
		Map<String, List<Disease>> initialInfectionMap = new HashMap<>();
		for (String city : allCitiesName()) {
			initialInfectionMap.put(city, new ArrayList<>());
		}
		return initialInfectionMap;
	}

	/**
	 * @return map : (Disease -> initial blocks number) for each disease
	 */
	private Map<Disease, Integer> getInitialMapDiseaseBlock() {
		Map<Disease, Integer> initialDiseaseBlocksMap = new HashMap<>();

		for (Disease disease : Disease.values()) {
			initialDiseaseBlocksMap.put(disease, GameEngine.NBINFECTIONBLOCK);
		}

		return initialDiseaseBlocksMap;

	}

	/**
	 * Following edges : a city is neighbour of another one if it's the edge source
	 * (respectively the target) and the other the target (respectively the source)
	 * 
	 * @return map : (City name -> neighboring cities name) for each city
	 */
	private Map<String, List<String>> getMapNeighbours() {
		Map<String, List<String>> neighboursMap = new HashMap<>();
		for (City city : this.cities) {
			List<String> neighbours = new ArrayList<>();
			for (Edge edge : this.edges) {
				int sourceCityId = edge.getSource();
				int targetCityId = edge.getTarget();
				String sourceCityName = null;
				String targetCityName = null;
				if (sourceCityId == city.getId()) {
					targetCityName = getCityNameById(targetCityId);

					if (targetCityName == null) {
						throw new IllegalStateException(
								"The data in the XML File is not valid, some data are incoherent");
					}
					if (!neighbours.contains(targetCityName)) {
						neighbours.add(targetCityName);
					}
				} else if (targetCityId == city.getId()) {
					sourceCityName = getCityNameById(sourceCityId);

					if (sourceCityName == null) {
						throw new IllegalStateException(
								"The data in the XML File is not valid, some data are incoherent");
					}
					if (!neighbours.contains(sourceCityName)) {
						neighbours.add(sourceCityName);
					}
				}
			}
			neighboursMap.put(city.getLabel(), neighbours);
		}
		return neighboursMap;
	}

	private String getCityNameById(int cityId) {
		for (City city : this.cities) {
			if (city.getId() == cityId)
				return city.getLabel();
		}
		return null;
	}

	private List<String> allCitiesName() {
		List<String> citiesName = new ArrayList<>();
		for (City city : this.cities) {
			citiesName.add(city.getLabel());
		}
		return citiesName;
	}

	/**
	 * @return the initial deck of player cities cards
	 */
	private LinkedList<PlayerCardInterface> getInitialPlayerDeck() {
		LinkedList<PlayerCardInterface> playerDeck = new LinkedList<>();
		for (City city : cities) {
			PlayerCardInterface playerCard = new Card(city);
			playerDeck.add(playerCard);
		}
		Collections.shuffle(playerDeck);

		return playerDeck;
	}

	/**
	 * Dispatch epidemy's cards following rules (4 stacks with approximatively the
	 * same size). At the end, player deck now contains epidemy's cards.
	 */
	public void addInitalEpidemyCards() {
		// TODO Multi-difficulty : to change
		LinkedList<PlayerCardInterface> playerDeckWithEpidemyCards = new LinkedList<>();

		int sizePerPack = playerDeck.size() / nbEpidemyCards;

		for (int i = 0; i < nbEpidemyCards; i++) {
			LinkedList<PlayerCardInterface> tmp = new LinkedList<>();
			tmp.add(new Card(Card.EPIDEMYCARD, null));
			if (i == nbEpidemyCards - 1) {
				tmp.addAll(playerDeck);
				playerDeck.clear();
			} else {
				for (int j = 0; j < sizePerPack; j++) {
					tmp.add(playerDeck.poll());
				}
			}
			Collections.shuffle(tmp);
			playerDeckWithEpidemyCards.addAll(tmp);
		}
		this.playerDeck = playerDeckWithEpidemyCards;
	}

	// Data getters

	public int getNbPlayerCardsLeft() {
		return playerDeck.size();
	}

	public List<City> getCities() {
		return cities;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public List<String> getCityNames() {
		return cityNames;
	}

	public String[] getCityNamesSortedTab() {
		cityNames.sort(new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		String[] cities = (String[]) cityNames.toArray();
		return cities;
	}

	public Map<String, List<String>> getMapNeighbourgs() {
		return mapNeighbours;
	}

	public Map<Disease, Integer> getDiseaseBlocks() {
		return diseaseBlocks;
	}

	public int[] getDiseaseBlocksLeft() {
		int[] diseaseBlocksLeft = new int[4];
		diseaseBlocksLeft[0] = diseaseBlocks.get(Disease.BLUE);
		diseaseBlocksLeft[1] = diseaseBlocks.get(Disease.YELLOW);
		diseaseBlocksLeft[2] = diseaseBlocks.get(Disease.BLACK);
		diseaseBlocksLeft[3] = diseaseBlocks.get(Disease.RED);
		return diseaseBlocksLeft;
	}

	public Map<String, List<Disease>> getMapInfections() {
		return mapInfections;
	}

	public List<Disease> getCuredDiseases() {
		return curedDiseases;
	}

	public String getCured() {
		String res = "";
		for (Disease disease : curedDiseases) {
			res = res + disease.name() + " ";
		}
		return res;

	}

	/**
	 * @return number of player's actions left
	 */
	public int getActionLeft() {
		return actionLeft;
	}

	public LinkedList<PlayerCardInterface> getDiscardPlayerDeck() {
		return discardPlayerDeck;
	}

	public String getLocation() {
		return location;
	}

	public List<PlayerCardInterface> getHand() {
		return hand;
	}

	public int getInfectionRateIndex() {
		return infectionRateIndex;
	}

	public int getNbOutBreaks() {
		return nbOutBreaks;
	}

	/**
	 * @param cityName
	 * @return each disease blocks left on a city in this order : BLUE YELLOW BLACK
	 *         RED
	 */
	public int[] getBlocksOnCity(String cityName) {
		int[] diseasesblocksleft = new int[4];
		for (Disease block : mapInfections.get(cityName)) {
			switch (block) {
			case BLUE:
				diseasesblocksleft[0]++;
				break;
			case YELLOW:
				diseasesblocksleft[1]++;
				break;
			case BLACK:
				diseasesblocksleft[2]++;
				break;
			case RED:
				diseasesblocksleft[3]++;
				break;
			}
		}
		return diseasesblocksleft;

	}

	public LinkedList<PlayerCardInterface> getPlayerDeck() {
		return playerDeck;
	}

	public int getNbEpidemyCards() {
		return nbEpidemyCards;
	}

	public short getTurn() {
		return turn;
	}

	public PlayerInterface getPlayer() {
		return player;
	}

	public List<String> getNeighboursOfLocation() {
		List<String> neighbours = new ArrayList<String>();
		neighbours = mapNeighbours.get(location);
		return neighbours;
	}

	// Data setters

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}

	public void setActionLeft(int i) {
		Preconditions.checkArgument(i <= GameEngine.ACTIONPERTURN);
		actionLeft = i;
	}

	public void decrementActionLeft() {
		actionLeft--;
	}

	public void resetActionLeft() {
		actionLeft = GameEngine.ACTIONPERTURN;
	}

	public void setPlayerDeck(LinkedList<PlayerCardInterface> playerDeck) {
		this.playerDeck = playerDeck;
	}

	public void setNbEpidemyCards(int nbEpidemyCards) {
		this.nbEpidemyCards = nbEpidemyCards;
	}

	public void setPlayer(PlayerInterface player) {
		this.player = player;
	}

	// Observer pattern implementation using BoardChange

	@Override
	public void addObserver(Observer obs) {
		this.listObserver.add(obs);

	}

	@Override
	public void removeObserver() {
		listObserver = new ArrayList<Observer>();

	}

	@Override
	public void notifyObserver(String str, List<BoardChange> boardChanges) {
		for (Observer obs : listObserver)
			obs.update(str, boardChanges);

	}

	/**
	 * Draw the first player card from the player deck
	 */
	public PlayerCardInterface drawPlayerCard() {
		PlayerCardInterface pCard = playerDeck.poll();

		String str = "";
		List<BoardChange> b = new ArrayList<BoardChange>();
		if (pCard == null)
			return pCard;
		b.add(BoardChange.DRAW_CARD);
		notifyObserver(str, b);

		return pCard;
	}

	/**
	 * Change player's location to cityName
	 */
	public void setLocation(String cityName) {
		this.location = cityName;

		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.MOVE_TO);
		notifyObserver(location, b);
	}

	public void incrementInfectionRateIndex() {
		this.infectionRateIndex++;
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.INCR_INFECTIONRATE);
		if (GameEngine.INFECTIONRATETAB[infectionRateIndex] > GameEngine.INFECTIONRATETAB[infectionRateIndex - 1])
			notifyObserver(String.valueOf(GameEngine.INFECTIONRATETAB[infectionRateIndex]), b);
		else {
			notifyObserver("", b);
		}
	}

	public void incrementNbOutBreaks(String city) {
		this.nbOutBreaks++;

		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.OUTBREAK);
		notifyObserver(city, b);
	}

	/**
	 * Add a card to player's hand
	 */
	public void addHand(PlayerCardInterface card) {
		hand.add(card);

		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.ADD_CARD);
		if (hand.size() > 9) {
			b.add(BoardChange.CARD_TO_DISCARD);
		}
		notifyObserver(card.getCityName(), b);

	}

	/**
	 * It helps the user to follow the game changes better
	 */
	public void notifyWait() {
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.WAIT_USER);
		notifyObserver("", b);

	}

	public void setCityDisease(String location, List<Disease> arrayList) {
		mapInfections.put(location, arrayList);
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.TREAT_DISEASE);
		notifyObserver(location, b);
	}

	public void setDiseaseBlocks(Disease d, int value) {
		diseaseBlocks.put(d, value);
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.INCR_BLOCK_DISEASE);
		notifyObserver("", b);
	}

	public void addCuredDiseases(Disease toTreat) {
		curedDiseases.add(toTreat);
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.CURE_DISEASE);
		notifyObserver(toTreat.name(), b);
	}

	public void removeHand(List<PlayerCardInterface> playerCards) {
		if (!Objects.isNull(playerCards))
			hand.removeAll(playerCards);
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.UPDATE_MAP);
		notifyObserver("", b);
	}

	public void removeHandByName(String cityName) {
		Iterator<PlayerCardInterface> it = hand.iterator();
		PlayerCardInterface card = null;
		while (it.hasNext()) {
			card = it.next();
			if (card.getCityName().equals(cityName)) {
				it.remove();
				break;
			}

		}

		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.HUMAN_DISCARD);
		discardPlayerDeck.add(card);
		if (hand.size() <= 9)
			b.add(BoardChange.NO_DISCARD);
		notifyObserver(card.getCityName(), b);
	}

	public void noNecessaryDiscard() {
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.NO_DISCARD);
		notifyObserver("", b);
	}

	public void notifyDiscard(List<PlayerCardInterface> discarded) {
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.DISCARD);
		String msg = "";
		for (PlayerCardInterface playerCardInterface : discarded) {
			msg += playerCardInterface.getCityName() + " | ";
		}
		notifyObserver(msg, b);

	}

	public void notifyUsedCard(PlayerCardInterface cardUsed) {
		hand.remove(cardUsed);
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.USE_CARD);
		notifyObserver(cardUsed.getCityName(), b);

	}

	public void incrementTurn() {
		this.turn++;

		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.NEW_TURN);
		notifyObserver(String.valueOf(turn), b);
	}

	public void setDefeated(String defeatReason) {
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.DEFEAT);
		notifyObserver(defeatReason, b);
	}

	public void setVictorious() {
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.VICTORY);
		notifyObserver("", b);
	}

	@Override
	public String toString() {
		return "Board [cities=" + cities + ", edges=" + edges + "]";
	}

	/**
	 * Extracts all cities informations (id, label, position, color) and edges
	 * informations (source, target) by browsing the graphml tags.
	 * 
	 * @param graphmlFileName
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	private void getMapFromGraph(String graphmlFileName) throws FileNotFoundException, XMLStreamException {
		City city = null;
		Edge edge = null;
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(graphmlFileName));
			while (xmlEventReader.hasNext()) {
				XMLEvent xmlEvent = xmlEventReader.nextEvent();
				if (xmlEvent.isStartElement()) {
					StartElement startElement = xmlEvent.asStartElement();
					if (startElement.getName().getLocalPart().equals("node")) {
						city = new City();
						// Get the 'id' attribute from City element
						Attribute idAttr = startElement.getAttributeByName(new QName("id"));
						if (idAttr != null) {
							// Get Id value
							city.setId(Integer.parseInt(idAttr.getValue()));
						}
					}
					// set the other variables from xml elements
					else if (startElement.getName().getLocalPart().equals("data")) {

						Attribute idAttr = startElement.getAttributeByName(new QName("key"));

						xmlEvent = xmlEventReader.nextEvent();

						switch (idAttr.getValue()) {
						case "label":
							if (idAttr != null) {
								city.setLabel(
										// get the data between tags
										xmlEvent.asCharacters().getData());
							}
							break;
						case "eigencentrality":
							if (idAttr != null) {
								city.setEigencentrality(Double.parseDouble(xmlEvent.asCharacters().getData()));
							}
							break;
						case "degree":
							if (idAttr != null) {
								city.setDegree(Integer.parseInt(xmlEvent.asCharacters().getData()));
							}
							break;
						case "size":
							if (idAttr != null) {
								city.setSize(Double.parseDouble(xmlEvent.asCharacters().getData()));
							}
							break;
						case "r":
							if (idAttr != null) {
								city.setR(Short.parseShort(xmlEvent.asCharacters().getData()));
							}
							break;
						case "g":
							if (idAttr != null) {
								city.setG(Short.parseShort(xmlEvent.asCharacters().getData()));
							}
							break;
						case "b":
							if (idAttr != null) {
								city.setB(Short.parseShort(xmlEvent.asCharacters().getData()));
							}
							break;
						case "x":
							if (idAttr != null) {
								city.setX(Double.parseDouble(xmlEvent.asCharacters().getData()));
							}
							break;
						case "y":
							if (idAttr != null) {
								city.setY(Double.parseDouble(xmlEvent.asCharacters().getData()));
							}
							break;
						case "weight":
							if (idAttr != null) {
								edge.setWeight(Double.parseDouble(xmlEvent.asCharacters().getData()));
							}
							break;
						default:
							break;
						}

					} else if (startElement.getName().getLocalPart().equals("edge")) {
						edge = new Edge();
						// Get the 'id' attribute from City element
						Attribute sourceAttr = startElement.getAttributeByName(new QName("source"));
						Attribute targetAttr = startElement.getAttributeByName(new QName("target"));

						if (sourceAttr != null) {
							edge.setSource(Integer.parseInt(sourceAttr.getValue()));
						}
						if (targetAttr != null) {
							edge.setTarget(Integer.parseInt(targetAttr.getValue()));
						}

					}
				}
				// if City end element is reached, add employee object to list
				if (xmlEvent.isEndElement()) {
					EndElement endElement = xmlEvent.asEndElement();
					if (endElement.getName().getLocalPart().equals("node")) {
						cities.add(city);
					} else if (endElement.getName().getLocalPart().equals("edge")) {
						edges.add(edge);
					}
				}
			}

		} catch (FileNotFoundException | XMLStreamException e) {
			throw e;
		}
	}

	public void updateFlyToList() {
		List<BoardChange> b = new ArrayList<BoardChange>();
		b.add(BoardChange.UPDATEFLY);
		notifyObserver("", b);

	}

}
