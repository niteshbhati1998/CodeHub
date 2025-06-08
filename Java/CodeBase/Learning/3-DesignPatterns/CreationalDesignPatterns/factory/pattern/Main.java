package factory.pattern;

class Main {

	public static void main(String[] args) throws Exception {
		Bank b1 = BankFactory.getBankObject("SBI");
		System.out.println(b1.ROI());
		
		Bank b2 = BankFactory.getBankObject("BOB");
		System.out.println(b2.ROI());
	}
}