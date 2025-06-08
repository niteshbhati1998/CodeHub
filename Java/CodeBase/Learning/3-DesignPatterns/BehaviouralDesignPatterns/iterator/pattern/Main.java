package iterator.pattern;

class Main {

	public static void main(String[] args) {
		UserManagement u = new UserManagement();
		u.addUser(new User("A",1));
		u.addUser(new User("B",2));
		u.addUser(new User("C",3));
		
	    MyIterator itr = u.getIterator();
	    while(itr.hasNext()) {
	    	User user = (User) itr.next();
	    	System.out.println(user.getName());
	    }
	}
}
