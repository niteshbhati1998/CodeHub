package prototype.pattern;

class Main {
	public static void main(String[] args) throws CloneNotSupportedException {
		NetworkConnection nc1 = new NetworkConnection("140.200.100.1");
		nc1.loadImptData();
		System.out.println(nc1);
		
		NetworkConnection nc2 = (NetworkConnection) nc1.clone();
		System.out.println(nc2);
	}
}  