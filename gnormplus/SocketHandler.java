package gov.nih.nlm.ner.gnormplus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketHandler extends Thread {
    final BufferedInputStream bis;
    final BufferedOutputStream bos;
    final Socket socket;
    final GNormPlusJNIServer gps;

    // Constructor
    public SocketHandler(Socket s, BufferedInputStream bis, BufferedOutputStream bos, GNormPlusJNIServer gps) {
	this.socket = s;
	this.bis = bis;
	this.bos = bos;
	this.gps = gps;
    }

    @Override
    public void run() {
	String received;
	String toreturn;
	try {
	    BufferedReader br = new BufferedReader(new InputStreamReader(bis));
	    PrintWriter bw = new PrintWriter(socket.getOutputStream(), true);
	    String line;
	    while (true) {
		line = br.readLine();
		if (line != null && !line.equals("quit")) {
		    if (line != null) {
			System.out.println("Input: " + line);
			String result = gps.annotateText2String(line); // attach new line symbol at the end of the line
			System.out.println("Output: " + result);
			bw.println(result);
			bw.flush();
		    } else
			continue;
		} else
		    break;
	    } // end of inner while
	} catch (Exception e) {
	    e.printStackTrace();
	}

	try {
	    // closing resources
	    this.bis.close();
	    this.bos.close();

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
