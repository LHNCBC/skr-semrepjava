package gov.nih.nlm.semrepjava;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SemRepSocketServer {
	
	public static void main(String[] args) throws IOException {
		 int port = Integer.parseInt(args[0]);
		 ServerSocket serverSocket = new ServerSocket(port); 

		 try {
	     while(true) {
	    	 Socket socket = serverSocket.accept();
	    	 System.out.println("Client connected");
	    	 BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
	    	 BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
	    	 Thread t = new SemRepSocketHandler(socket, bis, bos);
	    	 t.start();
	     }
		 } catch(Exception e) {
			 e.printStackTrace();
		 }

	}
}
