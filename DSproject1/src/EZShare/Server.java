package EZShare;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * This class is the main class for servers which can communicate with the each
 * other. The main function of this class: 1. configurable on the command line
 * when the server is run 2. receive the JSON message from the multiple clients
 * at the same time 3. based on the JSON message, choose which function to
 * execute.
 * 
 * @author Ruoyi Wang, Luxin Weng, Qiulei Zhang, Huanan Li
 *
 */
public class Server {

	// A Logger object is used to log messages for a specific system or
	// application component.
	private static final Logger logger = LogManager.getLogger(Server.class);

	// Declare the port number
	private static int UPORT = 3001;
	private static int SPORT = 3781;

	// Declare the initialization of connectionintervallimit
	private static final int CILIMIT = 1;

	// Declare the initialization of exchangeinternal
	private static final int EINTERNAL = 60000;

	// Identifies the user number connected
	private static int counter = 0;

	private static HashMap<InetAddress, Long> ipTime = new HashMap<InetAddress, Long>();

	// Identifies variables
	private static ServerControl server_control = null;
	private static ServerIO serverIO = null;
	private static ServerInteractions serverInteraction = null;
	private static ServerServices svs;

	public static void main(String[] args) {

		// System.out.println(time.getTime());
		// using options, parse the arguments into the command line
		CommandLine cmd = ParseCommandLineOptions(args);

		// if the argument uses a wrong type, the variable "cmd" is null
		if (cmd == null) {
			System.out.println("the type of the command is incorrect");
		}
		// it the type of the argument is correct, execute the corresponding
		// function
		else {
			logger.info("Starting the EZShare Server");
			// set the variables
			// based on configurable on the command line when the server is run
			initialServerControl(cmd);
			initialServerIO(cmd);
			// create a serverServices instance
			svs = new ServerServices();
			// exchange automatically based on the server record list
			serverInteraction = new ServerInteractions(server_control.getExchangeinternal());
			// start the server
			serverStart();
		}
	}

	/**
	 * using options, parse the arguments into the command line
	 * 
	 * @param args
	 *            the input arguments when executing the client.class
	 * @return the command line
	 */
	public static CommandLine ParseCommandLineOptions(String[] args) {

		// create a new options
		Options options = new Options();

		// The server must work exactly with the following command line options
		options.addOption("advertisedhostname", true, "advertised hostname");
		options.addOption("connectionintervallimit", true, "connection interval limit in seconds");
		options.addOption("exchangeinternal", true, "exchange interval in seconds");
		options.addOption("port", true, "server port, an integer");
		options.addOption("secret", true, "secret");
		options.addOption("debug", false, "debug modes on/off");

		// parse the command line options
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(options, args);
			return cmd;
		} catch (org.apache.commons.cli.ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ant", options);
		}

		// if there is a parseException, return null
		return null;
	}

	/**
	 * configure the variables in the server
	 * 
	 * @param cmd
	 *            the command line
	 */
	public static void initialServerControl(CommandLine cmd) {

		// set the default value
		// The default secret will be a large random string
		String secret = getRandomString(26);

		// 1 second by default but configurable on the command line
		int connectionintervallimit = CILIMIT;

		// The default advertised host name will be the operating system
		// supplied hostname
		String advertisedhostname = null;
		try {
			InetAddress addr = InetAddress.getLocalHost();
			advertisedhostname = addr.getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// The default exchange interval will be 10 minutes (600 seconds).
		int exchangeinternal = EINTERNAL;
		boolean debug = false;

		// set the variables if there is a configuration of these variables
		// when the server is running
		if (cmd.hasOption("advertisedhostname")) {
			advertisedhostname = cmd.getOptionValue("advertisedhostname");
		}

		if (cmd.hasOption("connectionintervallimit")) {
			connectionintervallimit = Integer.parseInt(cmd.getOptionValue("connectionintervallimit"));
		}

		if (cmd.hasOption("exchangeinternal")) {
			exchangeinternal = Integer.parseInt(cmd.getOptionValue("exchangeinternal"));
		}

		if (cmd.hasOption("secret")) {
			secret = cmd.getOptionValue("secret");
		}

		if (cmd.hasOption("debug")) {
			debug = true;
		}

		// create a new serverControl to store these variables
		server_control = new ServerControl(advertisedhostname, connectionintervallimit, exchangeinternal, secret,
				debug);
	}

	/**
	 * generate a random string with a specified length
	 * 
	 * @param length
	 *            the length of the string
	 * @return the created random string
	 */
	public static String getRandomString(int length) {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	/**
	 * initial the port number of the server
	 * 
	 * @param cmd
	 *            the command line
	 */
	public static void initialServerIO(CommandLine cmd) {

		// the port number of the server
		// the default value of the port is 3000
		int uport = UPORT;
		int sport = SPORT;

		// if the command line has the port, renew the port number
		if (cmd.hasOption("port")) {
			uport = Integer.parseInt(cmd.getOptionValue("port"));
			serverIO = new ServerIO(uport);
		} else if (cmd.hasOption("sport")) {
			sport = Integer.parseInt(cmd.getOptionValue("sport"));
			serverIO = new ServerIO(sport);
		}

	}

	/**
	 * start the server and then the server is ready to receive the JSON message
	 * from client If there are multiple clients requesting to connect at the
	 * same time, the server will create multiple threads for each client
	 * 
	 */
	private static void serverStart() {
		// Specify the keystore details (this can be specified as VM arguments
		// as well)
		// the keystore file contains an application's own certificate and
		// private key
		System.setProperty("javax.net.ssl.keyStore", "clientKeystore/client.jks");
		// Password to access the private key from the keystore file
		System.setProperty("javax.net.ssl.keyStorePassword", "comp90015");
		// create a factory to manage the sockets
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		// start the exchange thread. Thus the server can exchange
		// automatically.
		serverInteraction.start();

		try (ServerSocket server = factory.createServerSocket(serverIO.getPort())) {

			// Wait for connections.
			while (true) {
				// if there a client want to connect, accept the request of
				// connection
				Date time = new Date();
				Socket client = server.accept();

				if (ipTime.containsKey(client.getInetAddress())) {
					if ((time.getTime() - ipTime.get(client.getInetAddress())) > server_control
							.geConnectionintervallimit() * 1000) {
						ipTime.replace(client.getInetAddress(), time.getTime());
					} else {
						client.close();
						continue;
					}
				} else {
					ipTime.put(client.getInetAddress(), time.getTime());
				}

				counter++;
				logger.info("new connection from " + client.getInetAddress() + ":" + client.getPort());

				// Start a new thread for a connection
				Thread t = new Thread(() -> serveClient(client));
				t.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * deal with the JSON message interaction with client
	 * 
	 * @param client
	 */
	private static void serveClient(Socket client) {
		try (Socket clientSocket = client) {

			// The JSON Parser
			JSONParser parser = new JSONParser();
			// Input stream
			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
			// Output Stream
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			// System.out.println("CLIENT: " + input.readUTF());
			// output.writeUTF("Server: Hi Client " + counter + " !!!");

			// Receive more data..
			while (true) {
				if (input.available() > 0) {
					// Attempt to convert read data to JSON
					JSONObject command = (JSONObject) parser.parse(input.readUTF());
					logger.info("RECEIVED: " + command.toJSONString());

					// gain the output result from different function in terms
					// of JSON
					JSONObject result = parseCommand(command, output);
					logger.info("SENT: " + result.toJSONString());

					// send JSON message to the client
					output.writeUTF(result.toJSONString());
				}
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * parse the JSON message to execute different function
	 * 
	 * @param command
	 *            the JSON message received from the client
	 * @param output
	 *            the output stream to client
	 * @return the execution result
	 */
	private static JSONObject parseCommand(JSONObject command, DataOutputStream output) {

		JSONObject result = new JSONObject();

		// judge which function to execute according to receiving JSON message
		if (command.containsKey("command")) {
			if (command.get("command").equals("PUBLISH")) {
				result = svs.publish(command, server_control, serverIO);
			} else if (command.get("command").equals("REMOVE")) {
				result = svs.remove(command);
			} else if (command.get("command").equals("QUERY")) {
				result = svs.query(command, output);
			} else if (command.get("command").equals("EXCHANGE")) {
				result = svs.exchange(command);
			} else if (command.get("command").equals("SHARE")) {
				result = svs.share(command, server_control, serverIO);
			} else if (command.get("command").equals("FETCH")) {
				result = svs.fetch(command, output);
			} else {
				// the invalid command
				result.put("response", "error");
				result.put("errorMessage", "invalid command");
			}

		}
		return result;
	}

}
