package com.wikispaces.lsfn.Shared.Subscription;

import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details.Value_type;

public class AccelerateNorthSouth extends Subscribeable {
	UnitDirection where_to;

	public AccelerateNorthSouth(UnitDirection where_to) {
		super(2, "North-south acceleration input.", Value_type.INT32);
		this.where_to = where_to;
	}
	
	public boolean can_combine(Subscribeable c) {
		return c instanceof AccelerateNorthSouth;
	}

	public Subscribeable combine_with(Subscribeable c){
		if(can_combine(c)) {
			AccelerateNorthSouth a = (AccelerateNorthSouth)c;
			return new AccelerateNorthSouth(this.where_to.combine(a.where_to));
		}
		else throw new RuntimeException("Cannot combine commands of type " + this + " and " + c);
	}

	public int get_value() {
		return where_to.get_north_south();
	}
}
