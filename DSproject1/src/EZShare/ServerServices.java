package EZShare;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The ServerServices class contains functions to proceed different command
 * parsed from clients respectively, then return the respond to each client
 */
public class ServerServices {
	// Logger is to log the information that needs to be displayed
	private static final Logger logger = LogManager.getLogger(ServerServices.class);
	// Resource list is to store all the published resources
	private ResourceList resList = new ResourceList();
	// Share list is to store all the shared resources
	private ResourceList shareList = new ResourceList();
	// Server record list is to store the server list connected to this server
	private static ArrayList<String> serverRecordList = new ArrayList<String>();

	/**
	 * 
	 * @param command
	 * @param server_control
	 * @param serverIO
	 * @return Result of publish operation Publish method is to create a new
	 *         resource and make it available
	 */
	public synchronized JSONObject publish(JSONObject command, ServerControl server_control, ServerIO serverIO) {

		JSONObject result = new JSONObject();
		String ezserver = null;
		String[] tagArray = null;
		// The resource must be present
		if (command.containsKey("resource")) {
			JSONObject resource = (JSONObject) command.get("resource");
			// All the attributes of resource must be present in the command
			// parsed from client
			if (resource.containsKey("name") && resource.containsKey("description") && resource.containsKey("uri")
					&& resource.containsKey("channel") && resource.containsKey("owner")
					&& resource.containsKey("ezserver") && resource.containsKey("tags")) {
				String name = resource.get("name").toString();
				String description = resource.get("description").toString();
				String uri = resource.get("uri").toString();
				String channel = resource.get("channel").toString();
				String owner = resource.get("owner").toString();
				if (resource.get("ezserver") != null) {
					ezserver = resource.get("ezserver").toString();
				} else {
					// Create a new string that combine the hostname and port of
					// server
					ezserver = server_control.getAdvertisedhostname() + ":" + serverIO.getPort();
				}
				// Transfer the JSONArray of tags to the array of tag strings
				JSONArray tags = (JSONArray) resource.get("tags");
				if (tags != null) {
					tagArray = new String[tags.size()];
					for (int i = 0; i < tags.size(); i++) {
						tagArray[i] = tags.get(i).toString();
					}
				}
				// String values must not contain the "\0" character, nor start
				// or end with whitespace.
				// The server may consider the resource invalid if such things
				// are found and the Owner field must not be the single
				// character "*".
				if (!checkResource(name, description, uri, channel, owner, ezserver, tagArray) || !checkOwner(owner)) {
					result.put("response", "error");
					result.put("errorMessage", "cannot publish resource");
					return result;
				}

				// The URI must be present, must be absolute and cannot be a
				// file scheme
				if (!uri.equals("") && checkURI(uri) && !isFile(uri)) {
					if (resList.lengthResource() > 0) {
						for (Resource res : resList.getResourceList()) {
							// Publishing a resource with the same channel and
							// URI
							if (res.getUri().equals(uri) && res.getChannel().equals(channel)) {
								// if the owner is default owner, anyone can
								// publish resources
								// or the same primary key as an existing
								// resource simply overwrites the
								// existing resource
								if (res.getOwner().equals("") || res.getOwner().equals(owner)) {
									overwriteResource(res, name, description, tagArray, ezserver);
									result.put("response", "success");
									return result;
								} else if (!res.getOwner().equals(owner)) {
									// But different owner is not allowed
									result.put("response", "error");
									result.put("errorMessage", "cannot publish resource");
									return result;
								}
							}
						}
					}
					// Create a resource with the parsed attributes and store it
					// in the resourse list
					Resource re = new Resource(name, description, tagArray, uri, channel, owner, ezserver);
					resList.addResource(re);
					result.put("response", "success");
				} else {
					// If the resource contained incorrect information that
					// could not be recovered from,
					// the error will occur
					result.put("response", "error");
					result.put("errorMessage", "invalid resource");
				}

			} else {
				// If any attribute of resourse is not present, the error will
				// occur
				result.put("response", "error");
				result.put("errorMessage", "missing resource");
			}

		} else {
			// If the resource is not present, the error will error
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
		}
		return result;
	}

	/**
	 * 
	 * @param command
	 * @param server_control
	 * @param serverIO
	 * @return The result of share operation
	 */
	public synchronized JSONObject share(JSONObject command, ServerControl server_control, ServerIO serverIO) {

		JSONObject result = new JSONObject();
		String ezserver = null;
		String[] tagArray = null;

		// The resource and server secret must be present
		if (command.containsKey("resource") && command.containsKey("secret")) {

			String secret = command.get("secret").toString();
			// The server secret must equal the value known to the server
			if (!secret.equals(server_control.getSecret())) {
				result.put("response", "error");
				result.put("errorMessage", "incorrect secret");
				return result;
			}

			JSONObject resource = (JSONObject) command.get("resource");

			// All the attributes of resource must be present in the command
			// parsed from client
			if (resource.containsKey("name") && resource.containsKey("description") && resource.containsKey("uri")
					&& resource.containsKey("channel") && resource.containsKey("owner")
					&& resource.containsKey("ezserver") && resource.containsKey("tags")) {
				String name = resource.get("name").toString();
				String description = resource.get("description").toString();
				String uri = resource.get("uri").toString();
				String channel = resource.get("channel").toString();
				String owner = resource.get("owner").toString();
				if (resource.get("ezserver") != null) {
					ezserver = resource.get("ezserver").toString();
				} else {
					// Create a new string that combine the hostname and port of
					// server
					ezserver = server_control.getAdvertisedhostname() + ":" + serverIO.getPort();
				}
				// Transfer the JSONArray of tags to the array of tag strings
				JSONArray tags = (JSONArray) resource.get("tags");
				if (tags != null) {
					tagArray = new String[tags.size()];
					for (int i = 0; i < tags.size(); i++) {
						tagArray[i] = tags.get(i).toString();
					}
				}
				// String values must not contain the "\0" character, nor start
				// or end with whitespace.
				// The server may consider the resource invalid if such things
				// are found
				// and the Owner field must not be the single character "*".
				if (!checkResource(name, description, uri, channel, owner, ezserver, tagArray) || !checkOwner(owner)) {
					result.put("response", "error");
					result.put("errorMessage", "cannot share resource");
					return result;
				}

				// The URI must be present, must be absolute, non-authoritative
				// and must be a file scheme
				if (!uri.equals("") && checkURI(uri) && isFile(uri)) {
					// It must point to a file on the local file system
					// that the server can read as a file
					URI uri1 = null;
					try {
						uri1 = new URI(uri);
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					File f = new File(uri1.getPath());
					// The file must exist and can be read
					if (!(f.exists() && f.canRead())) {
						result.put("response", "error");
						result.put("errorMessage", "invalid resource");
						return result;
					} else {
						for (Resource res : shareList.getResourceList()) {
							// Publishing a resource with the same channel and
							// URI
							if (res.getUri().equals(uri) && res.getChannel().equals(channel)) {
								// If the owner is default owner, anyone can
								// publish resources
								// or the same primary key as an existing
								// resource simply overwrites the
								// existing resource
								if (res.getOwner().equals(owner)) {
									overwriteResource(res, name, description, tagArray, ezserver);
									result.put("response", "success");
									return result;
								} else {
									// But different owner is not allowed
									result.put("response", "error");
									result.put("errorMessage", "cannot share resource");
									return result;
								}
							}
						}

						// Create a resource with the parsed attributes
						// and store it in the resourse list and share list
						Resource re = new Resource(name, description, tagArray, uri, channel, owner, ezserver);
						shareList.addResource(re);
						resList.addResource(re);
						result.put("response", "success");
					}
				} else {
					// If the resource contained incorrect information that
					// could not be recovered from,
					// the error will occur
					result.put("response", "error");
					result.put("errorMessage", "invalid resource");
				}

			} else {
				// If the share rules are broken, the error will occur
				result.put("response", "error");
				result.put("errorMessage", "cannot share resource");
			}

		} else {
			// If the resource or secret field was not given or not of the
			// correct type,
			// the error will occur
			result.put("response", "error");
			result.put("errorMessage", "missing resource or secret");
		}
		return result;
	}

	/**
	 * @param command
	 * @return Result of remove operation Remove method is to remove an existing
	 *         resource from the resource list
	 */
	public synchronized JSONObject remove(JSONObject command) {
		JSONObject result = new JSONObject();

		if (command.containsKey("resource")) {
			// Get the resource content parsed from the client
			JSONObject resource = (JSONObject) command.get("resource");

			if (resource.containsKey("uri")) {
				String uri = resource.get("uri").toString();
				// Remove the resource from the resource list if the uri is not
				// null and is absolute
				if (uri != null && checkURI(uri)) {
					for (Resource res : resList.getResourceList()) {
						if (res.getUri().equals(uri)) {
							resList.removeResource(res);
							result.put("response", "success");
							return result;
						}
					}
					// If the uri provided in the command is not equal to the
					// uri of all resources
					// in the list, the error will occur
					result.put("response", "error");
					result.put("errorMessage", "cannot remove resource");

				} else {
					// If the resource contained incorrect information that
					// could not be recovered from, the error will occur
					result.put("response", "error");
					result.put("errorMessage", "invalid resource");
				}

			} else {
				// If the uri option is not contained, the error will occur
				result.put("response", "error");
				result.put("errorMessage", "missing resource");
			}

		} else {
			// If the resource is not contained, the error will occur
			result.put("response", "error");
			result.put("errorMessage", "missing resource");
		}
		return result;
	}

	/**
	 * @param command
	 * @return Result of remove operation Remove method is to The purpose of the 
	 * query is to match the template against existing resources.
	 */
	public synchronized JSONObject query(JSONObject command, DataOutputStream output) {

		JSONObject queryResult = new JSONObject();
		int resultSize = 0;
		ArrayList<JSONObject> serverResourceResult = new ArrayList<JSONObject>();

		boolean relay = (boolean) command.get("relay");

		//judge the format of the JSON command from the client
		if (command.containsKey("resourceTemplate")) {
            
			//before the query start, told the client the query has been read
			JSONObject querySuc = new JSONObject();
			querySuc.put("response", "success");

			try {
				output.writeUTF(querySuc.toJSONString());
				output.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			int resourceNum = 0;
			JSONObject resourceTemplate = (JSONObject) command.get("resourceTemplate");

			String name = (String) resourceTemplate.get("name");
			String description = (String) resourceTemplate.get("description");
			String uri = (String) resourceTemplate.get("uri");
			String channel = (String) resourceTemplate.get("channel");
			String owner = (String) resourceTemplate.get("owner");
			JSONArray tags = (JSONArray) resourceTemplate.get("tags");

			//create an array to store the tags
			String[] tagArray = null;
			if (tags != null) {
				tagArray = new String[tags.size()];
				for (int i = 0; i < tags.size(); i++) {
					tagArray[i] = tags.get(i).toString();
				}
			}

			//compare the query with all the resource in the server and put the
			//matched resource into the list: resourceResult
			ArrayList<Resource> resourceResult = queryResources(name, description, uri, channel, owner, tagArray);
			resourceNum = resourceResult.size();

			//change all resources to the JSON format and store them in a list
			for (Resource re : resourceResult) {
				JSONObject queryRes = new JSONObject();

				queryRes = resourceToJSON(re);
				serverResourceResult.add(queryRes);
			}

		} else {
			queryResult.put("response", "error");
			queryResult.put("errorMessage", "missing resourceTemplate");
			return queryResult;
		}

		//if relay is true, the server should send the command to other servers
		if (relay) {
			JSONObject serverQueryCommand = (JSONObject) command;
			JSONObject serverResourceTemplate = (JSONObject) serverQueryCommand.get("resourceTemplate");
            
			//change the JSON command to hide some information
			serverQueryCommand.replace("relay", false);
			serverResourceTemplate.replace("owner", "");
			serverResourceTemplate.replace("channel", "");
			serverQueryCommand.replace("resourceTemplate", serverResourceTemplate);

			// ArrayList<JSONObject> serverResourceResult = new
			// ArrayList<JSONObject>();

			//connect with all the servers in the server list
			for (String se : serverRecordList) {
				String[] hostPort = se.split(":");
				String ip = hostPort[0];
				String portStr = hostPort[1];
				int port = Integer.parseInt(portStr);

				try (Socket socket = new Socket(ip, port);) {
					JSONParser parser = new JSONParser();
					logger.info("querying to " + ip + ":" + port);
					// Output and Input Stream
					DataInputStream serverInput = new DataInputStream(socket.getInputStream());
					DataOutputStream serverOutput = new DataOutputStream(socket.getOutputStream());

					//output the query command to the server
					output.writeUTF(serverQueryCommand.toJSONString());
					output.flush();
					logger.info("SENT: " + serverQueryCommand.toJSONString());

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

								if(!serverResourceResult.contains(serverCommand)){
									serverResourceResult.add(serverCommand);
								}
								
							}

						}
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {

				}
			}
		}

		JSONObject result = new JSONObject();

		//send the JSON command to the client which contains the resource information
		for (JSONObject js : serverResourceResult) {
			try {
				
				if(!js.get("owner").toString().isEmpty()){
					js.replace("channel", "*");
				}
				
				output.writeUTF(js.toJSONString());
				output.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// send to the client the number of resources
		resultSize = serverResourceResult.size();
		result.put("resultSize", resultSize);

		return result;
	}

	/**
	 * 
	 * @param name
	 * @param description
	 * @param uri
	 * @param channel
	 * @param owner
	 * @param ezshare
	 * @param tags
	 * @return ArrayList<Resource> the server contains the matched resources
	 */
	private ArrayList<Resource> queryResources(String name, String description, String uri, String channel,
			String owner, String[] tagsArray) {

		ArrayList<Resource> resourceResult = new ArrayList<Resource>();

		boolean primaryKeyMatched = true;
		boolean nameDescriptionMatched = true;
		boolean tagMatched = true;
		boolean nameMatched = true;
		boolean descriptionMatched = true;

		for (Resource r : resList.getResourceList()) {

			primaryKeyMatched = true;
			nameDescriptionMatched = true;
			tagMatched = true;
			nameMatched = true;
			descriptionMatched = true;
			
            /* The purpose of the query is to match the template against existing
             * resources. The template will match a candidate resource if:
             * (The template channel equals (case sensitive) the resource channel 
             * AND If the template contains an owner that is not "", then the 
             * candidate owner must equal it (case sensitive) AND Any tags present 
             * in the template also are present in the candidate (case insensitive) 
             * AND If the template contains a URI then the candidate URI matches 
             * (case sensitive) AND (The candidate name contains the template name 
             * as a substring (for non "" template name) OR The candidate description
             * contains the template description as a substring (for non "" template 
             * descriptions) OR The template description and name are both ""))*/
			
			//if channel does not match, the resource is not match
			if (!(channel.isEmpty())) {		
				if (!channel.equals(r.getChannel())) {
					primaryKeyMatched = false;
				}
			}else if(!(r.getChannel().isEmpty())){
				primaryKeyMatched = false;
			}
			// System.out.println("channel3 matched is "+matched);
			
			//if owner does not match, the resource is not match
			if (!owner.isEmpty()) {
				if (!owner.equals(r.getOwner())) {
					primaryKeyMatched = false;
				}
			}else if(!(r.getOwner().isEmpty())){
				primaryKeyMatched = false;
			}
			// System.out.println("owner matched is "+matched);
			
			//if uri does not match, the resource is not match
			if (!uri.isEmpty()) {
				if (!uri.equals(r.getUri())) {
					primaryKeyMatched = false;
				}
			}
			// System.out.println("uri matched is "+matched);

			//if name does not match, the resource is not match
			if (!name.isEmpty()) {
				// System.out.println("Try name");
				if (!(r.getName().contains(name) || r.getName().equals(name))) {
					nameMatched = false;
					// System.out.println("Name different");
				}
			}
			// System.out.println("name matched is "+matched);
			//if description does not match, the resource is not match
			if (!description.isEmpty()) {
				if (!(r.getDescription().contains(description) || r.getDescription().equals(description))) {
					descriptionMatched = false;
				}
			}
			
			nameDescriptionMatched = (nameMatched||descriptionMatched);
			// System.out.println("description matched is "+matched);

			// boolean a = (List)tagsArray.containsAll((List)r.getTags());
			List<String> tagTemplate = new ArrayList<String>();
			Collections.addAll(tagTemplate, tagsArray);
			List<String> tagResource = new ArrayList<String>();
			Collections.addAll(tagResource, r.getTags());

			tagMatched = tagResource.containsAll(tagTemplate);

			// System.out.println("matched is "+matched+" and tagMatched is "+
			// tagMatched);
			if (primaryKeyMatched && tagMatched &&nameDescriptionMatched) {
				resourceResult.add(r);
			}
		}
		return resourceResult;
	}

	/**
	 * @param command
	 * @param output
	 * @return Result of fetch operation
	 */
	public synchronized JSONObject fetch(JSONObject command, DataOutputStream output) {
		JSONObject result = new JSONObject();
		JSONObject fetchResult = new JSONObject();
		// The resource template must be present
		if (command.containsKey("resourceTemplate")) {
			JSONObject resource = (JSONObject) command.get("resourceTemplate");
			// The URI and channel must be present
			if (resource.containsKey("uri") && resource.containsKey("channel")) {

				String uri = resource.get("uri").toString();
				String channel = resource.get("channel").toString();
				String owner = (String) resource.get("owner");

				// String values must not contain the "\0" character, nor start
				// or end with whitespace.
				// The server may consider the resource invalid if such things
				// are found
				// and the Owner field must not be the single character "*".
				if (!(checkString(uri) && checkString(channel)) || !checkOwner(owner)) {
					result.put("response", "error");
					result.put("errorMessage", "cannot fetch resource");
					return result;
				}

				// The URI must be present, must be absolute and cannot be a
				// file scheme
				if (isFile(uri)) {
					URI uriFile = null;
					try {
						uriFile = new URI(uri);
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					File f = new File(uriFile.getPath());
					if (f.exists() && f.canRead()) {
						// System.out.println("the size of the share list is " + shareList.lengthResource());
						for (Resource res : shareList.getResourceList()) {
							// Fetching a resource with the same channel and URI
							if (res.getUri().equals(uri) && res.getChannel().equals(channel)) {
								fetchResult.put("response", "success");

								try {
									output.writeUTF(fetchResult.toJSONString());
									output.flush();
								} catch (IOException e1) {
									e1.printStackTrace();
								}

								JSONObject fetchResource = new JSONObject();
								fetchResource = resourceToJSON(res);
								fetchResource.put("resourceSize", f.length());
								try {
									output.writeUTF(fetchResource.toJSONString());
									output.flush();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								try {
									// Start sending file
									RandomAccessFile byteFile = new RandomAccessFile(f, "r");
									byte[] sendingBuffer = new byte[1024 * 1024];
									int num;
									// While there are still bytes to send..
									while ((num = byteFile.read(sendingBuffer)) > 0) {
										System.out.println(num);
										output.write(Arrays.copyOf(sendingBuffer, num));
									}
									byteFile.close();

								} catch (IOException e) {
									e.printStackTrace();
								}
								// Download one result successfully
								result.put("resultSize", 1);
								return result;
							}
						}
						fetchResult.put("response", "success");
						try {
							output.writeUTF(fetchResult.toJSONString());
							output.flush();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						result.put("resultSize", 0);
					} else {
						// If the resource does not exist or cannot be read,
						// the error will occur
						result.put("response", "error");
						result.put("errorMessage", "invalid resource");
					}
				} else {
					// If the resource is not file scheme, the error will occur
					result.put("response", "error");
					result.put("errorMessage", "invalid resource");
				}
			} else {
				// If the resource is not of the correct type,
				// the error will occur
				result.put("response", "error");
				result.put("errorMessage", "missing resourceTemplate");
			}
		} else {
			// If the resource template was not given,
			// the error will occur
			result.put("response", "error");
			result.put("errorMessage", "missing resourceTemplate");
		}

		return result;

	}
	
	/**
	 * @param command
	 * @return Result of exchange operation
	 */
	public synchronized JSONObject exchange(JSONObject command) {
		JSONObject result = new JSONObject();
        
		//check the exchange format
		if (command.containsKey("serverList")) {
			JSONArray serverList = (JSONArray) command.get("serverList");

			int serverListNum = serverList.size();
            
			//analyse the JSON command and find the server information
			for (int i = 0; i < serverListNum; i++) {
				JSONObject serverRecord = (JSONObject) serverList.get(i);
				String hostname = null;
				String portStr = null;
				int port = 0;

				if (serverRecord.containsKey("hostname")) {
					hostname = serverRecord.get("hostname").toString();
				} else {
					result.put("response", "error");
					result.put("errorMessage", "invalid server record");
					return result;
				}

				if (serverRecord.containsKey("port")) {
					portStr = serverRecord.get("port").toString();
					port = Integer.parseInt(portStr);
				} else {
					result.put("response", "error");
					result.put("errorMessage", "invalid server record");
					return result;
				}

				//System.out.println(hostname + ":" + portStr);
				
				//connect with the server, if connected, add the server to 
				//the server llist.
				try (Socket socket = new Socket(hostname, port);) {
					// System.out.println("Connect!!!!!!!!!");
					String newServer = hostname + ":" + portStr;
					if(!serverRecordList.contains(newServer)){
					serverRecordList.add(newServer);
					}

				} catch (UnknownHostException e) {
					result.put("response", "error");
					result.put("errorMessage", "missing server record");
					return result;
				} catch (IOException e1) {
					result.put("response", "error");
					result.put("errorMessage", "missing server record");
					return result;
				}
			}

			result.put("response", "success");

		} else {
			result.put("response", "error");
			result.put("errorMessage", "missing or invalid server list");
		}
		return result;
	}

	private JSONObject resourceToJSON(Resource res) {
		JSONObject resJSON = new JSONObject();

		resJSON.put("name", res.getName());

		JSONArray tagsJSON = new JSONArray();

		String[] tagsArray = res.getTags();

		for (int i = 0; i < tagsArray.length; i++) {
			tagsJSON.add(tagsArray[i]);
		}

		resJSON.put("tags", tagsJSON);

		resJSON.put("description", res.getDescription());
		resJSON.put("uri", res.getUri());
		resJSON.put("channel", res.getChannel());
		if (res.getOwner() == "" || res.getOwner() == null) {
			resJSON.put("owner", "");
		} else {
			resJSON.put("owner", "*");
		}
		resJSON.put("ezserver", res.getEzserver());

		return resJSON;
	}

	/**
	 * @return serverRecordList
	 */
	public static ArrayList<String> getServerRecordList() {
		return serverRecordList;
	}

	/**
	 * @param string
	 * @return boolean Check if the given string has correct format
	 */
	private boolean checkString(String string) {
		if (string != null && string.length() > 0) {
			if (string.contains("\0") || string.charAt(0) == ' ' || string.charAt(string.length() - 1) == ' ') {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param name
	 * @param description
	 * @param uri
	 * @param channel
	 * @param owner
	 * @param ezshare
	 * @param tags
	 * @return boolean Check if the given resource has correct format
	 */
	private boolean checkResource(String name, String description, String uri, String channel, String owner,
			String ezshare, String[] tags) {
		if (checkString(name) && checkString(description) && checkString(uri) && checkString(channel)
				&& checkString(owner) && checkString(ezshare)) {
			for (int i = 0; i < tags.length; i++) {
				if (!checkString(tags[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * @param owner
	 * @return boolean 
	 * Check if the given owner has correct format
	 */
	private boolean checkOwner(String owner) {

		if (owner.equals("*") || owner.equals(".classpath")) {

			return false;
		}
		return true;
	}

	/**
	 * @param uri_string
	 * @return boolean
	 * Check if the given URI has correct format
	 */
	private boolean checkURI(String uri_string) {
		try {
			URI uri = new URI(uri_string);
			if (uri.isAbsolute()) {
				uri.parseServerAuthority();
			}

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
 /**
  * @param uri_string
  * @return boolean
  * Check if the given URI is file scheme
  */
	private boolean isFile(String uri_string) {
		try {
			URI uri = new URI(uri_string);
			if (uri.getScheme().equals("file")) {
				return true;
			} else {
				return false;
			}

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @param res
	 * @param name
	 * @param description
	 * @param tagsArray
	 * @param ezserver
	 * Overwrite an existing resource
	 */
	private void overwriteResource(Resource res, String name, String description, String[] tagsArray, String ezserver) {
		res.setName(name);
		res.setDescription(description);
		res.setTags(tagsArray);
		res.setEzserver(ezserver);
	}
}