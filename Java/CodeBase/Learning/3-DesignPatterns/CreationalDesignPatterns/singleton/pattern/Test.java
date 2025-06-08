package singleton.pattern;

class Test {
	
	private static Test test;
	
	private Test() {	
	}
	
	public static Test getObj() {
		if(test==null) {
			synchronized(Test.class) {
				if(test==null) {
					test = new Test();
				}
			}
		}
		return test;
	}	
}