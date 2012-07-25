package com.wikispaces.lsfn.Shared.Subscription;

import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details.Value_type;

public class AccelerateEastWest extends Subscribeable {
	UnitDirection where_to;

	public AccelerateEastWest(UnitDirection where_to) {
		super(3, "East-West acceleration input.", Value_type.INT32);
		this.where_to = where_to;
	}
	
	public boolean can_combine(Subscribeable c) {
		return c instanceof AccelerateEastWest;
	}

	public Subscribeable combine_with(Subscribeable c){
		if(can_combine(c)) {
			AccelerateEastWest a = (AccelerateEastWest)c;
			return new AccelerateEastWest(this.where_to.combine(a.where_to));
		}
		else throw new RuntimeException("Cannot combine commands of type " + this + " and " + c);
	}

	public int get_value() {
		return where_to.get_east_west();
	}
}