package gov.nih.nlm.semrepjava;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SemRepSocketClient {
	private String queryServer(Socket socket,String input) {
		 StringBuilder sb = new StringBuilder();  
		try {
			// write text to the socket
			DataInputStream bis = new DataInputStream(socket.getInputStream());
        	BufferedReader br = new BufferedReader(new InputStreamReader(bis));
    	 	PrintWriter bw = new PrintWriter(socket.getOutputStream(), true);
        	bw.println(input);
        	bw.flush();
        	String line;
        	while ((line = br.readLine()) != null) {
        		System.out.println(line);
        	}
        	bis.close();
        	br.close();

		} catch (IOException ioe) {
			System.err.println("Socket error");
		}
	      return sb.toString();
	}
	
	  
	public static void main(String args[]) throws IOException {
		try {
			int port = Integer.parseInt(args[1]);
			SemRepSocketClient client = new SemRepSocketClient();
			String inputText = new String(Files.readAllBytes(Paths.get("/export/home/pengz3/eclipse-workspace/semrepjava/test document.txt")));
			System.out.println(inputText);
			Socket s = new Socket(args[0], port);
			String answer = client.queryServer(s, inputText);
			//System.out.println(answer);
		} catch(Exception e) {
			e.printStackTrace();
		}
   }
}

