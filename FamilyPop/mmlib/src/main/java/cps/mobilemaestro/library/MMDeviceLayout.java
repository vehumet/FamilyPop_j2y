package cps.mobilemaestro.library;

public class MMDeviceLayout {
	private double centerX, centerY; // Center?? ???
	private double zeroX, zeroY; // ?????? ???
	private double width, height; // ????? ???

	public MMDeviceLayout(double centerX, double centerY, double width, double height)
	{
		this.centerX = centerX;
		this.centerY = centerY;
		this.width = width;
		this.height = height;

		this.zeroX = this.centerX - this.width / 2;
		this.zeroY = this.centerY - this.height / 2;
	}

	public double getCenterX() {
		return centerX;
	}

	public void setCenterX(double centerX) {
		this.centerX = centerX;
	}

	public double getCenterY() {
		return centerY;
	}

	public void setCenterY(double centerY) {
		this.centerY = centerY;
	}

	public double getZeroX() {
		return zeroX;
	}

	public void setZeroX(double zeroX) {
		this.zeroX = zeroX;
	}

	public double getZeroY() {
		return zeroY;
	}

	public void setZeroY(double zeroY) {
		this.zeroY = zeroY;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}
}

