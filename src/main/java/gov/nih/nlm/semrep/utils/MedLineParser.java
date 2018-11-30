package gov.nih.nlm.semrep.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.semrep.core.MedLineDocument;
/**
 * This class is used to parse MedLine formatted document
 * 
 * @author Zeshan Peng
 *
 */
public class MedLineParser {
	
	/**
	 * Parse multiple MedLine formatted documents within the given buffered reader
	 * 
	 * @param br the buffered reader for the multiple MedLine formatted documents
	 * @return a list of MedLineDocument objects
	 * @throws IOException if the buffered reader fails to read
	 */
	
	public static List<MedLineDocument> parseMultiMedLines(BufferedReader br, OpennlpUtils nlpClient) throws IOException {
		
		List<MedLineDocument> mdList = new ArrayList<MedLineDocument>();
		String[] textKeys = {"ID", "PMID", "SO", "RF", "NI", "JC", "TA", "IS", "CY", "TT",
		                     "CA", "IP", "VI", "DP", "YR", "PG", "LID", "DA", "LR", "OWN",
		                     "STAT", "DCOM", "PUBM", "DEP", "PL", "JID", "SB", "PMC",
		                     "EDAT", "MHDA", "PST", "AB", "AD", "EA", "TI", "JT"};
		String pkey = "",ckey,content,text,title,PMID,value;
		List<Sentence> sentList;
		String line = br.readLine();
		JSONObject json = new JSONObject();
		MedLineDocument md;
		StringBuilder sb = new StringBuilder();

		while(line != null) {
			if(!line.trim().isEmpty()) {
				ckey = line.substring(0, 4).trim();
				content = line.substring(6, line.length()).trim();
				if(!ckey.isEmpty() && Arrays.asList(textKeys).contains(ckey)) {
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
					sentList = nlpClient.sentenceSplit(text);
					title = json.getString("TI");
					PMID = json.getString("PMID");
					md = new MedLineDocument(PMID, text, sentList, title);
					for(Sentence sent: sentList) {
						sent.setDocument(md);
					}
					mdList.add(md);
					sb = new StringBuilder();
					json = new JSONObject();
					pkey = "";
				}
			}
			line = br.readLine();
		}
		
		br.close();
		return mdList;
	}
	
	/**
	 * Parse single MedLine formatted document within the given buffered reader
	 * 
	 * @param br the buffered reader for the single MedLine formatted document
	 * @return a MedLineDocument object
	 * @throws IOException if the buffered reader fails to read
	 */
	public static MedLineDocument parseSingleMedLine(BufferedReader br, OpennlpUtils nlpClient) throws IOException {
		return parseMultiMedLines(br, nlpClient).get(0);
	}
	
//	public static List<MedLineDocument> parseMultiMedLinesXML(String inPath, OpennlpUtils nlpClient, DocumentBuilder dBuilder) throws IOException {
//		
//		List<MedLineDocument> mdList = new ArrayList<MedLineDocument>();
//		MedLineDocument md;
//		String PMID=null, text=null, title=null;
//		List<Sentence> sentList = null;
//		Document doc;
//		try {
//			System.out.println("1111");
//			doc = dBuilder.parse(inPath);
//			System.out.println("2222");
//			if(doc.getElementsByTagName("PubmedArticleSet").getLength() > 0) {
//				Element articles = (Element) doc.getElementsByTagName("PubmedArticleSet").item(0);
//				if(articles.getElementsByTagName("PubmedArticle").getLength() > 0) {
//					NodeList nl = articles.getElementsByTagName("PubmedArticle");
//					Element article, cit, art;
//					for(int i = 0; i < nl.getLength(); i++) {
//						System.out.println("haha");
//						article = (Element) nl.item(i);
//						if(article.getElementsByTagName("MedlineCitation").getLength() > 0) {
//							cit = (Element) article.getElementsByTagName("MedlineCitation").item(0);
//							if(cit.getElementsByTagName("PMID").getLength() > 0) PMID = cit.getElementsByTagName("PMID").item(0).getTextContent();
//							if(cit.getElementsByTagName("Article").getLength() > 0) {
//								art = (Element) cit.getElementsByTagName("Article").item(0);
//								if(art.getElementsByTagName("ArticleTitle").getLength() > 0) title = art.getElementsByTagName("ArticleTitle").item(0).getTextContent();
//								if(art.getElementsByTagName("Abstract").getLength() > 0) {
//									text = art.getElementsByTagName("Abstract").item(0).getTextContent();
//									sentList = nlpClient.sentenceSplit(text);
//								}
//								System.out.println(title);
//							}
//						}
//						md = new MedLineDocument(PMID, text, sentList, title);
//						for(Sentence sent: sentList) {
//							sent.setDocument(md);
//						}
//						mdList.add(md);
//					}
//				}
//			}
//		} catch (SAXException e) {
//			e.printStackTrace();
//			System.out.println("Failed to parse Medline XML file using DocumentBuilder class...");
//			return null;
//		}
//		return mdList;
//	}
	
	public static List<MedLineDocument> parseMultiMedLinesXML(String inPath) throws IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		MedLineParserHandler handler = new MedLineParserHandler();
		try {
			saxParser = factory.newSAXParser();
			saxParser.parse(new File(inPath), handler);
		} catch (ParserConfigurationException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Failed to configure SAX parser");
		}
		
		return handler.getDocuments();
	}
	
}
