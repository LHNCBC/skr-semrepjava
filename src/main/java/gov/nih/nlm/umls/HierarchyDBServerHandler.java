package gov.nih.nlm.umls;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class HierarchyDBServerHandler extends Thread {

	private static Logger log = Logger.getLogger(HierarchyDBServerHandler.class.getName());	
	final BufferedInputStream bis;
	final BufferedOutputStream bos;
	final Socket socket;
	final HierarchyDatabase hdb;
	

	// Constructor
	public HierarchyDBServerHandler(Socket s, BufferedInputStream bis, BufferedOutputStream bos, HierarchyDatabase hdb){
		this.socket = s;
		this.bis = bis;
		this.bos = bos;
		this.hdb = hdb;
	}
	
	@Override
	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(bis));
		PrintWriter bw;
		try {
			bw = new PrintWriter(socket.getOutputStream(), true);
			String inputText = br.readLine();
			System.out.println("inputtext: " + inputText);
			if(hdb.contains(inputText))
				bw.print("true");
			else
				bw.print("false");
			bw.flush();
			br.close();
			bw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			// closing resources
			this.bis.close();
			this.bos.close();

		} catch (IOException e) {
			log.severe("Unable to close Hierarchy Database Server streams.");
			e.printStackTrace();
		}
	}
}
