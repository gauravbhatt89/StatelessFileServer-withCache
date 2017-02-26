package ServerProperties;

import java.io.Serializable;

public class ServerProperties implements Serializable {
    	public int portnum, offset, bytesRead;
    	public String hostName, fileName, operation;
    	public boolean isExist;
    	public byte[] data;
    	public boolean isEOF;
    	public long lastModifiedTime;
		
    	// Constructor
	   	public ServerProperties() {
	   		portnum = 0;
	   		offset = 0;
	   		bytesRead = -1;
	   		hostName = "";
	   		fileName = "";
	   		operation = "";
	   		isExist = false;
	   		data = null;
	   		isEOF=false;
	   		lastModifiedTime = 0;
	   	}	
   }