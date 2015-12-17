package cps.mobilemaestro.library;

public class MMLocationResult {
	private String id;
	private double locX, locY;

	public MMLocationResult(String id, double locX, double locY)
	{
		this.id = id;
		this.locX = locX;
		this.locY = locY;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getLocX() {
		return locX;
	}

	public void setLocX(double locX) {
		this.locX = locX;
	}

	public double getLocY() {
		return locY;
	}

	public void setLocY(double locY) {
		this.locY = locY;
	}
}

