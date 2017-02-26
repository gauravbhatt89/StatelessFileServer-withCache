package Server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.io.*;
import ServerProperties.ServerProperties;

public class Server {
	public static void main (String[] args){
		try {
			int port = 7896;
			ServerSocket serverSock = new ServerSocket(port);
			while(true) {
				Socket clientSock = serverSock.accept();
				operations op = new operations(clientSock);
			}
		} catch (IOException e) {
			System.out.println("IO 1: "+e.getMessage());
		}
	}
	
	/* 
	 * Lookup Operation: To check if file exists 
	 * in the Server. If so save its last modified time
	 * and send back to client for maintaining cache.
	 */
	public static boolean fileLookup (ServerProperties sp) {
		try {
		//	System.out.println("./"+sp.fileName);
			
			FileInputStream f = new FileInputStream("./" + sp.fileName);
		//	System.out.println("File Exists: "+ f);
			
			// Save its last modified time.
			sp.lastModifiedTime=getAttribute("./"+sp.fileName);
			try {
				f.close();
			} catch (IOException e) {
				System.out.println("IO 2: "+e.getMessage());
			}
			return true;
		} catch (FileNotFoundException e) {
				System.out.println("File does not exists");
				return false;
			}		
	}
	
	/*
	 * getAttribute Function to get the last modified
	 * time if the file 
	 */
	public static long getAttribute(String fileName)
	{
		File f=new File("./"+fileName);			
		return f.lastModified();
	}
	
	
	/*
	 * Write Operation: To write the data to the file.
	 */
	public static boolean write(ServerProperties sp)  
	{
		RandomAccessFile file=null;
		try
		{		
			file = new RandomAccessFile("./"+sp.fileName, "rw");
			String ss = sp.data.toString();
			String ss1 = ss.replace("","\r\n");
			sp.data = ss1.getBytes();
			file.seek(sp.offset);		
			file.write(sp.data);				
			sp.offset = sp.offset + sp.data.length;
			sp.bytesRead = sp.bytesRead + sp.data.length;		
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if(file!=null)
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
}