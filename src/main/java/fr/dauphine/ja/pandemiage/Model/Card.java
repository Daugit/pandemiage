package fr.dauphine.ja.pandemiage.Model;

import java.util.Objects;

import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;

/**
 * <b>Card represents a player's deck card with a city name and a disease</b>
 * <p>
 * Epidemic cards have "EPIDEMY" as city and no disease
 * </p>
 * 
 * @author avastTeam
 */
public class Card implements PlayerCardInterface {

	private final String city;
	private final Disease disease;

	public final static String EPIDEMYCARD = "EPIDEMY";

	public Card() {
		city = null;
		disease = null;
	}

	public Card(String city, Disease disease) {
		if (EPIDEMYCARD.equals(city)) {
			this.city = EPIDEMYCARD;
			this.disease = null;
		} else {
			this.city = Objects.requireNonNull(city);
			this.disease = Objects.requireNonNull(disease);
		}
	}

	public Card(City city) {
		this.city = city.getLabel();

		if (city.getR() == 107 && city.getG() == 112 && city.getB() == 184) {
			disease = Disease.BLUE;
		} else if (city.getR() == 242 && city.getG() == 255 && city.getB() == 0) {
			disease = Disease.YELLOW;
		} else if (city.getR() == 153 && city.getG() == 153 && city.getB() == 153) {
			disease = Disease.BLACK;
		} else if (city.getR() == 153 && city.getG() == 18 && city.getB() == 21) {
			disease = Disease.RED;
		} else {
			throw new IllegalArgumentException("The specified city's color is not corresponding");
		}

	}

	@Override
	public String getCityName() {
		return city;
	}

	@Override
	public Disease getDisease() {
		return disease;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((disease == null) ? 0 : disease.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Card other = (Card) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (disease != other.disease)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Card [city=" + city + ", disease=" + disease + "]";
	}

}
