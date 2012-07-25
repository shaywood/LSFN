package com.wikispaces.Shared.Messaging;

import com.wikispaces.lsfn.Shared.UnitDirection;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details.Value_type;

public class Accelerate extends Message {
	UnitDirection where_to;

	public Accelerate(UnitDirection where_to) {
		super(2, "Acceleration input.", Value_type.INT32);
		this.where_to = where_to;
	}
	
	public boolean can_combine(Message c) {
		return c instanceof Accelerate;
	}

	public Message combine_with(Message c){
		if(can_combine(c)) {
			Accelerate a = (Accelerate)c;
			return new Accelerate(this.where_to.combine(a.where_to));
		}
		else throw new RuntimeException("Cannot combine commands of type " + this + " and " + c);
	}

	public UnitDirection get_direction() {
		return where_to;
	}
}