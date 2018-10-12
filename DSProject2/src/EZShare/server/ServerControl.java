package EZShare.server;

import java.net.InetAddress;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * store the control information of the server, including advertisedhostname,
 * connectionintervallimit, exchangeinternal, secret, debug
 * 
 * @author Ruoyi Wang, Luxin Weng, Qiulei Zhang, Huanan Li
 *
 */
public class ServerControl {
	
	// create variables
	private String advertisedhostname;
	private int connectionintervallimit;
	private int exchangeinternal;
	private String secret;
	private boolean debug;
	// A Logger object is used to log messages for a specific system or application component. 
	private static Logger logger = LogManager.getLogger(ServerControl.class);
	
	private static HashMap<InetAddress, Long> ipTime = new HashMap<InetAddress, Long>();
	
	/**
	 *  initialize the control information
	 */
	ServerControl(String advertisedhostname, int connectionintervallimit,
			int exchangeinternal, String secret, boolean debug){
		this.advertisedhostname = advertisedhostname;
		this.connectionintervallimit = connectionintervallimit;
		this.exchangeinternal = exchangeinternal;
		this.secret = secret;
		this.debug = debug;	
		logger.info("using secret:"+this.secret);
		logger.info("using advertised hostname:"+this.advertisedhostname);
	}
	
	public HashMap<InetAddress, Long> getIPTime(){
		return ipTime;
	}
	
	public void resetIPTime(InetAddress ipAddress, Long time){
		ipTime.replace(ipAddress, time);
	}
	
	public void addIPTime(InetAddress ipAddress, Long time){
		ipTime.put(ipAddress, time);
	}
	
	/**
	 *  get the advertisedhostname
	 * @return
	 */
	public String getAdvertisedhostname(){
		return this.advertisedhostname;
	}
	
	/**
	 * set the advertisedhostname
	 * @param hostname
	 */
	public void setAdvertisedhostname(String hostname){
		this.advertisedhostname = hostname;
	}
	
	/**
	 * get connectionintervallimit
	 * @return
	 */
	public int geConnectionintervallimit(){
		return this.connectionintervallimit;
	}
	
	/**
	 *  set connectionintervallimit
	 * @param limitTime
	 */
	public void setConnectionintervallimit(int limitTime){
		this.connectionintervallimit = limitTime;
	} 
	
	/**
	 *  get exchangeinternal
	 * @return
	 */
	public int getExchangeinternal(){
		return this.exchangeinternal;
	}
	
	/**
	 * set exchangeinternal
	 * @param exchangeTime
	 */
	public void setExchangeinternal(int exchangeTime){
		this.exchangeinternal = exchangeTime;
	} 
	
	/**
	 * get secret
	 * @return
	 */
	public String getSecret(){
		return this.secret;
	}
	
	/**
	 * set secret
	 * @param secret
	 */
	public void setSecret(String secret){
		this.secret = secret;
	} 
	
	/**
	 * get debug
	 * @return
	 */
	public boolean getDebug(){
		return this.debug;
	}
	
	/**
	 * set debug
	 * @param debug
	 */
	public void setDebug(boolean debug){
		this.debug = debug;
	}
}
