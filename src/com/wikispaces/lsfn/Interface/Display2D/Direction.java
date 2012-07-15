package com.wikispaces.lsfn.Interface.Display2D;

public enum Direction {
	BOW(0, 1),
	STERN(0, -1),
	PORT(-1, 0),
	STARBOARD(1, 0);
	
	private int sway;
	private int surge;

	Direction(int sway, int surge) {
		this.sway = sway;
		this.surge = surge;
	}

	public int get_sway() {
		return sway;
	}
	
	public int get_surge() {
		return surge;
	}
}
