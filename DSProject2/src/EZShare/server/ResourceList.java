package EZShare.server;
import java.util.ArrayList;

/**
 * store a list of resource
 * @author Ruoyi Wang, Luxin Weng, Qiulei Zhang, Huanan Li
 *
 */
public class ResourceList {
	//create a ArrayList to store resource
	private ArrayList<Resource> resources;
	private Resource newResource;
	/**
	 *  initialize the list of the resource
	 */
	public ResourceList(){
		resources = new ArrayList<Resource>();
	}
	
	/**
	 * return the length of the resource list
	 * @return the length of the resource list
	 */
	public synchronized int lengthResource(){
		return resources.size();
	}
	
	/**
	 * add a new resource into the arrayList of the resources
	 * @param re the new resource
	 * @throws InterruptedException 
	 */
	public synchronized void addResource(Resource re) throws InterruptedException{
		resources.add(re);
		this.newResource = re;
		notifyAll();
		
	}
	
	/**
	 * remove a existing resource in the arrayList
	 * @param re the resource that want to remove
	 */
	public synchronized void removeResource(Resource re){
		
		resources.remove(re);
	}
	
	/**
	 * return the whole ArrayList of the resource
	 * @return
	 */
	public synchronized ArrayList<Resource> getResourceList(){
		
		return resources;
		
	}
	
	public synchronized Resource notifyNewResource() throws InterruptedException{
		
		while (this.newResource == null) {
            wait();
        }

		return this.newResource;
		
	}
	
	public synchronized void setNewResourceNull(){
		this.newResource = null;
		notifyAll();
	}
}