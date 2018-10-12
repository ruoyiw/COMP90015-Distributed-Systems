package EZShare.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.net.ssl.SSLSocket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ClientUnsubscribe extends Thread{
	
	SSLSocket sslsocket;
	BufferedWriter bufferedwriter;
	String ip;
	int port;
	int counter;
	
	public ClientUnsubscribe(SSLSocket sslsocket, BufferedWriter bufferedwriter, String ip, int port, int counter){
		this.sslsocket = sslsocket;
		this.bufferedwriter = bufferedwriter;
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
				unsubscribeCommand.put("id", ip+":"+port+":"+counter++);
				
				bufferedwriter.write(unsubscribeCommand.toJSONString()+ '\n');
				bufferedwriter.flush();
				
				//Send data to the server
				ClientSecureServices.closeSocket(sslsocket);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
