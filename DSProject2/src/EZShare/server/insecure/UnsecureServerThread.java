package EZShare.server.insecure;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

import javax.net.ServerSocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import EZShare.server.ServerControl;
import EZShare.server.ServerIO;
import EZShare.server.secure.SecureServerThread;

public class UnsecureServerThread extends Thread{
	
	private static ServerIO serverIO = null;
	private static HashMap<InetAddress, Long> ipTime = null;
	private static ServerControl server_control = null;
	private static int counter = 0;
	private static ServerServices serverServices = new ServerServices();
	private static final Logger logger = LogManager.getLogger(UnsecureServerThread.class);
	
	public UnsecureServerThread(ServerIO serverIO, HashMap<InetAddress, Long> ipTime,
			ServerControl server_control,int counter){
		this.serverIO = serverIO;
		this.ipTime = ipTime;
		this.server_control = server_control;
		this.counter = counter;
	}
	
    public void run(){

		// create a factory to manage the sockets
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		// start the exchange thread. Thus the server can exchange automatically.
		
		try (ServerSocket server = factory.createServerSocket(serverIO.getUport())) {
					
			// Wait for connections.
			while (true) {
				// if there a client want to connect, accept the request of connection
				Date time = new Date();
				Socket client = server.accept();
				
				if(ipTime.containsKey(client.getInetAddress())){
					if((time.getTime()-ipTime.get(client.getInetAddress()))>
										server_control.geConnectionintervallimit()*1000){
						ipTime.replace(client.getInetAddress(), time.getTime());
					}else{
						client.close();
						continue;
					}
				}else{
					ipTime.put(client.getInetAddress(), time.getTime());
				}
				

				counter++;
				logger.info("new connection from "+client.getInetAddress()+":"+client.getPort());


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
	 * @param client
	 * @throws InterruptedException 
	 */
	private static void serveClient(Socket client){
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
					
					// gain the output result from different function in terms of JSON
					JSONObject result;
					result = parseCommand(command, output); 
					if(!result.isEmpty()){
    					logger.info("SENT: " + result.toJSONString());
					
					// send JSON message to the client
					output.writeUTF(result.toJSONString());
					}
				}
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * parse the JSON message to execute different function
	 * 
	 * @param command the JSON message received from the client
	 * @param output the output stream to client
	 * @return the execution result
	 * @throws InterruptedException 
	 */
	private static JSONObject parseCommand(JSONObject command, DataOutputStream output) throws InterruptedException {

		JSONObject result = new JSONObject();
		
		// judge which function to execute according to receiving JSON message
		if(command.containsKey("command")){
			if (command.get("command").equals("PUBLISH")) {
				result = serverServices.publish(command, server_control, serverIO);
			} else if (command.get("command").equals("REMOVE")) {
				result = serverServices.remove(command);
			} else if (command.get("command").equals("QUERY")) {
				result = serverServices.query(command,output);
			} else if (command.get("command").equals("EXCHANGE")) {
				result = serverServices.exchange(command);
			} else if (command.get("command").equals("SHARE")) {
				result = serverServices.share(command, server_control, serverIO);
			} else if (command.get("command").equals("FETCH")) {
				result = serverServices.fetch(command,output); 
			} else if (command.get("command").equals("SUBSCRIBE")) {
				result = serverServices.subscribe(command,output);
			} else if (command.get("command").equals("UNSUBSCRIBE")) {
					result = serverServices.unsubscribe(command,output);
			} else {
				// the invalid command
				result.put("response", "error");
				result.put("errorMessage", "invalid command");
			}

		}
		return result;
	}
}
