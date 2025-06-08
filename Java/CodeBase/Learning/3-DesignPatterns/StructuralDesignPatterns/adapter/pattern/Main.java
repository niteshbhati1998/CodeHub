package adapter.pattern;

class Main {

	public static void main(String[] args) {
		Iphone iphone = new Iphone(new AppleChargerXYZCompany());
		iphone.chargeRequired();
		
		Android android = new Android(new AndroidChargerXYZCompany());
		android.chargeRequired();
		
		//charge iphone using android charger
		Iphone iphone1 = new Iphone(new AdapterCharger(new AndroidChargerXYZCompany()));
		iphone1.chargeRequired();
	}
}
