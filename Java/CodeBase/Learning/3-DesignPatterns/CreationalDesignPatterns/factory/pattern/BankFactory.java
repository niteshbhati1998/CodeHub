package factory.pattern;

class BankFactory {

	public static Bank getBankObject(String type) {
		
		if(type.equalsIgnoreCase("SBI")) {
			return new SBI();
		} else if(type.equalsIgnoreCase("BOB")) {
			return new BOB();
		} else {
			return null;
		}
	}
}
