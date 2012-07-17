package com.wikispaces.lsfn.Interface.Display2D;

import java.util.Arrays;
import java.util.List;

import com.wikispaces.lsfn.Interface.PlayerCommand;
import com.wikispaces.lsfn.Interface.Model.KnownSpace;
import com.wikispaces.lsfn.Shared.LSFN.Subscription_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.Subscribeable;

public class Accelerate implements PlayerCommand {

	UnitDirection where_to;

	public Accelerate(UnitDirection where_to) {
		this.where_to = where_to;
	}
	
	public boolean can_combine(PlayerCommand c) {
		return c instanceof Accelerate;
	}

	public PlayerCommand combine_with(PlayerCommand c){
		if(can_combine(c)) {
			Accelerate a = (Accelerate)c;
			return new Accelerate(this.where_to.combine(a.where_to));
		}
		else throw new RuntimeException("Cannot combine commands of type " + this + " and " + c);
	}

	public void update_local_model(KnownSpace model) {
		System.out.println("Accelerating " + where_to);
	}

	public List<Subscription_update> build_message_update() {
		
		Subscription_update.Builder ns_builder = Subscription_update.newBuilder();
		ns_builder.setID(Subscribeable.ACCELERATE_NORTHSOUTH.get_id());
		ns_builder.setInt32Value(where_to.get_north_south());
		
		Subscription_update.Builder ew_builder = Subscription_update.newBuilder();
		ew_builder.setID(Subscribeable.ACCELERATE_EASTWEST.get_id());
		ew_builder.setInt32Value(where_to.get_east_west());
		
		return Arrays.asList(ns_builder.build(), ew_builder.build());
	}
}
