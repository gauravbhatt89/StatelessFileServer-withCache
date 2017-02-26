package Server;

import java.io.*;
import ServerProperties.ServerProperties;
import java.net.*;

public class operations extends Thread {
	ObjectInputStream in; 
	ObjectOutputStream out;
	
	Socket clientSock;
	
	public operations(Socket aClientSock) {
		try {
			clientSock = aClientSock;
			in = new ObjectInputStream(clientSock.getInputStream());
			out = new ObjectOutputStream(clientSock.getOutputStream());
			this.start();
		} catch (IOException e) {
			System.out.println("IO 3: "+e.getMessage());
		}
	}

	public void run(){
		try {
			ServerProperties sp = new ServerProperties();
			try {
				sp = (ServerProperties) in.readObject();
				System.out.println("Server Called for operation:"+sp.operation);
				
				if (sp.operation.equals("open")) {
					sp.isExist = Server.fileLookup(sp);
					out.writeObject(sp);
				} else if (sp.operation.equals("read")) {
					System.out.println("Operation is read: should be handelled in client");
					return;
				} else if (sp.operation.equals("write")) {
					File f=new File("./"+sp.fileName);
					if(f.exists())
					{
						f.delete();
					}
					f=new File("./"+sp.fileName);
					FileOutputStream fos=new FileOutputStream(f);
					fos.write(sp.data);							
					fos.close();		
				} else if (sp.operation.equals("IsEOF")) {
					System.out.println("IS-EOF: Should be handled in client");
					return;
				} else if (sp.operation.equals("copy")) {
					FileInputStream fis;
					BufferedInputStream bis=null;
					try {					         					        
				        File myFile = new File ("./"+sp.fileName);
				        sp.data  = new byte [(int)myFile.length()];
				        fis = new FileInputStream(myFile);
				        bis = new BufferedInputStream(fis);
				        bis.read(sp.data,0,sp.data.length);	
			            out.writeObject(sp);
			            System.out.println("Done.");
			        }
				    finally 
		            {       	
				    if (bis != null) 
				        bis.close();					      					      
				    }
				} else {
					System.out.println(sp.operation);
					System.out.println("Wrong Operation!");
					return;
				} 
				
			} catch (ClassNotFoundException e){
				e.printStackTrace();
			}
		} catch (EOFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IO 4:"+e.getMessage());
		} finally {
			try 
			{
					clientSock.close();			
					in.close();
					out.close();
			} catch (IOException e) {
				System.out.println("CLOSE:"+e.getMessage());
			}
		}
	}
}

