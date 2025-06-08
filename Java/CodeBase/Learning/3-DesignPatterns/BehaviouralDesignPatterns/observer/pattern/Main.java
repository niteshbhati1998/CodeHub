package observer.pattern;

public class Main {

	public static void main(String[] args) {
		Subject sub = new YoutubeChannel();
		Observer obs1 = new Subscriber("Nitesh");
		Observer obs2 = new Subscriber("Aman");
		
		sub.subscribe(obs1);
		sub.subscribe(obs2);
		
		sub.notifyChanges();	
	}
}
