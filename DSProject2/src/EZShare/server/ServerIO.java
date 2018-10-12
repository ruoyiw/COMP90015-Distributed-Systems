package EZShare.server;

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
	private static int uport;
	private static int sport;
	// A Logger object is used to log messages for a specific system or application component. 
	private static Logger logger = LogManager.getLogger(ServerIO.class);
	
	/**
	 *  initialize the port
	 * @param port
	 */
	public ServerIO(int uport, int sport){
		this.uport = uport;
		this.sport = sport;
		logger.info("bound to secure port "+sport+", "+"bound to unsecure port "+uport);
	}
	
	/**
	 *  get the value of port
	 * @return
	 */
	public int getUport(){
		return uport;
	}
	
	public int getSport(){
		return sport;
	}
	
	/**
	 * set the value of port
	 * @param port
	 */
	public static void setUport(int port){
		uport = port;
	}
	
	public static void setSport(int port){
		sport = port;
	}
	
}
