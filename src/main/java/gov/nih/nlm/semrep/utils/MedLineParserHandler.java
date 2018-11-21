package gov.nih.nlm.semrep.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.semrep.SemRep;
import gov.nih.nlm.semrep.core.MedLineDocument;

/**
 *
 * Sax parser for a list of articles of pubmed, as returned by the EFetch web
 * service (in xml format).
 *
 *
 * @author Dongwook Shin
 *
 */
public class MedLineParserHandler extends DefaultHandler {

    private MedLineDocument currentDocument;
    private boolean inID = false;
    private boolean inTitle = false;
    private boolean inAbstract = false;

    private StringBuilder sbAbstract;
    private StringBuilder sbTitle;
    private StringBuilder sbID;
    private String abs;

    private List<MedLineDocument> docs = new ArrayList<>();
    private List<Sentence> sentList;

    public List<MedLineDocument> getDocuments() {
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
		try {
			if(sbAbstract != null) {
				abs = sbAbstract.toString().trim();
				sentList = SemRep.nlpClient.sentenceSplit(abs);
				currentDocument = new MedLineDocument(sbID.toString().trim(), abs, sentList, sbTitle.toString().trim());
				for(Sentence sent: sentList) {
					sent.setDocument(currentDocument);
				}
				docs.add(currentDocument);
			}else {
				docs.add(new MedLineDocument(sbID.toString().trim(), null, null, sbTitle.toString().trim()));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Failed to create medline document in SAX parser handler");
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

