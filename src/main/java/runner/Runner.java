package runner;

public class Runner {
	private String name;
	private boolean isDaemon;
	private Thread t;
	private Runnable r;
	
	public Thread buildThread(){
		Thread t = new Thread(r, name);
		t.setDaemon(isDaemon);
		return t;
	}
	
	public Runner(String name, boolean isDaemon, Runnable r) {
		this.name = name;
		this.isDaemon = isDaemon;
		this.r = r;
	}

	public String getName() {
		return name;
	}

	public Thread getT() {
		return t;
	}

	public Runnable getR() {
		return r;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setT(Thread t) {
		this.t = t;
	}

	public void setR(Runnable r) {
		this.r = r;
	}
	
}
