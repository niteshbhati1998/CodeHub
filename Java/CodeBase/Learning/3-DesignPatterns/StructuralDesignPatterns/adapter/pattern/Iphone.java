package adapter.pattern;

class Iphone {

	private AppleCharger appleCharger;
	
	public Iphone(AppleCharger appleCharger) {
		this.appleCharger = appleCharger;
	}

	public void chargeRequired() {
		appleCharger.chargeIphone();
	}
}