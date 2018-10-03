package gov.nih.nlm.semrep.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * A class that contains methods broadly useful in SemRep. 
 * 
 * @author Halil Kilicoglu
 *
 */

public class SemRepUtils {
	private static Logger log = Logger.getLogger(SemRepUtils.class.getName());	

	/**
	 * Obtains a socket to connect to a server on a given port.
	 * It will return null if a socket cannot be obtained.
	 * 
	 * @param serverName	name of the server
	 * @param serverPort		port to connect 
	 * @return a <code>Socket</code> object for connection
	 */
	public static Socket getSocket(String serverName, int serverPort) {
		Socket s = null;
		try {
			s = new Socket(serverName, serverPort);
		} 
		catch (UnknownHostException uhe) {
			log.warning("Unable to bind socket to server at " + serverName + ":" + serverPort + ".");
			uhe.printStackTrace();
		} 
		catch (IOException ioe) {
			log.warning("General IO error at creating socket to server at " + serverName + ":" + serverPort + ".");
			ioe.printStackTrace();
		}
		return s;
	}
	
	/**
	 * Closes a socket gracefully.
	 * 
	 * @param socket		Socket object to close
	 */
	public static void closeSocket(Socket socket) {
		try {
			socket.close();
		} catch (IOException ioe) {
			log.warning("Failed to close socket to " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Queries a server with the given socket and input string.
	 * It will return an empty string if a socket problem is encountered.
	 * 
	 * @param socket the socket connected with the the  server
	 * @param input string to be processed by the server process
	 * @return string returned by server process
	 */
	public static String queryServer(Socket socket,String input) {
		StringBuilder sb = new StringBuilder();  
		try {
			// write text to the socket
			DataInputStream bis = new DataInputStream(socket.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(bis));
			PrintWriter bw = new PrintWriter(socket.getOutputStream(), true);
			bw.println(input);
			bw.flush();
			String line = br.readLine();
			do {
				sb.append(line);
				line = br.readLine();
			} while(line != null && line.isEmpty() == false);
			bis.close();
			br.close();
		} catch (IOException ioe) {
			log.warning("Socket I/O error: " + socket.getInetAddress().getHostName() + ":" + socket.getPort());
			ioe.printStackTrace();
		}
		return sb.toString();
	}
	
}
