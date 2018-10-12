package EZShare.server;
import java.util.ArrayList;

/**
 * store a list of resource
 * @author Ruoyi Wang, Luxin Weng, Qiulei Zhang, Huanan Li
 *
 */
public class ServerRecordList {
	//create a ArrayList to store resource
	private ArrayList<String> serverRecordList;
	private String newServerRecord;
	/**
	 *  initialize the list of the resource
	 */
	public ServerRecordList(){
		serverRecordList = new ArrayList<String>();
		newServerRecord = null;
	}
	
	/**
	 * return the length of the resource list
	 * @return the length of the resource list
	 */
	public synchronized int lengthResource(){
		return serverRecordList.size();
	}
	
	/**
	 * add a new resource into the arrayList of the resources
	 * @param re the new resource
	 * @throws InterruptedException 
	 */
	public synchronized void addServerRecord(String serverRecord) throws InterruptedException{
		serverRecordList.add(serverRecord);
		newServerRecord = serverRecord;
		notifyAll();
		
	}
	
	/**
	 * remove a existing resource in the arrayList
	 * @param re the resource that want to remove
	 */
	public synchronized void removeServerRecord(String serverRecord){
		
		serverRecordList.remove(serverRecord);
	}
	
	/**
	 * return the whole ArrayList of the resource
	 * @return
	 */
	public synchronized ArrayList<String> getServerRecordList(){
		
		return serverRecordList;
		
	}
	
	public synchronized String notifyNewServerRecord() throws InterruptedException{
		
		while (this.newServerRecord == null) {
            wait();
        }
		
		return this.newServerRecord;
	}
	
	public synchronized void setNewResourceNull(){
		this.newServerRecord = null;
		notifyAll();
	}
	
	
}