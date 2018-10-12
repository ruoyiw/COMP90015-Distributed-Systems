package EZShare;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerExchanger extends Thread{
	
	private static Logger logger = LogManager.getLogger(ServerExchanger.class);
	
	public ServerExchanger(){
		logger.info("started");
	}

}
