package runner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class RunnerManager implements Runnable{
	
	private Map<String, Runner> threads = new ConcurrentHashMap<String, Runner>();
	private static Logger  log = Logger.getLogger("world");


	public void addRunner(Runnable r, String name){
		Runner re = new Runner(name, false, r);
		threads.put(re.getName(), re);
		Thread t = re.buildThread();
		re.setT(t);
		t.start();
	}
	
	public boolean update() {
		for(String name : threads.keySet()){
			Runner re = threads.get(name);
			Thread original = re.getT();
			if(original == null || !original.isAlive()){
				Thread t = re.buildThread();
				re.setT(t);
				t.start();
				log.info("Runner restart :" + name  + "," + t.getState() + "," + (original == null ? null : original.getState()));
			}
		}
		return true;
	}

	@Override
	public void run() {
		try{
			while(true){
				Thread.sleep(1000);
				this.update();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
