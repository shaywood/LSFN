package com.wikispaces.lsfn.Interface;

import java.util.List;

import com.wikispaces.lsfn.Interface.Model.KnownSpace;
import com.wikispaces.lsfn.Shared.LSFN.IS.Subscription_input_updates.Subscription_update;

public interface PlayerCommand {
	boolean can_combine(PlayerCommand c);
	PlayerCommand combine_with(PlayerCommand c);
	void update_local_model(KnownSpace model);
	List<Subscription_update> build_message_update();
}
