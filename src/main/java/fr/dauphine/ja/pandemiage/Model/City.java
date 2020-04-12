package fr.dauphine.ja.pandemiage.Model;

/**
 * <b>A city is essentially characterized by an id, label, color (r, g, b), and
 * position (x, y)</b>
 * 
 * @author avastTeam
 */
public class City {

	private int id;
	private String label;
	private double eigencentrality;
	private int degree;
	private double size;
	private short r;
	private short g;
	private short b;
	private double x;
	private double y;

	public City() {
	}

	@Override
	public String toString() {
		return "City [id=" + id + ", label=" + label + ", eigencentrality=" + eigencentrality + ", degree=" + degree
				+ ", size=" + size + ", r=" + r + ", g=" + g + ", b=" + b + ", x=" + x + ", y=" + y + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getEigencentrality() {
		return eigencentrality;
	}

	public void setEigencentrality(double eigencentrality) {
		this.eigencentrality = eigencentrality;
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public short getR() {
		return r;
	}

	public void setR(short r) {
		this.r = r;
	}

	public short getG() {
		return g;
	}

	public void setG(short g) {
		this.g = g;
	}

	public short getB() {
		return b;
	}

	public void setB(short b) {
		this.b = b;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

}
