package EZShare.server.insecure.subscribe;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import EZShare.server.Resource;
import EZShare.server.ResourceList;
import EZShare.server.insecure.ServerServices;

public class InsecureSubscribeResourceThread extends Thread{
	
	private static final Logger logger = LogManager.getLogger(InsecureSubscribeResourceThread.class);
	private String name;
	private String description;
	private String uri;
	private String channel;
	private String owner;
	private String[] tagsArray;
	private ResourceList resList;
	private DataOutputStream output;
	private String id;
//	private SecureSubscribeNewResourceThread ssnt;
	private InsecureSubscribeNewResourceThread snt;

	
	public InsecureSubscribeResourceThread(String name, String description, String uri, String channel,
			String owner, String[] tagsArray, ResourceList resList, DataOutputStream output, String id){
		this.name = name;
		this.description = description;
		this.uri = uri;
		this.channel = channel;
		this.owner = owner;
		this.tagsArray = tagsArray;	
		this.resList = resList;
		this.output = output;
		this.id = id;

		this.snt = new InsecureSubscribeNewResourceThread(name, description, uri, channel,
				owner, tagsArray, output, id);
	}
	
    public void run(){
    	ServerServices.resList.setNewResourceNull();
    
		for (Resource r : ServerServices.resList.getResourceList()){
		    queryResource(r, name, description, uri, channel, owner, tagsArray);
		}
		
//		for (Resource r : resList.getResourceList()){
//			System.out.println("query the secure list:" + r.getUri());
//		    queryResource(r, name, description, uri, channel, owner, tagsArray);
//		}  
		
//		ssnt.start();
		snt.start();

    }
    
	private void queryResource(Resource r, String name, String description, String uri, String channel,
			String owner, String[] tagsArray) {

		boolean primaryKeyMatched = true;
		boolean nameDescriptionMatched = true;
		boolean tagMatched = true;
		boolean nameMatched = true;
		boolean descriptionMatched = true;

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
				try {
					
					JSONObject matchRes = new JSONObject();
					matchRes = resourceToJSON(r);
					//System.out.println("output the list resource");
					ServerServices.insecureSubscribeManager.get(this.id).addResouceSize();
					output.writeUTF(matchRes.toJSONString());
					output.flush();
					
					logger.info("SENT: " + matchRes.toJSONString());
				} catch (IOException e1) {
					System.out.println("subscribeSuc output wrong");
				}
			}
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
	
}
