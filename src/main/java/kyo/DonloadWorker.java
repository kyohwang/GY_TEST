package kyo;


public class DonloadWorker implements Runnable {

	@Override
	public void run() {
		while(true){
			try{
				Thread.sleep(20);
				String infoHash = Downloader.nextInfoHash();
				if(infoHash != null){
					Downloader.download(infoHash);
				}
							
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

}
