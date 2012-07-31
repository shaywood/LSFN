package com.wikispaces.lsfn.Shared;

public enum UnitDirection {
	NOWHERE(0, 0),
	NORTH(1, 0),
	SOUTH(-1, 0),
	EAST(0, -1),
	WEST(0, 1),
	NORTHEAST(1, -1),
	NORTHWEST(1, 1),
	SOUTHEAST(-1, -1),
	SOUTHWEST(-1, 1);
	
	
	// These names are probably a lie. We're using this for controls, so we probably want these directions to be relative to the ship facing.
	private int north_south;
	private int east_west;

	UnitDirection(int north_south, int east_west) {
		this.north_south = north_south;
		this.east_west = east_west;
	}

	public int get_north_south() {
		return north_south;
	}
	
	public int get_east_west() {
		return east_west;
	}
	
	public UnitDirection combine(UnitDirection combine_with) {
		int new_north_south = clamp(this.north_south + combine_with.north_south);
		int new_east_west = clamp(this.east_west + combine_with.east_west);
		return lookup(new_north_south, new_east_west);
	}
	
	private int clamp(int value) {
		if(value > 1)
		{
			return 1;
		}
		if(value < -1)
		{
			return -1;
		}
		return value;
	}
	
	public static UnitDirection lookup(int north_south, int east_west) {
		for (UnitDirection u : UnitDirection.values()) {
			if(u.north_south == north_south && u.east_west == east_west) {
				return u;
			}
		}
		throw new RuntimeException("No direction defined for " + north_south + " and " + east_west);
	}
}
