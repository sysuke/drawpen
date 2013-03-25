package com.funai.drawpen;

public class CoordinateData {
	public int x;
	public int y;
	public int state;
	final static public int UP = 1;
	final static public int DOWN = 2;
	final static public int MOVE = 3;
	final static public int S_MOVE = 4;

	public CoordinateData() {
		dataClear();
	}

	public CoordinateData(int x, int y, int state) {
		this.x = x;
		this.y = y;
		this.state = state;
	}

	public CoordinateData(CoordinateData data) {
		this.x = data.x;
		this.y = data.y;
		this.state = data.state;
	}

	public void dataClear() {
		x = 0;
		y = 0;
		state = 0;
	}
}
