package EZShare.server.secure.subscribe;

import org.json.simple.JSONObject;

public class SecureSubscribeMember {
    private String id;
    private boolean isOpen;
    private JSONObject subscribeCommand;
    private int resultSize;
    
    public SecureSubscribeMember(String id, boolean isOpen, JSONObject subscribeCommand, int resultSize){
    	   this.id = id;
    	   this.isOpen = isOpen;
    	   this.subscribeCommand = subscribeCommand;
    	   this.resultSize = resultSize;
    }
    
    public void addResouceSize(){
    	this.resultSize++;
    }
    
    public void closeSubscribe(){
    	this.isOpen = false;
    }
    
    public boolean getRelay(){
    	return (boolean) subscribeCommand.get("relay");
    }
    
    public int getResultSize(){
    	return this.resultSize;
    }
    
    public boolean getIsOpen(){
    	return this.isOpen;
    }
}
