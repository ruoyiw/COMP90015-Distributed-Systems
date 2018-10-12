package EZShare.server;

/**
 * store a resource information
 * @author Ruoyi Wang, Luxin Weng, Qiulei Zhang, Huanan Li
 */
public class Resource {

	// create variables
	private String name;
	private String description;
	private String[] tagsArray;
	private String uri;
	private String channel;
	private String owner;
	private String ezserver;
	
	/**
	 *  initialize the variables
	 * @param name optional user supplied name (String), default is "".
	 * @param description optional user supplied description (String), default is "".
	 * @param tagsArray optional user supplied list of tags (Array of Strings), default is empty list
	 * @param uri mandatory user supplied absolute URI, that is unique for each 
	 *         resource on a given EZShare Server within each Channel on the server (String)
	 * @param channel optional user supplied channel name (String), default is "".
	 * @param owner optional user supplied owner name (String), default is "".
	 * @param ezserver system supplied server:port name that lists the Resource (String).
	 */
	public Resource(String name, String description, String[] tagsArray, String uri, String channel, String owner,
			String ezserver) {
		this.name = name;
		this.description = description;
		this.tagsArray = tagsArray;
		this.uri = uri;
		this.channel = channel;
		this.owner = owner;
		this.ezserver = ezserver;
	}
	
	/**
	 *  get name
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 *  set name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 *  get description
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 *  set description
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 *  get tagsArray
	 * @return
	 */
	public String[] getTags() {
		return tagsArray;
	}
	
	/**
	 *  set tagsArray
	 * @param tagsArray
	 */
	public void setTags(String[] tagsArray) {
		this.tagsArray = tagsArray;
	}
	
	/**
	 *  get uri
	 * @return
	 */
	public String getUri() {
		return uri;
	}
	
	/**
	 *  set uri
	 * @param uri
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	/**
	 *  get channel
	 * @return
	 */
	public String getChannel() {
		return channel;
	}
	
	/**
	 *  set channel
	 * @param channel
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	/**
	 *  get owner
	 * @return
	 */
	public String getOwner() {
		return owner;
	}
	
	/**
	 *  set owner
	 * @param owner
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	/**
	 *  get ezserver
	 * @return
	 */
	public String getEzserver() {
		return ezserver;
	}
	
	/**
	 *  set ezserver
	 * @param ezserver
	 */
	public void setEzserver(String ezserver) {
		this.ezserver = ezserver;
	}

}
