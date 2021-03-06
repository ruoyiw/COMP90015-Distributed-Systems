package EZShare;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * This class is the thread class for handling the automatically exchange command
 * which is executed by server in the certain time.
 * 
 * @author Ruoyi Wang, Luxin Weng, Qiulei Zhang, Huanan Li
 *
 */
public class ServerInteractions extends Thread{
	//the internal time which server will do the exchange command automatically
	private int exchangeInternal;
	private static Logger logger = LogManager.getLogger(ServerInteractions.class);
	//the server record which the server maintain
	private ArrayList<String> serverRecords; 
	
	//the exchange internal time is controlled by the command when the server
	//start
	public ServerInteractions(int exchangeInternal){
		this.exchangeInternal = exchangeInternal;
		logger.info("started");
	}
	
	public void run() {
	    while (true) {
	    	
	        try{
	        	//change the exchange internal time from second to millisecond
	        	exchangeInternal = exchangeInternal*1000;
	        	serverRecords = ServerServices.getServerRecordList();
	        	
	        	//check if the server has a non-empty server record
	        	if(serverRecords.isEmpty() || serverRecords == null){
	        		logger.info("no known servers to exchange with");
	        	}else{
		        	int serverRecordsNum = serverRecords.size();
		        	//select a server from the server record randomly
		        	Random r =new Random(); 
		        	String selectedServer = serverRecords.get(r.nextInt(serverRecordsNum));
		        	String[] hostPort = selectedServer.split(":");
	    			String ip = hostPort[0];
	    			String portStr = hostPort[1];
	    			int port =  Integer.parseInt(portStr);
	    			
	    			//connect the server and send it the server record
		        	try (Socket socket = new Socket(ip, port);) {
		    			
		    			// Output and Input Stream
		    			DataInputStream input = new DataInputStream(socket.getInputStream());
		    			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
	
		    			JSONObject serverExchangeCommand = new JSONObject();
		    			serverExchangeCommand.put("command", "EXCHANGE");
		    			
		    			JSONArray serversJSON = new JSONArray();
						//change the server record to the JOSN format
						for (String s: serverRecords) {
							JSONObject host = new JSONObject();
							//change each server record string to JSON
							String[] hostnamePort = s.split(":");
							String hostname = hostnamePort[0];
							String portVal = hostnamePort[1];
							host.put("hostname", hostname);
							host.put("port", portVal);
							serversJSON.add(host);
						}
						
						//organize the JSON format to ensure it can use as a 
						//exchange command and processed by the other server
						//as a same command received by the client
						
						serverExchangeCommand.put("serverList", serversJSON);
						//send the exchange command
		    			output.writeUTF(serverExchangeCommand.toJSONString());
		    			output.flush();
		    			logger.info("SENT: " + serverExchangeCommand.toJSONString());
		    			// Print out results received from server..
		    			String result = input.readUTF();
		    			logger.info("RECEIVED: " + result);
	
		    		} catch (UnknownHostException e) {
		    			e.printStackTrace();
		    		} catch (IOException e) {
	
		    		}
	        	}
		        
	        	//wait certain time to do exchange again
	            Thread.sleep(this.exchangeInternal);
	            
	        }catch (InterruptedException e) {

	        }
	    }
	}
}
