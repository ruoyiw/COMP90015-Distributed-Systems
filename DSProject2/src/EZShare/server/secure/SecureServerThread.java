package EZShare.server.secure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import EZShare.server.ServerControl;
import EZShare.server.ServerIO;

public class SecureServerThread extends Thread {

    private static ServerIO serverIO = null;
    private static HashMap<InetAddress, Long> ipTime = null;
    private static ServerControl server_control = null;
    private static int counter = 0;
    private static ServerSecureServices serverSecureServices = new ServerSecureServices();
    private static final Logger logger = LogManager.getLogger(SecureServerThread.class);

    public SecureServerThread(ServerIO serverIO, HashMap<InetAddress, Long> ipTime,
                              ServerControl server_control, int counter) {
        this.serverIO = serverIO;
        this.ipTime = ipTime;
        this.server_control = server_control;
        this.counter = counter;
    }

    public void run() {
        //Specify the keystore details (this can be specified as VM arguments as well)
        //the keystore file contains an application's own certificate and private key
        //System.setProperty("javax.net.ssl.keyStore", "KeyStore/server.jks");
        //Password to access the private key from the keystore file
        //System.setProperty("javax.net.ssl.keyStorePassword", "comp90015");
        //System.setProperty("javax.net.ssl.trustStore", "KeyStore/server.jks");
        //System.setProperty("javax.net.debug","all");
        //InputStream keystoreInput = this.getClass().getResourceAsStream("/KeyStore/Server.jks");

        InputStream keystoreInput = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("KeyStore/server.jks");
        InputStream truststoreInput = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("KeyStore/server.jks");

        try {
            ServerSecureServices.setSSLFactories(keystoreInput, "comp90015", truststoreInput);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keystoreInput.close();
            truststoreInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SSLServerSocketFactory sslserversocketfactory =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try (SSLServerSocket sslServerSocket =
                     (SSLServerSocket) sslserversocketfactory.createServerSocket(serverIO.getSport())) {

            while (true) {
                // if there a client want to connect, accept the request of connection
                Date time = new Date();
                SSLSocket sslClientSocket = (SSLSocket) sslServerSocket.accept();
                ipTime = server_control.getIPTime();
                if (ipTime.containsKey(sslClientSocket.getInetAddress())) {
                    if ((time.getTime() - ipTime.get(sslClientSocket.getInetAddress())) >
                            server_control.geConnectionintervallimit() * 1000) {
                        server_control.resetIPTime(sslClientSocket.getInetAddress(), time.getTime());
                    } else {
                        sslClientSocket.close();
                        continue;
                    }
                } else {
                    server_control.addIPTime(sslClientSocket.getInetAddress(), time.getTime());
                    //System.out.println("CONNECT SECURE2");
                }


                logger.info("new connection from " + sslClientSocket.getInetAddress() + ":" + sslClientSocket.getPort());

                // Start a new thread for a connection

                Thread t = new Thread(() -> serveClient(sslClientSocket));
                t.start();

                //System.out.println("I am start");

            }


        } catch (Exception exception) {
            System.out.println("Secure connection problem!");
        }

    }

    private static void serveClient(SSLSocket client) {
        try (Socket clientSocket = client) {
            // The JSON Parser
            JSONParser parser = new JSONParser();
//			// Input stream
//			DataInputStream input = new DataInputStream(clientSocket.getInputStream());
//			// Output Stream
//			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
//			// System.out.println("CLIENT: " + input.readUTF());
//			// output.writeUTF("Server: Hi Client " + counter + " !!!");
//
//			// Receive more data..

            //Create buffered reader to read input from the client
            InputStream inputstream = clientSocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            //Create buffered writer to send data to the client
            OutputStream outputstream = clientSocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            String string = null;
            //Read input from the client and print it to the screen
            while (true) {
                if ((string = bufferedreader.readLine()) != null) {
                    // Attempt to convert read data to JSON
                    JSONObject command = (JSONObject) parser.parse(string);
                    logger.info("RECEIVED: " + command.toJSONString());

//					 //gain the output result from different function in terms of JSON
                    JSONObject result;

                    result = parseCommand(command, bufferedwriter);
                    if (!result.isEmpty()) {
                        logger.info("SENT: " + result.toJSONString());
//					
//					// send JSON message to the client
//					output.writeUTF(result.toJSONString());
                        bufferedwriter.write(result.toJSONString() + '\n');
                        bufferedwriter.flush();

                        logger.info("SENT: " + result.toJSONString());
                    }
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static JSONObject parseCommand(JSONObject command, BufferedWriter output) throws InterruptedException {

        JSONObject result = new JSONObject();

        // judge which function to execute according to receiving JSON message
        if (command.containsKey("command")) {
            if (command.get("command").equals("PUBLISH")) {
                result = serverSecureServices.publish(command, server_control, serverIO);
            } else if (command.get("command").equals("REMOVE")) {
                result = serverSecureServices.remove(command);
            } else if (command.get("command").equals("QUERY")) {
                result = serverSecureServices.query(command, output);
            } else if (command.get("command").equals("EXCHANGE")) {
                result = serverSecureServices.exchange(command);
            } else if (command.get("command").equals("SHARE")) {
                result = serverSecureServices.share(command, server_control, serverIO);
            } else if (command.get("command").equals("FETCH")) {
                result = serverSecureServices.fetch(command, output);
            } else if (command.get("command").equals("SUBSCRIBE")) {
                result = serverSecureServices.subscribe(command, output);
            } else if (command.get("command").equals("UNSUBSCRIBE")) {
                result = serverSecureServices.unsubscribe(command, output);
            } else {
                // the invalid command
                result.put("response", "error");
                result.put("errorMessage", "invalid command");
            }

        }
        return result;
    }
}
