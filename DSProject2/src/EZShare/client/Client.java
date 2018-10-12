package EZShare.client;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import javax.net.ssl.*;
import java.security.KeyStore;

/**
 * This class is the main class for clients which can communicate with the servers.
 * The main function of this class is to parsing command line options,
 * and based on the input arguments, choose which function to execute.
 *
 * @author Ruoyi Wang, Luxin Weng, Qiulei Zhang, Huanan Li
 */
public class Client {

    // A Logger object is used to log messages for a specific system or application component.
    private static final Logger logger = LogManager.getLogger(Client.class.getName());
    // identify IP and port
//	private static String ip = "sunrise.cis.unimelb.edu.au";
//	private static int port = 3780;
    private static String ip = "localhost";
    private static final int SPORT = 3781;
    private static final int UPORT = 3000;
    private static int port;

    public static void main(String[] args) throws Exception {

        // using options, parse the arguments into the command line
        CommandLine cmd = ParseCommandLineOptions(args);

        // if the argument uses a wrong type, the variable "cmd" is null
        if (cmd == null) {
            System.out.println("the type of the command is incorrect");
        }
        // it the type of the argument is correct, execute the corresponding function
        else {
            // control the project whether to show the communication info
            if (cmd.hasOption("debug")) {
                logger.info("setting debug on");
            }

            if (cmd.hasOption("secure")) {
                if (cmd.hasOption("port")) {
                    port = Integer.parseInt(cmd.getOptionValue("port"));
                } else {
                    port = SPORT;
                }
                //Location of the Java keystore file containing the collection of
                //certificates trusted by this application (trust store).
                InputStream keystoreInput = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("KeyStore/client.jks");
                InputStream truststoreInput = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("KeyStore/client.jks");
                //System.setProperty("javax.net.ssl.trustStore", "KeyStore/client.jks");
                //System.setProperty("javax.net.ssl.keyStorePassword","comp90015");
                //System.setProperty("javax.net.debug","all");
                ClientSecureServices.setSSLFactories(keystoreInput, "comp90015", truststoreInput);
                keystoreInput.close();
                truststoreInput.close();

                // judge which function to execute according to the command line
                if (cmd.hasOption("publish")) {
                    ClientSecureServices.publishCommand(cmd, ip, port);
                } else if (cmd.hasOption("remove")) {
                    ClientSecureServices.removeCommand(cmd, ip, port);
                } else if (cmd.hasOption("share")) {
                    ClientSecureServices.shareCommand(cmd, ip, port);
                } else if (cmd.hasOption("fetch")) {
                    ClientSecureServices.fetchCommand(cmd, ip, port);
                } else if (cmd.hasOption("query")) {
                    ClientSecureServices.queryCommand(cmd, ip, port);
                } else if (cmd.hasOption("exchange")) {
                    ClientSecureServices.exchangeCommand(cmd, ip, port);
                } else if (cmd.hasOption("subscribe")) {
                    ClientSecureServices.subscribeCommand(cmd, ip, port);
                } else {
                    System.out.println("Please input a valid command");
                }

            } else {
                System.out.println("1");
                if (cmd.hasOption("port")) {
                    port = Integer.parseInt(cmd.getOptionValue("port"));
                } else {
                    port = UPORT;
                }

                // judge which function to execute according to the command line
                if (cmd.hasOption("publish")) {
                    System.out.println("2");
                    ClientUnsecureServices.publishCommand(cmd, ip, port);
                } else if (cmd.hasOption("remove")) {
                    ClientUnsecureServices.removeCommand(cmd, ip, port);
                } else if (cmd.hasOption("share")) {
                    ClientUnsecureServices.shareCommand(cmd, ip, port);
                } else if (cmd.hasOption("fetch")) {
                    ClientUnsecureServices.fetchCommand(cmd, ip, port);
                } else if (cmd.hasOption("query")) {
                    ClientUnsecureServices.queryCommand(cmd, ip, port);
                } else if (cmd.hasOption("exchange")) {
                    ClientUnsecureServices.exchangeCommand(cmd, ip, port);
                } else if (cmd.hasOption("subscribe")) {
                    ClientUnsecureServices.subscribeCommand(cmd, ip, port);
                } else {
                    System.out.println("Please input a valid command");
                }
            }

        }

    }

    /**
     * using options, parse the arguments into the command line
     *
     * @param args the input arguments when executing the client.class
     * @return the command line
     */
    public static CommandLine ParseCommandLineOptions(String[] args) {
        Options options = new Options();

        //The client must work exactly with the following command line options
        options.addOption("channel", true, "select a channel");
        options.addOption("debug", false, "debug modes on/off");
        options.addOption("description", true, "resource description");
        options.addOption("exchange", false, "exchange server list with server");
        options.addOption("fetch", false, "fetch reource from server");
        options.addOption("host", true, "server name, a domain name or IP address");
        options.addOption("name", true, "resource name");
        options.addOption("owner", true, "resource owner");
        options.addOption("port", true, "server port, an integer");
        options.addOption("publish", false, "select a function publish");
        options.addOption("query", false, "query for resources from server");
        options.addOption("remove", false, "remove resource from server");
        options.addOption("secret", true, "secret for server");
        options.addOption("servers", true, "server list,host1:port1,host2:port2,....");
        options.addOption("share", false, "share resource on server");
        options.addOption("tags", true, "resources tags,tag1,tag2,tag3");
        options.addOption("uri", true, "resources URI");
        options.addOption("secure", false, "secure");
        options.addOption("subscribe", false, "subscribe for resources from server");

        // parse the command line options
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            return cmd;
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
        }
        // if there is a parseException, return null
        return null;
    }


}
