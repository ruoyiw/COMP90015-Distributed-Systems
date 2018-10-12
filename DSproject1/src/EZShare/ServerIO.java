package EZShare;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * store the port number of the server
 * 
 * @author Ruoyi Wang, Luxin Weng, Qiulei Zhang, Huanan Li
 *
 */
public class ServerIO {
	// the port number of the server. The default value is 3000
	private static int port = 3000;
	// A Logger object is used to log messages for a specific system or
	// application component.
	private static Logger logger = LogManager.getLogger(ServerIO.class);

	/**
	 * initialize the port
	 * 
	 * @param port
	 */
	public ServerIO(int port) {
		this.port = port;
		logger.info("bound to port " + port);
	}

	/**
	 * get the value of port
	 * 
	 * @return
	 */
	public int getPort() {
		return port;
	}

	/**
	 * set the value of port
	 * 
	 * @param port
	 */
	public static void setPort(int port) {
		port = port;
	}

}
