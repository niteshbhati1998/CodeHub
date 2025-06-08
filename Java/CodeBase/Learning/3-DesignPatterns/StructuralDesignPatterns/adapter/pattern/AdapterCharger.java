package adapter.pattern;

class AdapterCharger implements AppleCharger{

	private AndroidCharger androidCharger;
	
	public AdapterCharger(AndroidCharger androidCharger) {
		this.androidCharger = androidCharger;
	}

	@Override
	public void chargeIphone() {
		androidCharger.chargeAndroid();
		System.out.println("using adapter charger");
	}
}