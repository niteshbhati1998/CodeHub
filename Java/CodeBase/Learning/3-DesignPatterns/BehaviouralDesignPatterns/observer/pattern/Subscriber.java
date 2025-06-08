package observer.pattern;

class Subscriber implements Observer{

	private String name;

	public Subscriber(String name) {
		this.name = name;
	}

	@Override
	public void notified() {
		System.out.println("Hello "+this.name+" New video uploaded");
	}
}
