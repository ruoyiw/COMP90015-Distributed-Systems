package EZShare;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is the main class for clients which can communicate with the servers.
 * The main function of this class is to parsing command line options,
 * and based on the input arguments, choose which function to execute.
 * 
 * @author Ruoyi Wang, Luxin Weng, Qiulei Zhang, Huanan Li
 *
 */
public class Client {
	
	// A Logger object is used to log messages for a specific system or application component. 
	private static final Logger logger = LogManager.getLogger(Client.class.getName());
	// identify IP and port
	private static String ip = "sunrise.cis.unimelb.edu.au";
	private static int uport = 3001;
	private static int sport = 3781;
	
	private static int port;

	public static void main(String[] args) {
		
		// using options, parse the arguments into the command line
		CommandLine cmd = ParseCommandLineOptions(args);
		
		// if the argument uses a wrong type, the variable "cmd" is null
		if(cmd == null){
			System.out.println("the type of the command is incorrect");
		}
		// it the type of the argument is correct, execute the corresponding function
		else{
			// control the project whether to show the communication info
			if (cmd.hasOption("debug")) {
				logger.info("setting debug on");
			}
			
			if (cmd.hasOption("secure")) {
				port = sport;
				//Location of the Java keystore file containing the collection of 
				//certificates trusted by this application (trust store).
				System.setProperty("javax.net.ssl.trustStore", "KeyStore/client.jks");
				//System.setProperty("javax.net.ssl.keyStorePassword","comp90015");
				//System.setProperty("javax.net.debug","all");
			}else{
				port = uport;
			}
			
			// judge which function to execute according to the command line
			if (cmd.hasOption("publish")) {
				ClientServices.publishCommand(cmd, ip, port);
			} else if (cmd.hasOption("remove")) {
				ClientServices.removeCommand(cmd, ip, port);
			} else if (cmd.hasOption("share")) {
				ClientServices.shareCommand(cmd, ip, port);
			} else if (cmd.hasOption("fetch")) {
				ClientServices.fetchCommand(cmd, ip, port);
			} else if (cmd.hasOption("query")) {
				ClientServices.queryCommand(cmd, ip, port);
			} else if (cmd.hasOption("exchange")) {
				ClientServices.exchangeCommand(cmd, ip, port);
			} else if (cmd.hasOption("subscribe")) {
				ClientServices.subscribeCommand(cmd, ip, port);
			} else if (cmd.hasOption("unsubscribe")) {
				ClientServices.unsubscribeCommand(cmd, ip, port);
			} else {
				System.out.println("Please input a valid command");
			}
		}

	}
	
	/**
	 * using options, parse the arguments into the command line
	 * @param args the input arguments when executing the client.class
	 * @return the command line
	 */
	public static CommandLine ParseCommandLineOptions(String[] args) {
		Options options = new Options();
		
		//The client must work exactly with the following command line options
		options.addOption("channel", true, "select a channel");
		options.addOption("debug", false, "debug modes on/off");
		options.addOption("description", true, "resource description");
		options.addOption("exchange", false, "exchange server list with server");
		options.addOption("fetch", false, "fetch reource from server");
		options.addOption("host", true, "server name, a domain name or IP address");
		options.addOption("name", true, "resource name");
		options.addOption("owner", true, "resource owner");
		options.addOption("port", true, "server port, an integer");
		options.addOption("publish", false, "select a function publish");
		options.addOption("query", false, "query for resources from server");
		options.addOption("remove", false, "remove resource from server");
		options.addOption("secret", true, "secret for server");
		options.addOption("servers", true, "server list,host1:port1,host2:port2,....");
		options.addOption("share", false, "share resource on server");
		options.addOption("tags", true, "resources tags,tag1,tag2,tag3");
		options.addOption("uri", true, "resources URI");
		options.addOption("secure", false, "secure");
		options.addOption("id",true,"id");
		
		// parse the command line options
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			return cmd;
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
	        formatter.printHelp( "ant", options );
		}
		// if there is a parseException, return null
		return null;
	}

}
