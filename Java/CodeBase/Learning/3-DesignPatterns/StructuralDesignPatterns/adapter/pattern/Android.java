package adapter.pattern;

public class Android {

	private AndroidCharger androidCharger;
	
	public Android(AndroidCharger androidCharger) {
		this.androidCharger = androidCharger;
	}

	public void chargeRequired() {
		androidCharger.chargeAndroid();
	}
}
