package gov.nih.nlm.semrep.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

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
	
}
