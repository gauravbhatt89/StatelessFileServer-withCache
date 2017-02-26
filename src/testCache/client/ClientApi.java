package testCache.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;

import javax.swing.text.html.HTML;

import org.omg.PortableInterceptor.ServerRequestInfoOperations;

import ServerProperties.ServerProperties;

public class ClientApi {

	/* Hash Table to store Server properties with
	 * corresponding file handle.
	 */
	HashMap<fileHandle, ServerProperties> Htable = 
			new HashMap<fileHandle, ServerProperties>();
	
	
	/*
	 * OPEN Operation. Initial operation to get the 
	 * fileName, port and hostname of the server.
	 * Will communicate with the server and checks if file
	 * exists or not. And cache the file if required.
	 */
	public fileHandle open(String url) throws IOException {

		/* URL is of format 'IP:port/path'
		 * Split the url to get IP(hostname), 
		 * portnum and filename(path).
		 */
		String temp[] = url.split("/");
		String filename = temp[1];
		
		temp = temp[0].split(":");
		int portnum = Integer.parseInt(temp[1]);
		String hostname = temp[0];
		
		// Fill the server properties
		ServerProperties sp = new ServerProperties();
		sp.fileName = filename;
		sp.hostName = hostname;
		sp.portnum = portnum;
		sp.operation = "open";
		
		System.out.println("Port Number and FileName is: "+sp.portnum +" & "+ sp.fileName);
		
		// Create the Socket
		Socket s = new Socket(hostname, portnum);
		
		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
		out.writeObject(sp);
		
		ObjectInputStream in = new ObjectInputStream(s.getInputStream());
		try {
			sp = (ServerProperties) in.readObject();
			// Prooceed only if file exists in the server. 
			if (sp.isExist) {		
				// Check if file stored in the cache.
				if (isCached(filename)) {	
					System.out.println("File Exists in cache");
					File f = new File("./"+ filename);
					Date date1 = new Date(sp.lastModifiedTime);
					Date date2 = new Date(f.lastModified());
					System.out.println("File was last modified in server at: "+date1);
					// Update cache if file in cache is outdated.
					if(sp.lastModifiedTime > f.lastModified()) {
						System.out.println("File is not latest.\n"
								+"Local modified time is : "+date2);
						System.out.println("Updating the file in cache");
						updateCache(s,sp);
					}
				} else {
					// Copy file in cache.
					System.out.println("File is not in cache, Copying from server to Client cache");
					updateCache(s,sp);
				}
				fileHandle fh = new fileHandle();
				sp.operation = "";
				sp.isExist = false;
				Htable.put(fh, sp);
				return fh;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (s!=null) 
				s.close();
			if (out!=null)
				out.close();
			if (in!=null)
				in.close();		
		}		
		return null;
	}

	
	/*
	 *  Operation to check if the file is stored in the 
	 *  cache or not.
	 */
	private boolean isCached(String fileName) {
		File f = new File("./"+fileName);
		if(f.exists()) {
			return true;
		}
		else {
			return false;
		}
	}

	
	/* 
	 * Copying or Updating the file from server to local cache. 
	 */
	private void updateCache(Socket s, ServerProperties sp) 
			throws UnknownHostException, IOException, ClassNotFoundException {
		
		sp.operation="copy";
		
		// Create the Socket.
		s=new Socket(sp.hostName, sp.portnum);
		
		// Create Input/Outupt Stream.
		ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());			
		out.writeObject(sp);			
		
		ObjectInputStream in = new ObjectInputStream(s.getInputStream());
		sp=(ServerProperties)in.readObject();														 					
	
		File f=new File(sp.fileName);			
		
		FileOutputStream fos = new FileOutputStream(f);
		// Write from start the whole file.
		fos.write(sp.data, 0, sp.data.length);
		
		if(out!=null)
			out.close();
		if(in!=null)
			in.close();
		if(s!=null)
			s.close();
		if(fos!=null)
			fos.close();				
	}
	
	/*
	 * Read Operation. Reads 'data' bytes from the file 
	 * in the cache.
	 */
	public int read(fileHandle fh, byte[] data) throws IOException {
		FileInputStream fs=null;
		try
		{
			ServerProperties sp = Htable.get(fh);	
			
			File file=new File("./"+sp.fileName);
			fs=new FileInputStream(file);
			
			
			int total_bytes= (int)file.length();
			if(sp.offset>=total_bytes) {
			//	System.out.println("File end reached.");
				return -1;
			}
			
			int bytes_available = total_bytes - sp.offset;
			int bytes2Read;
			
			if(data.length>bytes_available) {
				bytes2Read=bytes_available;
			}
			else {
				bytes2Read=data.length;
			}
			if(bytes2Read <= 0) {
				System.out.println("Reached End Of the File");
				return -1; //EOF reached.
			}
			
			int i= 0, j = 0;
			if(sp.offset!=0)			
				while(i<sp.offset){				
					j = fs.read(data, 0,sp.bytesRead);				
					i = i + j;
				}
					
				j = fs.read(data,0,bytes2Read);	
				if(j > 0){
					sp.offset=sp.offset + j;
					sp.bytesRead = j;
					Htable.put(fh, sp); 
				}
				return j;
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(fs!=null){
				try 
				{					
					fs.close();					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return -1;
	}

	
	/*
	 * Write Operation. writes 'data' to the file in cache.
	 */
	public boolean write(fileHandle fh, byte[] data) throws IOException {
		RandomAccessFile file=null;
		try {
			ServerProperties sp = Htable.get(fh);
			
			file = new RandomAccessFile("./"+sp.fileName, "rw");
			file.seek(sp.offset);
			file.write(data);
			
			sp.offset = sp.offset + data.length;
			sp.bytesRead = sp.bytesRead+data.length;
			Htable.put(fh, sp);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (file != null)
					file.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
   
	
	/*
     * isEOF Operation. Checks if end of file is reached.
     */
	public boolean isEOF(fileHandle fh) throws IOException {
		
		try
		{
			int bytes2Read, bytes_available, total_bytes;
			ServerProperties sp=Htable.get(fh);	
			
		//	System.out.println(sp.fileName);
			
			File f=new File("./"+sp.fileName);
			FileInputStream fs = new FileInputStream(f);
			
			byte[] data=new byte[]{' '};
				
			total_bytes= (int)f.length();
			if(sp.offset >= total_bytes) {
			//	System.out.println("\nEOF1 "+sp.offset);
				return true; //EOF.
			}
				
			bytes_available = total_bytes - sp.offset;
				
			if(data.length > bytes_available) {
				bytes2Read = bytes_available;
			} else {
				bytes2Read = data.length;
			}
			
			if(bytes2Read <= 0){
			//	System.out.println("\nEOF!!" +bytes2Read);
				return true;
			}
			
			if(fs!=null)
				fs.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 		
		return false;
	}

	
	/*
	 * CLOSE operation. Copy data from file in 
	 * cache to file on the server.
	 */
	public boolean close(fileHandle fh)  
	{		
		try
		{
			ServerProperties sp = Htable.get(fh);	
			
			File f = new File("./"+sp.fileName);
			FileInputStream fs = new FileInputStream(f);
										
			if(f.lastModified() > sp.lastModifiedTime)
			{
				//Write back the file modified to the server
				System.out.println("Writing modified file to the server.");
				RandomAccessFile file = new RandomAccessFile("./"+sp.fileName, "rw");
				
				sp.data=new byte[(int) file.length()];
				file.readFully(sp.data);				
				sp.operation = "write";
				
				Socket s = new Socket(sp.hostName, sp.portnum);
				
				ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());			
				out.writeObject(sp);	
				
				System.out.println("Written successfully.");	
				
				Thread.sleep(1);
				
				if(fs!=null)
					fs.close();				
				if(s!=null)
					s.close();
				if(out!=null)
					out.close();
				if(file!=null)
					file.close();				
			}
			else
			{
				return true;
			}			
		}
		catch(IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Htable.remove(fh);					
		}
		System.out.println("\nClose Successful.");
		return true;
	}
}
	
 