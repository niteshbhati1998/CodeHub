package observer.pattern;

import java.util.ArrayList;
import java.util.List;

class YoutubeChannel implements Subject{

	List<Observer> subscriberList = new ArrayList<>();
	 
	@Override
	public void subscribe(Observer obs) {
		this.subscriberList.add(obs);
		System.out.println("subscribed");
	}

	@Override
	public void unSubscribe(Observer obs) {
		this.subscriberList.remove(obs);
		System.out.println("unsubscribed");
	}

	@Override
	public void notifyChanges() {
		for(Observer obs: subscriberList) {
			obs.notified();
		}
	}
}
