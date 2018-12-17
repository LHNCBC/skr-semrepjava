package gov.nih.nlm.semrep.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.Sentence;

/**
 * This class extends {@link gov.nih.nlm.ling.core.Document} class to include Medline-specific information
 * 
 * @author Zeshan Peng
 * @author Halil Kilicoglu
 *
 */
public class MedlineDocument extends Document{
	
	private static  String[] KEYS = {"ID", "PMID", "SO", "RF", "NI", "JC", "TA", "IS", "CY", "TT",
            "CA", "IP", "VI", "DP", "YR", "PG", "LID", "DA", "LR", "OWN",
            "STAT", "DCOM", "PUBM", "DEP", "PL", "JID", "SB", "PMC",
            "EDAT", "MHDA", "PST", "AB", "AD", "EA", "TI", "JT"};

	private String titleText;
	private String abstractText;
	
	public MedlineDocument(String id, String text) {
		super(id, text);
	}
	
	public MedlineDocument(String id, String ti, String ab) {
		this(id, ti + " " + ab);
		this.titleText = ti;
		this.abstractText = ab;
	}
	
	public MedlineDocument(String id, String text, List<Sentence> sentence) {
		super(id,text,sentence);
	}
	
	public MedlineDocument(String id, String text, List<Sentence> sentence, String title) {
		super(id,text,sentence);
		this.titleText = title;
	}
	
	public String getTitleText() {
		return this.titleText;
	}
	
	public String getAbstractText() {
		return this.abstractText;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("PMID- " + id + "\n");
		buf.append("TI  - " + titleText + "\n");
		buf.append("AB  - " + abstractText + "\n");
		return buf.toString();
	}
	
	/**
	 * Parses multiple Medline-formatted documents within the given buffered reader
	 * 
	 * @param br the buffered reader for a file containing multiple Medline-formatted documents
	 * @return a list of <code>MedlineDocument</code> objects
	 * @throws IOException if the buffered reader fails to read
	 */
	public static List<MedlineDocument> parseMultiMedLines(BufferedReader br) throws IOException {
		List<MedlineDocument> mdList = new ArrayList<>();
		String pkey = "",ckey,content,text,title,PMID,value;
		String line = br.readLine();
		JSONObject json = new JSONObject();
		MedlineDocument md;
		StringBuilder sb = new StringBuilder();

		while(line != null) {
			if(!line.trim().isEmpty()) {
				ckey = line.substring(0, 4).trim();
				content = line.substring(6, line.length()).trim();
				if(!ckey.isEmpty() && Arrays.asList(KEYS).contains(ckey)) {
					if (!pkey.isEmpty()) {
						if (json.has(pkey)) {
							value = json.getString(pkey);
							json.put(pkey, new StringBuilder(value).append(sb).toString());
						}else {
							json.put(pkey, sb.toString());
						}
						sb = new StringBuilder();
					}
					sb.append(content);
					pkey = ckey;
				}else if (ckey.isEmpty() && !pkey.isEmpty()){
					sb.append(" ");
					sb.append(content);
				}
			} else {
				if(!sb.toString().isEmpty()) {
					json.put(pkey, sb.toString());
					text = json.getString("AB");
					title = json.getString("TI");
					PMID = json.getString("PMID");
					md = new MedlineDocument(PMID,title,text);
					mdList.add(md);
					sb = new StringBuilder();
					json = new JSONObject();
					pkey = "";
				}
			}
			line = br.readLine();
		}
		return mdList;
	}
		
	/**
	 * Parses a list of Medline documents in XML format.
	 * 
	 * @param inPath	input path for the XML file
	 * @return	a list of <code>MedlineDocument</code> objects
	 * @throws IOException
	 */
	public static List<MedlineDocument> parseMultiMedLinesXML(String inPath) throws IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		MedlineParserHandler handler = new MedlineParserHandler();
		try {
			saxParser = factory.newSAXParser();
			saxParser.parse(new File(inPath), handler);
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			System.out.println("Failed to configure SAX parser");
		}
		return handler.getDocuments();
	}
	
	private static class MedlineParserHandler extends DefaultHandler {

	    private MedlineDocument currentDocument;
	    private boolean inID = false;
	    private boolean inTitle = false;
	    private boolean inAbstract = false;

	    private StringBuilder sbAbstract;
	    private StringBuilder sbTitle;
	    private StringBuilder sbID;
	    private String abs;

	    private List<MedlineDocument> docs = new ArrayList<>();

	    public List<MedlineDocument> getDocuments() {
	    	return docs;
	    }

	    @Override
	    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		String eName = localName; // element name
		if ("".equals(eName))
		    eName = qName; // namespaceAware = false

		if (eName.equalsIgnoreCase("medlinecitation")) {
			sbID = null;
			sbTitle = null;
			sbAbstract = null;
		} else if (eName.equalsIgnoreCase("pmid") && sbID == null) {
		    inID = true;
		    sbID = new StringBuilder();
		} else if (eName.equalsIgnoreCase("articletitle") && sbTitle == null) {
		    inTitle = true;
		    sbTitle = new StringBuilder();
		} else if (eName.equalsIgnoreCase("abstracttext") && sbAbstract == null) {
		    inAbstract = true;
			sbAbstract = new StringBuilder();
	    }
	    }

	    @Override
	    public void endElement(String uri, String localName, String qName) throws SAXException {
		String eName = localName; // element name
		if ("".equals(eName))
		    eName = qName; // namespaceAware = false

		if (eName.equalsIgnoreCase("medlinecitation")) {
				if(sbAbstract != null) {
					abs = sbAbstract.toString().trim();
//					sentList = SemRep.openNLPClient.sentenceSplit(abs);
//					currentDocument = new MedlineDocument(sbID.toString().trim(), abs, sentList, sbTitle.toString().trim());
					currentDocument = new MedlineDocument(sbID.toString().trim(),sbTitle.toString().trim(),sbAbstract.toString().trim());
	/*				for(Sentence sent: sentList) {
						sent.setDocument(currentDocument);
					}*/
					docs.add(currentDocument);
				}else {
//					docs.add(new MedlineDocument(sbID.toString().trim(), null, null, sbTitle.toString().trim()));
					docs.add(new MedlineDocument(sbID.toString().trim(), sbTitle.toString().trim(),""));
				}
		} else if (eName.equalsIgnoreCase("pmid")) {
		    inID = false;
		} else if (eName.equalsIgnoreCase("articletitle")) {
		    inTitle = false;
		} else if (eName.equalsIgnoreCase("abstracttext"))
		    inAbstract = false;
	    }

	    @Override
	    public void characters(char[] ch, int start, int length) throws SAXException {
			if (inID)
			    sbID.append(ch, start, length);
			else if (inTitle)
			    sbTitle.append(ch, start, length);
			else if (inAbstract) 
			    sbAbstract.append(ch, start, length);
	    }
	}

}
