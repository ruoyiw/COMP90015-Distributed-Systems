package EZShare.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ClientUnsecureUnsubscribe extends Thread{
	
	private static Logger logger = LogManager.getLogger(ClientUnsecureUnsubscribe.class);
	DataOutputStream output;
	String ip;
	int port;
	int counter;
	
	public ClientUnsecureUnsubscribe(DataOutputStream output, String ip, int port, int counter){
		this.output = output;
		this.ip = ip;
		this.port = port;
		this.counter = counter;
	}
	
	public void run(){
		//Create buffered reader to read input from the console
		InputStream inputstream_console = System.in;
		InputStreamReader inputstreamreader_console = new InputStreamReader(inputstream_console);
		BufferedReader bufferedreader_console = new BufferedReader(inputstreamreader_console);
		
		try {
			if (bufferedreader_console.readLine() != null) {
				// JSON Message
				JSONObject unsubscribeCommand = new JSONObject();
				JSONParser parser = new JSONParser();
				unsubscribeCommand.put("command", "UNSUBSCRIBE");
				unsubscribeCommand.put("id", ip+":"+port+":"+counter);
				
				logger.info("SENT: "+unsubscribeCommand.toJSONString());
				output.writeUTF(unsubscribeCommand.toJSONString()+ '\n');
				output.flush();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
