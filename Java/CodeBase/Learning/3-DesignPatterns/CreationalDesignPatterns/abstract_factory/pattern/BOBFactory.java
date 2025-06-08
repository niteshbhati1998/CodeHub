package abstract_factory.pattern;

class BOBFactory extends BankAbstractFactory{

	@Override
	public Bank getObj() {
	    return new BOB();
	}
}
