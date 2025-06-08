package abstract_factory.pattern;

public class BankFactory {

	public static Bank getBankObj(BankAbstractFactory b) {
		return b.getObj();
	}
}
