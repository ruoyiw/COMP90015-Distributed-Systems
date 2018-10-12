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

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import EZShare.server.ServerRecordList;
import EZShare.server.insecure.ServerServices;
import EZShare.server.secure.ServerSecureServices;

public class SecureSubscribeServerThread extends Thread{
	private JSONObject command;
	private ServerRecordList secureServerRecordList;
	private BufferedWriter output;
	private static final Logger logger = LogManager.getLogger(SecureSubscribeServerThread.class);
	private SecureSubscribeNewServerThread ssnst;

	public SecureSubscribeServerThread(JSONObject command, ServerRecordList secureServerRecordList, BufferedWriter output){
		this.command = command;
		this.secureServerRecordList = secureServerRecordList;
		this.output = output;
		this.ssnst = new SecureSubscribeNewServerThread(command, secureServerRecordList, output);
		//this.snst = new SubscribeNewServerThread(command, ServerServices.serverRecordList, output);
	}

    public void run(){
    	
		JSONObject serverResourceTemplate = (JSONObject) command.get("resourceTemplate");
        
		//change the JSON command to hide some information
		command.replace("relay", false);
		serverResourceTemplate.replace("owner", "");
		serverResourceTemplate.replace("channel", "");
		command.replace("resourceTemplate", serverResourceTemplate);

		this.secureServerRecordList.setNewResourceNull();

		//connect with all the servers in the server list
		for (String se : this.secureServerRecordList.getServerRecordList()) {
			secureSubscribeOtherServer(se);
		}
		
//		for (String se : ServerServices.serverRecordList.getServerRecordList()) {
//			subscribeOtherServer(se);
//		}
		
		ssnst.start();
		//snst.start();
		
    }
    
    private void secureSubscribeOtherServer(String se){
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
						logger.info("SENT: " + serverCommand.toJSONString());
						
					}

				}
			}
			sslRelaySocket.close();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {

		}
    }
    
//    private void subscribeOtherServer(String se){
//    	String[] hostPort = se.split(":");
//		String ip = hostPort[0];
//		String portStr = hostPort[1];
//		int port = Integer.parseInt(portStr);
//
//		try (Socket socket = new Socket(ip, port);) {
//			JSONParser parser = new JSONParser();
//			logger.info("querying to " + ip + ":" + port);
//			// Output and Input Stream
//			DataInputStream serverInput = new DataInputStream(socket.getInputStream());
//			DataOutputStream serverOutput = new DataOutputStream(socket.getOutputStream());
//
//			//output the query command to the server
//			serverOutput.writeUTF(command.toJSONString());
//			serverOutput.flush();
//			logger.info("SENT: " + command.toJSONString());
//
//			//receive the JSON result from other server
//			while (true) {
//				if (serverInput.available() > 0) {
//					// Attempt to convert read data to JSON
//					// JSONObject command = (JSONObject)
//					// parser.parse(input.readUTF());
//					JSONObject serverCommand = null;
//					try {
//						serverCommand = (JSONObject) parser.parse(serverInput.readUTF());
//					} catch (org.json.simple.parser.ParseException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//                    
//					//if the JOSN contains the information about a resource,
//					//store it in the resource list
//					if (serverCommand.containsKey("name") && serverCommand.containsKey("tags")
//							&& serverCommand.containsKey("description") && serverCommand.containsKey("uri")
//							&& serverCommand.containsKey("channel") && serverCommand.containsKey("owner")
//							&& serverCommand.containsKey("ezserver")) {
//
//						output.write(serverCommand.toJSONString());
//						output.flush();
//						
//					}
//
//				}
//			}
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//
//		}
//    }
}
