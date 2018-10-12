package EZShare.server.insecure.subscribe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import EZShare.server.ServerRecordList;
import EZShare.server.insecure.ServerServices;

public class InsecureSubscribeNewServerThread extends Thread{
	private JSONObject command;
	private ServerRecordList serverRecordList;
	private DataOutputStream output;
	private static final Logger logger = LogManager.getLogger(InsecureSubscribeNewServerThread.class);
	
	public InsecureSubscribeNewServerThread(JSONObject command, ServerRecordList serverRecordList, DataOutputStream output){
		this.command = command;
		this.serverRecordList = serverRecordList;
		this.output = output;
	}
	public void run(){
		String id = (String) command.get("id");
		while(ServerServices.insecureSubscribeManager.get(id).getIsOpen()){

			try {
				String se;
				se = serverRecordList.notifyNewServerRecord();
				subscribeOtherServer(se);
				serverRecordList.setNewResourceNull();
			} catch (InterruptedException e) {

			}
			
		}
	}
	
    private void subscribeOtherServer(String se){
    	String[] hostPort = se.split(":");
		String ip = hostPort[0];
		String portStr = hostPort[1];
		int port = Integer.parseInt(portStr);

		try (Socket socket = new Socket(ip, port);) {
			JSONParser parser = new JSONParser();
			logger.info("subscribing to " + ip + ":" + port);
			// Output and Input Stream
			DataInputStream serverInput = new DataInputStream(socket.getInputStream());
			DataOutputStream serverOutput = new DataOutputStream(socket.getOutputStream());

			//output the query command to the server
			serverOutput.writeUTF(command.toJSONString());
			serverOutput.flush();
			logger.info("SENT: " + command.toJSONString());

			//receive the JSON result from other server
			while (true) {
				if (serverInput.available() > 0) {
					// Attempt to convert read data to JSON
					// JSONObject command = (JSONObject)
					// parser.parse(input.readUTF());
					JSONObject serverCommand = null;
					try {
						serverCommand = (JSONObject) parser.parse(serverInput.readUTF());
					} catch (org.json.simple.parser.ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    
					//if the JOSN contains the information about a resource,
					//store it in the resource list
					if (serverCommand.containsKey("name") && serverCommand.containsKey("tags")
							&& serverCommand.containsKey("description") && serverCommand.containsKey("uri")
							&& serverCommand.containsKey("channel") && serverCommand.containsKey("owner")
							&& serverCommand.containsKey("ezserver")) {
						String id = (String) command.get("id");
						ServerServices.insecureSubscribeManager.get(id).addResouceSize();
						output.writeUTF(serverCommand.toJSONString());
						output.flush();
						
						logger.info("SENT: " + serverCommand.toJSONString());
						
					}

				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {

		}
    }
}
