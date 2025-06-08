package abstract_factory.pattern;

class Main {

	public static void main(String[] args) {
		Bank b1 = BankFactory.getBankObj(new SBIFactory());
		System.out.println(b1.ROI());
		
		Bank b2 = BankFactory.getBankObj(new BOBFactory());
		System.out.println(b2.ROI());
	}
}
