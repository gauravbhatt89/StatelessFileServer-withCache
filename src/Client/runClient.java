package Client;
import ServerProperties.ServerProperties;
import java.io.*;
import java.sql.Time;
import java.util.Scanner;

public class runClient {
	public static void main(String [] args) {
		System.out.println(args[0]);
		
		ClientApi cApi = new ClientApi();
		fileHandle fh;
		try {
			System.out.println("Please enter one of following operations:");
			System.out.println("1. Read \n2. Write");
			Scanner sc = new Scanner(System.in);
			String op = sc.nextLine();
			
			/* OPEN REQUEST: create a file handle for this request for file */
			fh = cApi.open(args[0]);
			System.out.println("FileHandle is: "+fh);
	
			if (op.equals("1")|| op.equalsIgnoreCase("read")) {
				/* READ OPERATION :
				 * We will read the complete file from cache
				 * but 20 bytes at a time.
				 */	
				System.out.println("\nREADING:");
				long startTime = System.currentTimeMillis();
				while(!cApi.isEOF(fh)) {
					// Read 20 bytes at a time. 
					byte[] data = new byte[20];
					
					int numbytes = cApi.read(fh, data);
					for (int i=0;i<numbytes;i++){
						// Print as we read.
						System.out.print((char)data[i]);
					}
				}
				long endTime = System.currentTimeMillis();
				System.out.println("\nREAD SUCCESSFUL!");
				System.out.println("Read Operation Took: "+(endTime - startTime)+"mSeconds");
			} 
			else if (op.equals("2")|| op.equalsIgnoreCase("write")) { 
				/* WRITE OPERATION :
				 * We will write the data entered by the user to the file in cache, 
				 * appending at the last of the file. Then update the file in server.
				 */
				System.out.println("Enter the data to append in the file:\n ");		
				String st = sc.nextLine();
			
				// Read the file before write to get the latest file.
				while(!cApi.isEOF(fh)) {
					byte[] data = new byte[10];
					int numbytes = cApi.read(fh, data);
				}
			    
				byte[] data=new byte[st.length()];
				int j=0;
				for(int i=0;i<st.length();i++)
				{
					data[j]= (byte)st.charAt(i);
					j++;
				}
				long startTime = System.currentTimeMillis();
				cApi.write(fh, data);
				
				// Call close to update the file in server.
				cApi.close(fh);
				long endTime = System.currentTimeMillis();
				System.out.println("Write Operation Took: "+(endTime - startTime)+"mSeconds");
			} else {
				System.out.println("Please enter valid Operation!");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}