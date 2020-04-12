package fr.dauphine.ja.student.pandemiage.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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

public class Main {

	public static List<City> cities;
	public static List<Edge> edges;

	public Main() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		String fileName = "./pandemic.graphml";
		cities = new ArrayList<>();
		edges = new ArrayList<>();

		parseXML(fileName);
		for (City city : cities) {
			System.out.println(city.toString());
		}

		for (Edge edge : edges) {
			System.out.println(edge.toString());
		}

	}

	private static void parseXML(String fileName) {
		City city = null;
		Edge edge = null;
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(fileName));
			while (xmlEventReader.hasNext()) {
				XMLEvent xmlEvent = xmlEventReader.nextEvent();
				if (xmlEvent.isStartElement()) {
					StartElement startElement = xmlEvent.asStartElement();
					if (startElement.getName().getLocalPart().equals("node")) {
						city = new City();
						// Get the 'id' attribute from City element
						Attribute idAttr = startElement.getAttributeByName(new QName("id"));
						if (idAttr != null) {
							city.setId(Integer.parseInt(idAttr.getValue())); // get
																				// tag
																				// attribute
																				// value
						}
					}
					// set the other varibles from xml elements
					else if (startElement.getName().getLocalPart().equals("data")) {

						Attribute idAttr = startElement.getAttributeByName(new QName("key"));

						xmlEvent = xmlEventReader.nextEvent();

						switch (idAttr.getValue()) {
						case "label":
							if (idAttr != null) {
								city.setLabel(xmlEvent.asCharacters().getData()); // get
																					// data
																					// between
																					// tags
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
			e.printStackTrace();
		}
	}

}
