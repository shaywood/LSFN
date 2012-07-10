package com.wikispaces.lsfn.Shared.Subscriptions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.wikispaces.lsfn.Shared.LSFN.SI.Subscription_output_updates.Subscription_update;
import com.wikispaces.lsfn.Shared.LSFN.SI.Subscriptions_available.Value_details.Value_type;
import com.wikispaces.lsfn.Shared.SubscribeableNotFoundException;

public abstract class Subscribeable {
	private int id;
	private String description;
	private Value_type value_type;
	
	private static Set<Subscribeable> values = new HashSet<Subscribeable>(Arrays.asList(
		new TestSubscribeable()
	));
	
	public int get_id() {
		return id;
	}
	
	public String get_description() {
		return description;
	}
	
	public Value_type get_value_type() {
		return value_type;
	}
	
	protected Subscribeable(int id, String description, Value_type value_type) {
		this.id = id;
		this.description = description;
		this.value_type = value_type;
	}
	
	public static Set<Subscribeable> get_all_available_subscribeables() {
		return Subscribeable.values; 
		/* In the future, we may want some way to filter some of these out (e.g. based on ship capabilities, or disabled features in the environment config). 
		 * When that happens, we should probably move this method out into another class. */
	}
	
	public static Subscribeable lookup_by_id(int id) throws SubscribeableNotFoundException {
		for (Subscribeable s : Subscribeable.values) {
			if(id == s.id) {
				return s;
			}
		}
			
		throw new SubscribeableNotFoundException(id); 
	}
	
	protected void Publish_Failed_Exception(String failed_value) throws PublishFailedException {
		throw new Subscribeable.PublishFailedException(this, failed_value);
	}
	
	public abstract Subscription_update build_subscription_update();
	public abstract void parse_subscription_update(Subscription_update s) throws PublishFailedException;
	
	@SuppressWarnings("serial")
	public class PublishFailedException extends Exception {
		public PublishFailedException(Subscribeable s, String failed_value) {
			super("Publishing subcription with id " + s.get_id() + " and name " + s.get_description() + " failed. Received value: " + failed_value);
		}
	}
}
