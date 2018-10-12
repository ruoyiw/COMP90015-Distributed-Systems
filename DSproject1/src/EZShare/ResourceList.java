package EZShare;
import java.util.ArrayList;

/**
 * store a list of resource
 * @author Ruoyi Wang, Luxin Weng, Qiulei Zhang, Huanan Li
 *
 */
public class ResourceList {
	//create a ArrayList to store resource
	private ArrayList<Resource> resources;
	
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
	public int lengthResource(){
		return resources.size();
	}
	
	/**
	 * add a new resource into the arrayList of the resources
	 * @param re the new resource
	 */
	public void addResource(Resource re){
		
		resources.add(re);
		
	}
	
	/**
	 * remove a existing resource in the arrayList
	 * @param re the resource that want to remove
	 */
	public void removeResource(Resource re){
		
		resources.remove(re);
	}
	
	/**
	 * return the whole ArrayList of the resource
	 * @return
	 */
	public ArrayList<Resource> getResourceList(){
		
		return resources;
		
	}
	
}