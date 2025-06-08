package abstract_factory.pattern;

class SBIFactory extends BankAbstractFactory{

	@Override
	public Bank getObj() {
	    return new SBI();
	}
}
