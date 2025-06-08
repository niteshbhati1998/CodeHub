package singleton.pattern;

class Main {

	public static void main(String[] args) throws Exception {
		Test t1 = Test.getObj();
		Test t2 = Test.getObj();
		
		System.out.println(t1.hashCode());
		System.out.println(t2.hashCode());
	}
}