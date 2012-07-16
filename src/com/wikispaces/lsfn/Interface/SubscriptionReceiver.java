package com.wikispaces.lsfn.Interface;

import java.util.List;

import com.wikispaces.lsfn.Interface.SubscriptionMessageParser.PublishFailedException;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscription_output_updates;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscription_output_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.SubscribeableOutput;
import com.wikispaces.lsfn.Shared.SubscribeableNotFoundException;

public class SubscriptionReceiver {
	public void parse_subscription_outputs_data(Subscription_output_updates output_updates) throws PublishFailedException, NoSubscriptionParserDefinedException, SubscribeableNotFoundException  {
		List<Subscription_update> updates = output_updates.getUpdatesList();
		
		for (Subscription_update u : updates) {
			SubscribeableOutput s = SubscribeableOutput.lookup_by_id(u.getOutputID());
			SubscriptionMessageParser.get_parser(s).parse_subscription_update(u);
		}
	}
}
