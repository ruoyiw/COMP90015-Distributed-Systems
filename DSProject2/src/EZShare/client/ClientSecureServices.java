package EZShare.client;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter.Result;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.Arrays;

import javax.net.ssl.*;

/**
 * the class mainly provides the specific function to execute
 * including publish, remove, share, fetch, query, exchange
 * <p>
 * Created by Apple on 20-Apr-17.
 *
 * @author Ruoyi Wang, Luxin Weng, Qiulei Zhang, Huanan Li
 */
public class ClientSecureServices {

    // A Logger object is used to log messages for a specific system or application component.
    private static Logger logger = LogManager.getLogger(ClientSecureServices.class);
    private static int counter = 0;

    /**
     * create a new Resource in the server and make it available
     *
     * @param cmd  the command line
     * @param ip   the ip address of the server
     * @param port the port number of the server
     */
    public static void publishCommand(CommandLine cmd, String ip, int port) {
        try {

            SSLSocket sslsocket;
            //Create SSL socket and connect it to the remote server
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            sslsocket = (SSLSocket) sslsocketfactory.createSocket(ip, port);

            //Create buffered writer to send data to the server
            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            //Create buffered reader to read input from the console
            InputStream inputstream = sslsocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            // JSON Message
            JSONObject publishCommand = new JSONObject();
            JSONParser parser = new JSONParser();
            publishCommand.put("command", "PUBLISH");
            publishCommand.put("resource", generateResourceJSON(cmd));

            // send message to server
            bufferedwriter.write(publishCommand.toJSONString() + '\n');
            bufferedwriter.flush();

            // set debug sending info
            if (cmd.hasOption("debug")) {
                logger.info("publishing to " + ip + ":" + port);
                logger.info("SENT: " + publishCommand.toJSONString());
            }

            String string = null;
            //Read line from the console
            JSONObject result = null;

            if ((string = bufferedreader.readLine()) != null) {
                // receive the JSON message from the server
                result = (JSONObject) parser.parse(string);
                // set debug receiving info
                if (cmd.hasOption("debug")) {
                    logger.info("RECEIVED: " + result.toJSONString());
                }

                // print out the result message
                if (result.containsValue("success")) {
                    System.out.println("publish succeeded");
                    generateOutput(generateResourceJSON(cmd));
                } else {
                    System.out.println("publish failed");
                    System.out.println("error message: " + result.get("errorMessage"));
                }
            }

            sslsocket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * remove an existing Resource, including file and other uri
     *
     * @param cmd  the command line
     * @param ip   the ip address of the server
     * @param port the port number of the server
     */
    public static void removeCommand(CommandLine cmd, String ip, int port) {
        try {

            SSLSocket sslsocket;
            //Create SSL socket and connect it to the remote server
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            sslsocket = (SSLSocket) sslsocketfactory.createSocket(ip, port);

            //Create buffered writer to send data to the server
            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            //Create buffered reader to read input from the console
            InputStream inputstream = sslsocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            // JSON Message
            JSONObject removeCommand = new JSONObject();
            JSONParser parser = new JSONParser();
            removeCommand.put("command", "REMOVE");
            removeCommand.put("resource", generateResourceJSON(cmd));
//			// send message to server			
            bufferedwriter.write(removeCommand.toJSONString() + '\n');
            bufferedwriter.flush();

            // set debug sending info
            if (cmd.hasOption("debug")) {
                logger.info("removing from " + ip + ":" + port);
                logger.info("SENT: " + removeCommand.toJSONString());
            }

            String string = null;
            //Read line from the console
            JSONObject result = null;

            if ((string = bufferedreader.readLine()) != null) {
                // receive the JSON message from the server
                result = (JSONObject) parser.parse(string);
                // set debug receiving info
                if (cmd.hasOption("debug")) {
                    logger.info("RECEIVED: " + result.toJSONString());
                }

                // print out the result message
                if (result.containsValue("success")) {
                    System.out.println("remove succeeded");
                    generateOutput(generateResourceJSON(cmd));
                } else {
                    System.out.println("remove failed");
                    System.out.println("error message: " + result.get("errorMessage"));
                }
            }

            sslsocket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * create a new Resource with a file URI in the server and make it available
     *
     * @param cmd  the command line
     * @param ip   the ip address of the server
     * @param port the port number of the server
     */
    public static void shareCommand(CommandLine cmd, String ip, int port) {
        try {
            SSLSocket sslsocket;
            //Create SSL socket and connect it to the remote server
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            sslsocket = (SSLSocket) sslsocketfactory.createSocket(ip, port);

            //Create buffered writer to send data to the server
            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            //Create buffered reader to read input from the console
            InputStream inputstream = sslsocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            // JSON Message
            JSONObject shareCommand = new JSONObject();
            JSONParser parser = new JSONParser();
            shareCommand.put("command", "SHARE");
            if (cmd.hasOption("secret")) {
                shareCommand.put("secret", cmd.getOptionValue("secret"));
            } else {
                shareCommand.put("secret", "");
            }
            shareCommand.put("resource", generateResourceJSON(cmd));

            bufferedwriter.write(shareCommand.toJSONString() + '\n');
            bufferedwriter.flush();

            // set debug sending info
            if (cmd.hasOption("debug")) {
                logger.info("sharing to " + ip + ":" + port);
                logger.info("SENT: " + shareCommand.toJSONString());
            }

            String string = null;
            //Read line from the console
            JSONObject result = null;

            if ((string = bufferedreader.readLine()) != null) {
                // receive the JSON message from the server
                result = (JSONObject) parser.parse(string);
                // set debug receiving info
                if (cmd.hasOption("debug")) {
                    logger.info("RECEIVED: " + result.toJSONString());
                }

                // print out the result message
                if (result.containsValue("success")) {
                    System.out.println("share succeeded");
                    generateOutput(generateResourceJSON(cmd));
                } else {
                    System.out.println("share failed");
                    System.out.println("error message: " + result.get("errorMessage"));
                }

            }

            sslsocket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * download all resources that match a Resource template which includes a file URI
     *
     * @param cmd  the command line
     * @param ip   the ip address of the server
     * @param port the port number of the server
     */
    public static void fetchCommand(CommandLine cmd, String ip, int port) {
        try {
            SSLSocket sslsocket;
            //Create SSL socket and connect it to the remote server
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            sslsocket = (SSLSocket) sslsocketfactory.createSocket(ip, port);

            //Create buffered writer to send data to the server
            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            //Create buffered reader to read input from the console
            InputStream inputstream = sslsocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            // JSON Message
            JSONObject fetchCommand = new JSONObject();
            JSONParser parser = new JSONParser();
            fetchCommand.put("command", "FETCH");
            fetchCommand.put("resourceTemplate", generateResourceJSON(cmd));

            bufferedwriter.write(fetchCommand.toJSONString() + '\n');
            bufferedwriter.flush();

            // set debug sending info
            if (cmd.hasOption("debug")) {
                logger.info("fetch from " + ip + ":" + port);
                logger.info("SENT: " + fetchCommand.toJSONString());
            }

            String string = null;
            //Read line from the console
            JSONObject result = null;

            while (true) {
                if ((string = bufferedreader.readLine()) != null) {

                    // receive the JSON message from the server
                    result = (JSONObject) parser.parse(string);

                    // set debug receiving info
                    if (cmd.hasOption("debug")) {
                        logger.info("RECEIVED: " + result.toJSONString());
                    }

                    // print out the result message
                    if (result.containsKey("response")) {
                        if (result.containsValue("success")) {
                            System.out.println("fetch succeeded");
                        } else if (result.containsValue("error")) {
                            System.out.println("fetch failed");
                            System.out.println("error message: " + result.get("errorMessage"));
                        }
                    } else if (result.containsKey("resultSize")) {
                        System.out.println("hit " + result.get("resultSize") + " resource(s)");
                    } else {
                        generateOutput(result);
                    }

                    // Check the command name
                    if (result.containsKey("uri")) {
                        // The file location
                        MakeDirectory();

                        File f = new File(result.get("uri").toString());
                        String fileName = "client_files/" + f.getName();

                        // Create a RandomAccessFile to read and write the
                        // output file.
                        RandomAccessFile downloadingFile = new RandomAccessFile(fileName, "rw");

                        // Find out how much size is remaining to get from
                        // the server.
                        long fileSizeRemaining = (Long) result.get("resourceSize");

                        int chunkSize = setChunkSize(fileSizeRemaining);

                        // Represents the receiving buffer
                        char[] receiveBuffer_char = new char[chunkSize];
                        byte[] receiveBuffer = getBytes(receiveBuffer_char);

                        // Variable used to read if there are remaining size
                        // left to read.
                        int num;

                        // System.out.println("Downloading " + fileName + " of
                        // size
                        // " + fileSizeRemaining);
                        while ((num = bufferedreader.read(receiveBuffer_char)) > 0) {
                            // Write the received bytes into the
                            // RandomAccessFile
                            downloadingFile.write(Arrays.copyOf(receiveBuffer, num));

                            // Reduce the file size left to read..
                            fileSizeRemaining -= num;

                            // Set the chunkSize again
                            chunkSize = setChunkSize(fileSizeRemaining);
                            receiveBuffer = new byte[chunkSize];

                            // If you're done then break
                            if (fileSizeRemaining == 0) {
                                break;
                            }
                        }

                        // logger.info("RECEIVED: " + input.readUTF());
                        downloadingFile.close();

                    }
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * receive a list of EZShare host:port names
     *
     * @param cmd  the command line
     * @param ip   the ip address of the server
     * @param port the port number of the server
     */
    public static void exchangeCommand(CommandLine cmd, String ip, int port) {
        try {
            SSLSocket sslsocket;
            //Create SSL socket and connect it to the remote server
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            sslsocket = (SSLSocket) sslsocketfactory.createSocket(ip, port);

            //Create buffered writer to send data to the server
            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            //Create buffered reader to read input from the console
            InputStream inputstream = sslsocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            // JSON Message
            JSONObject exchangeCommand = new JSONObject();
            JSONParser parser = new JSONParser();
            exchangeCommand.put("command", "EXCHANGE");

            // separate the string of ip and port by ":"
            // and then transfer the string type of ip and port into the array type
            // finally store into JSONArray and generate a JSON message
            if (cmd.hasOption("servers")) {
                JSONArray serversJSON = new JSONArray();

                String servers = cmd.getOptionValue("servers");
                String[] serversArray = servers.split(",");

                for (int i = 0; i < serversArray.length; i++) {
                    JSONObject host = new JSONObject();
                    String[] hostnamePort = serversArray[i].split(":");
                    String hostname = hostnamePort[0];
                    String portVal = hostnamePort[1];
                    host.put("hostname", hostname);
                    host.put("port", portVal);
                    serversJSON.add(host);
                }
                exchangeCommand.put("serverList", serversJSON);
            } else {
                JSONArray serversJSON = new JSONArray();
                exchangeCommand.put("serverList", serversJSON);
            }

            // send message to server
            bufferedwriter.write(exchangeCommand.toJSONString() + '\n');
            bufferedwriter.flush();

            // set debug sending info
            if (cmd.hasOption("debug")) {
                logger.info("exchanging to " + ip + ":" + port);
                logger.info("SENT: " + exchangeCommand.toJSONString());
            }

            String string = null;
            //Read line from the console
            JSONObject result = null;

            if ((string = bufferedreader.readLine()) != null) {
                // receive the JSON message from the server
                result = (JSONObject) parser.parse(string);
                // set debug receiving info
                if (cmd.hasOption("debug")) {
                    logger.info("RECEIVED: " + result.toJSONString());
                }

                // print out the result message
                if (result.containsValue("success")) {
                    System.out.println("exchange succeeded");
                } else {
                    System.out.println("exchange failed");
                    System.out.println("error message: " + result.get("errorMessage"));
                }
            }

            sslsocket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * list all resources that match a Resource template
     *
     * @param cmd  the command line
     * @param ip   the ip address of the server
     * @param port the port number of the server
     */
    public static void queryCommand(CommandLine cmd, String ip, int port) {
        try {
            SSLSocket sslsocket;
            //Create SSL socket and connect it to the remote server
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            sslsocket = (SSLSocket) sslsocketfactory.createSocket(ip, port);

            //Create buffered writer to send data to the server
            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            //Create buffered reader to read input from the console
            InputStream inputstream = sslsocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            // JSON Message
            JSONObject queryCommand = new JSONObject();
            JSONParser parser = new JSONParser();
            queryCommand.put("command", "QUERY");
            queryCommand.put("relay", true);
            queryCommand.put("resourceTemplate", generateResourceJSON(cmd));

            bufferedwriter.write(queryCommand.toJSONString() + '\n');
            bufferedwriter.flush();

            // set debug sending info
            if (cmd.hasOption("debug")) {
                logger.info("querying to " + ip + ":" + port);

                logger.info("SENT: " + queryCommand.toJSONString());
            }

            String string = null;
            //Read line from the console
            JSONObject result = null;

            while (true) {
                if ((string = bufferedreader.readLine()) != null) {
                    // Attempt to convert read data to JSON
                    result = (JSONObject) parser.parse(string);

                    // set debug receiving info
                    if (cmd.hasOption("debug")) {
                        logger.info("RECEIVED: " + result.toJSONString());
                    }

                    // print out the result message
                    if (result.containsKey("response")) {
                        if (result.containsValue("success")) {
                            System.out.println("query succeeded");
                        } else if (result.containsValue("error")) {
                            System.out.println("query failed");
                            System.out.println("error message: " + result.get("errorMessage"));
                        }
                    } else if (result.containsKey("resultSize")) {
                        System.out.println("hit " + result.get("resultSize") + " resource(s)");
                        break;
                    } else {
                        generateOutput(result);
                    }

                }

            }

            sslsocket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {

        } catch (ParseException e) {

        }

    }

    /**
     * list all resources that match a Resource template
     *
     * @param cmd  the command line
     * @param ip   the ip address of the server
     * @param port the port number of the server
     */
    public static void subscribeCommand(CommandLine cmd, String ip, int port) {
        try {
            counter++;
            SSLSocket sslsocket;
            //Create SSL socket and connect it to the remote server
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            sslsocket = (SSLSocket) sslsocketfactory.createSocket(ip, port);

            //Create buffered writer to send data to the server
            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            //Create buffered reader to read input from the console
            InputStream inputstream = sslsocket.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            // JSON Message
            JSONObject subscribeCommand = new JSONObject();
            JSONParser parser = new JSONParser();
            subscribeCommand.put("command", "SUBSCRIBE");
            subscribeCommand.put("relay", true);
            subscribeCommand.put("id", ip + ":" + port + ":" + counter);
            subscribeCommand.put("resourceTemplate", generateResourceJSON(cmd));

            bufferedwriter.write(subscribeCommand.toJSONString() + '\n');
            bufferedwriter.flush();

            // set debug sending info
            if (cmd.hasOption("debug")) {
                logger.info("subscribing to " + ip + ":" + port);

                logger.info("SENT: " + subscribeCommand.toJSONString());
            }

            String string = null;
            //Read line from the console
            JSONObject result = null;

            while (true) {
                ClientSecureUnsubscribe unsubscribe = new ClientSecureUnsubscribe(sslsocket, bufferedwriter,
                        ip, port, counter);
                if ((string = bufferedreader.readLine()) != null) {

                    // Attempt to convert read data to JSON
                    result = (JSONObject) parser.parse(string);

                    // set debug receiving info
                    if (cmd.hasOption("debug")) {
                        logger.info("RECEIVED: " + result.toJSONString());
                    }

                    // print out the result message
                    if (result.containsKey("response")) {
                        unsubscribe.start();
                        
                        if (result.containsValue("success")) {
                            System.out.println("subscribe succeeded");
                        } else if (result.containsValue("error")) {
                            System.out.println("subscribe failed");
                            System.out.println("error message: " + result.get("errorMessage"));
                        }
                    } else if (result.containsKey("resultSize")) {
                        System.out.println("hit " + result.get("resultSize") + " resource(s)");
                        sslsocket.close();
                        break;
                    } else {
                        unsubscribe.start();
               
                        generateOutput(result);
                    }

                }

            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {

        } catch (ParseException e) {

        }

    }

    public static void closeSocket(SSLSocket sslsocket) {
        try {
            sslsocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * generate a JSON message of resource based on the command line
     *
     * @param cmd the command line
     * @return the resource in terms on JSON message
     */
    private static JSONObject generateResourceJSON(CommandLine cmd) {

        // create a JSONObject for recording resource
        JSONObject resource = new JSONObject();

        // optional user supplied name (String), default is "".
        if (cmd.hasOption("name")) {
            resource.put("name", cmd.getOptionValue("name"));
        } else {
            resource.put("name", "");
        }

        // optional user supplied list of tags (Array of Strings),
        // default is empty list
        if (cmd.hasOption("tags")) {
            JSONArray tagsJSON = new JSONArray();

            String tags = cmd.getOptionValue("tags");
            String[] tagsArray = tags.split(",");

            for (int i = 0; i < tagsArray.length; i++) {
                tagsJSON.add(tagsArray[i]);
            }
            resource.put("tags", tagsJSON);
        } else {
            JSONArray tagsJSON = new JSONArray();
            resource.put("tags", tagsJSON);
        }

        // optional user supplied description (String), default is "".
        if (cmd.hasOption("description")) {
            resource.put("description", cmd.getOptionValue("description"));
        } else {
            resource.put("description", "");
        }

        // mandatory user supplied absolute URI, that is unique
        // for each resource on a given EZShare
        // Server within each Channel on the server (String).
        if (cmd.hasOption("uri")) {
            resource.put("uri", cmd.getOptionValue("uri"));
        } else {
            resource.put("uri", "");
        }

        // optional user supplied channel name (String), default is ""
        if (cmd.hasOption("channel")) {
            resource.put("channel", cmd.getOptionValue("channel"));
        } else {
            resource.put("channel", "");
        }

        //optional user supplied owner name (String), default is "".
        if (cmd.hasOption("owner")) {
            resource.put("owner", cmd.getOptionValue("owner"));
        } else {
            resource.put("owner", "");
        }

        // system supplied server:port name that lists the Resource (String).
        if (cmd.hasOption("ezserver")) {
            resource.put("ezserver", cmd.getOptionValue("ezserver"));
        } else {
            resource.put("ezserver", null);
        }

        return resource;
    }

    /**
     * set the sending or receiving size
     * for avoid exceeding the maximum size of the buffer
     *
     * @param fileSizeRemaining the remaining size of the file
     * @return the size of chunk
     */
    private static int setChunkSize(long fileSizeRemaining) {
        // Determine the chunkSize
        int chunkSize = 1024 * 1024;

        // If the file size remaining is less than the chunk size
        // then set the chunk size to be equal to the file size.
        if (fileSizeRemaining < chunkSize) {
            chunkSize = (int) fileSizeRemaining;
        }

        return chunkSize;
    }

    /**
     * generate an print out type for a better readability
     *
     * @param json the sending or receiving JSON message
     */
    private static void generateOutput(JSONObject json) {

        // print the name, tags and description on one line
        if (json.containsKey("name")) {
            System.out.print("|" + json.get("name"));
        }

        // transfer the JSONArray type into array type
        if (json.containsKey("tags")) {
            JSONArray tags = (JSONArray) json.get("tags");
            String[] tagsArray = null;
            if (tags != null) {
                tagsArray = new String[tags.size()];
                for (int i = 0; i < tags.size(); i++) {
                    if (i == 0) {
                        System.out.printf(" [");
                    }
                    tagsArray[i] = tags.get(i).toString();
                    System.out.print(tagsArray[i]);
                    if (i < tagsArray.length - 1) {
                        System.out.print(" ");
                    } else {
                        System.out.print("]");
                    }
                }
            }
        }

        if (json.containsKey("description")) {
            System.out.println(" " + json.get("description"));
        }

        // print out uri
        if (json.containsKey("uri")) {
            System.out.println("|" + json.get("uri"));
        }

        // print out channel
        if (json.containsKey("channel")) {
            System.out.print("|" + "==");
            System.out.print(json.get("channel"));
            System.out.println("==");
        }

        // print out ezserver
        if (json.containsKey("ezserver")) {
            System.out.println("|ezserver: " + json.get("ezserver"));
        }
        System.out.println("");

    }

    /**
     * Set the buffer chuckSize for the transmitting files.
     *
     * @param fileSizeRemaining
     * @return
     */
    private static int setChunkSize(long fileSizeRemaining, String ip, int port) {
        // Determine the chunkSize
        int chunkSize = 1024 * 1024;

        // If the file size remaining is less than the chunk size
        // then set the chunk size to be equal to the file size.
        if (fileSizeRemaining < chunkSize) {
            chunkSize = (int) fileSizeRemaining;
        }

        return chunkSize;
    }

    /**
     * generate a directory to store the files that fetch from the server
     */
    private static void MakeDirectory() {
        File file = new File("client_files");
        // judge whether the directory exists.
        // if not, create one.
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created");
            } else {
                System.out.println("Not create directory");
            }
        }
    }

    private static byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);

        return bb.array();
    }


    public static void setSSLFactories(InputStream keyStream, String keyStorePassword,
                                        InputStream trustStream) throws Exception {
        // Get keyStore
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        // if your store is password protected then declare it (it can be null however)
        char[] keyPassword = keyStorePassword.toCharArray();

        // load the stream to your store
        keyStore.load(keyStream, keyPassword);

        // initialize a trust manager factory with the trusted store
        KeyManagerFactory keyFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyFactory.init(keyStore, keyPassword);

        // get the trust managers from the factory
        KeyManager[] keyManagers = keyFactory.getKeyManagers();

        // Now get trustStore
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

        // if your store is password protected then declare it (it can be null however)
        //char[] trustPassword = password.toCharArray();

        // load the stream to your store
        trustStore.load(trustStream, null);

        // initialize a trust manager factory with the trusted store
        TrustManagerFactory trustFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);

        // get the trust managers from the factory
        TrustManager[] trustManagers = trustFactory.getTrustManagers();

        // initialize an ssl context to use these managers and set as default
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(keyManagers, trustManagers, null);
        SSLContext.setDefault(sslContext);
    }

}
