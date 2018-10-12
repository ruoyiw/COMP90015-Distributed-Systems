package EZShare.server.secure.subscribe;


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
import EZShare.server.secure.ServerSecureServices;

public class SecureSubscribeNewServerThread extends Thread{
	private JSONObject command;
	private ServerRecordList serverRecordList;
	private BufferedWriter output;
	private static final Logger logger = LogManager.getLogger(SecureSubscribeServerThread.class);
	
	public SecureSubscribeNewServerThread(JSONObject command, ServerRecordList serverRecordList, BufferedWriter output){
		this.command = command;
		this.serverRecordList = serverRecordList;
		this.output = output;
	}
	public void run(){
		String id = (String) command.get("id");
		while(ServerSecureServices.secureSubscribeManager.get(id).getIsOpen()){
			String se;
			try {
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

		try {
            SSLSocket sslRelaySocket;
			//Create SSL socket and connect it to the remote server 
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			sslRelaySocket = (SSLSocket) sslsocketfactory.createSocket(ip, port);
			
			JSONParser parser = new JSONParser();
			logger.info("subscribing to " + ip + ":" + port);
			//Create buffered writer to send data to the server
			OutputStream outputstream = sslRelaySocket.getOutputStream();
			OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
			BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);
			
			//Create buffered reader to read input from the console
			InputStream inputstream = sslRelaySocket.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

			//output the query command to the server
			bufferedwriter.write(command.toJSONString()+ '\n');
			bufferedwriter.flush();
			logger.info("SENT: " + command.toJSONString());
			
			String string = null;					
			//receive the JSON result from other server
			String id = (String) command.get("id");
			while (ServerSecureServices.secureSubscribeManager.get(id).getIsOpen()) {
				if ((string = bufferedreader.readLine()) != null) {
					// Attempt to convert read data to JSON
					// JSONObject command = (JSONObject)
					// parser.parse(input.readUTF());
					JSONObject serverCommand = null;
					try {
						serverCommand = (JSONObject) parser.parse(string);
					} catch (org.json.simple.parser.ParseException e) {
						System.out.println("parser the other server command problem");
					}
                    
					//if the JOSN contains the information about a resource,
					//store it in the resource list
					if (serverCommand.containsKey("name") && serverCommand.containsKey("tags")
							&& serverCommand.containsKey("description") && serverCommand.containsKey("uri")
							&& serverCommand.containsKey("channel") && serverCommand.containsKey("owner")
							&& serverCommand.containsKey("ezserver")) {
						ServerSecureServices.secureSubscribeManager.get(id).addResouceSize();
						output.write(serverCommand.toJSONString()+ '\n');
						output.flush();
						
					}

				}
			}
			sslRelaySocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {

		}
    }
    
    
}
