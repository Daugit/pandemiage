package fr.dauphine.ja.student.pandemiage.ai;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import fr.dauphine.ja.pandemiage.Model.City;
import fr.dauphine.ja.pandemiage.Model.Edge;
import fr.dauphine.ja.pandemiage.common.AiInterface;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;

public class CheatAi implements AiInterface {
	private static InputStream cityGraphFile = CheatAi.class.getResourceAsStream("pandemic.graphml");
	private static List<City> cities = new ArrayList<>();
	private static List<Edge> edges = new ArrayList<>();

	private static Map<String, List<String>> mapNeighbours;

	public CheatAi() {
		try {
			getMapFromGraph(cityGraphFile);
			mapNeighbours = getMapNeighbours();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
		}
	}

	@Override
	public void playTurn(GameInterface g, PlayerInterface p) {
		for (int i = 0; i < 4; ++i) {

			try {
				List<PlayerCardInterface> cureCards = curableDisease(p.playerHand());
				p.discoverCure(cureCards);
			} catch (UnauthorizedActionException e) {
			}

			Disease toTreat = infectedCity(g, p);
			if (toTreat != null) {
				try {
					p.treatDisease(toTreat);
				} catch (UnauthorizedActionException e) {

				}
			}
			String cityToMove = bestChoiceCityToMove(g, p);
			try {
				p.moveTo(cityToMove);
			} catch (UnauthorizedActionException e) {
			}
		}
	}

	private Disease infectedCity(GameInterface g, PlayerInterface p) {
		for (Disease d : Disease.values()) {
			if (g.infectionLevel(p.playerLocation(), d) >= 2) {
				return d;
			}
		}
		return null;
	}

	private String bestChoiceCityToMove(GameInterface g, PlayerInterface p) {
		String playerLocation = p.playerLocation();
		String nearestCityWithOutbreak = nearestCityWithOutbreak(g, playerLocation);

		if (nearestCityWithOutbreak == null) {
			return null;
		}
		String cityChoice = shortestPath(playerLocation, nearestCityWithOutbreak);
		return cityChoice;
	}

	private String shortestPath(String from, String to) {
		List<String> neighbours = mapNeighbours.get(from);
		double distanceMin = 100000;
		String bestCity = neighbours.get(0);

		for (String city : neighbours) {
			City fromObj = getCity(city);
			City toObj = getCity(to);

			double distance = Math
					.sqrt(Math.pow((fromObj.getX() - toObj.getX()), 2) + Math.pow((fromObj.getY() - toObj.getY()), 2));
			if (distance < distanceMin) {
				distanceMin = distance;
				bestCity = city;
			}
		}
		return bestCity;
	}

	private String nearestCityWithOutbreak(GameInterface g, String from) {
		List<String> cities = citiesWithOutbreaks(g);

		double distanceMin = 100000;
		String bestCity = null;
		if (cities.size() > 0) {
			bestCity = cities.get(0);
		}
		for (String city : cities) {
			City fromObj = getCity(from);
			City toObj = getCity(city);

			double distance = Math
					.sqrt(Math.pow((fromObj.getX() - toObj.getX()), 2) + Math.pow((fromObj.getY() - toObj.getY()), 2));
			if (distance < distanceMin) {
				distanceMin = distance;
				bestCity = city;
			}
		}
		return bestCity;
	}

	private List<String> citiesWithOutbreaks(GameInterface g) {
		List<String> cities = new ArrayList<>();

		for (String city : g.allCityNames()) {
			for (Disease d : Disease.values()) {
				if (g.infectionLevel(city, d) >= 2) {
					cities.add(city);
				}
			}
		}
		return cities;
	}

	private City getCity(String city) {
		for (City city2 : cities) {
			if (city2.getLabel().equals(city)) {
				return city2;
			}
		}
		return null;
	}

	/**
	 * @return a map between a city and its neighbours
	 */
	private static Map<String, List<String>> getMapNeighbours() {
		Map<String, List<String>> map = new HashMap<>();
		for (City city : cities) {
			List<String> neighbours = new ArrayList<>();
			for (Edge edge : edges) {
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
			map.put(city.getLabel(), neighbours);
		}
		return map;
	}

	private static String getCityNameById(int targetCityId) {
		for (City i : cities) {
			if (i.getId() == targetCityId)
				return i.getLabel();
		}
		return null;
	}

	@Override
	public List<PlayerCardInterface> discard(GameInterface g, PlayerInterface p, int maxHandSize, int nbEpidemic) {
		List<PlayerCardInterface> discard = new ArrayList<>();
		List<PlayerCardInterface> hand = p.playerHand();
		int numdiscard = p.playerHand().size() - maxHandSize;

		Disease mostDisease = mostDisease(hand);
		for (int i = 0; i < numdiscard; i++) {
			PlayerCardInterface toDiscard = containsOtherThan(mostDisease, hand);
			if (toDiscard != null) {
				discard.add(toDiscard);
				hand.remove(toDiscard);
			} else {
				discard.add(hand.get(0));
				hand.remove(0);
			}
		}
		return discard;
	}

	private List<PlayerCardInterface> curableDisease(List<PlayerCardInterface> playerHand) {
		for (Disease d : Disease.values()) {
			List<PlayerCardInterface> cards = new ArrayList<>();
			for (PlayerCardInterface playerCard : playerHand) {
				if (d.equals(playerCard.getDisease())) {
					cards.add(playerCard);
				}
			}
			if (cards.size() >= GameEngine.NBCARDCURE) {
				while (cards.size() > GameEngine.NBCARDCURE) {
					cards.remove(cards.size() - 1);
				}
				return cards;
			}
		}
		return new ArrayList<>();
	}

	private PlayerCardInterface containsOtherThan(Disease d, List<PlayerCardInterface> hand) {
		for (PlayerCardInterface playerCardInterface : hand) {
			if (!playerCardInterface.getDisease().equals(d))
				return playerCardInterface;
		}
		return null;
	}

	private Disease mostDisease(List<PlayerCardInterface> hand) {
		Disease most = Disease.BLACK;
		int maxCount = 0;

		for (Disease d : Disease.values()) {
			int count = 0;
			for (PlayerCardInterface playerCard : hand) {
				if (d.equals(playerCard.getDisease())) {
					count++;
				}
			}
			if (count > maxCount) {
				maxCount = count;
				most = d;
			}
		}

		return most;
	}

	private static void getMapFromGraph(InputStream fileName) throws FileNotFoundException, XMLStreamException {
		City city = null;
		Edge edge = null;
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(fileName);
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

		} catch (XMLStreamException e) {

		}
	}

//	public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
//
//		CheatAi ai = new CheatAi();
//		getMapFromGraph(cityGraphFile);
//		mapNeighbours = getMapNeighbours();
//		List<String> str = new ArrayList<>();
//		str.add("Chicago");
//		System.out.println(ai.shortestPath("Moscow", "Chicago"));
//
//	}
}
