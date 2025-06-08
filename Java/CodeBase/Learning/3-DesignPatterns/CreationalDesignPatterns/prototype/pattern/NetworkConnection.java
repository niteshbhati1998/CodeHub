package prototype.pattern;

class NetworkConnection implements Cloneable{

	private String ip;
	private String loadImptData;
	
	public NetworkConnection(String ip) {
		this.ip = ip;
	}
	
	public void loadImptData() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.loadImptData = "Data loaded successfully";		
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public String toString() {
		return "NetworkConnection [ip=" + ip + ", loadImptData=" + loadImptData + "]";
	}
}
