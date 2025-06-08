package iterator.pattern;

import java.util.ArrayList;
import java.util.List;

public class UserManagement {

	private List<User> list = new ArrayList<>();
	
	public void addUser(User user) {
		list.add(user);
	}

	public MyIterator getIterator() {
		return new MyIteratorImpl(list);
	}
}
