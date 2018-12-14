package gov.nih.nlm.semrep.core;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to represent temporary token information before we generate surface elements for text.
 * This class can be useful in preprocessing with various tools, such as OpenNLP.  <p>
 * Token information includes text, span information, POS tag, and lemma. 
 * 
 * @author Halil Kilicoglu
 *
 */
public class TokenInfo {
	 private String token;
	 private int begin;
	 private int end;
	 private String pos;
	 private String lemma;
	 
	 public TokenInfo(String token, int b, int e) {
		 this.token = token;
		 this.begin = b;
		 this.end = e;
	 }
	 
	 public TokenInfo(String token, int b, int e , String pos) {
		 this(token,b,e);
		 this.pos =pos;
	 }
	 
	 public TokenInfo(String token, int b, int e, String pos, String lemma) {
		 this(token,b,e,pos);
		 this.lemma = lemma;
	 }

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getLemma() {
		return lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	
	public static List<String> getTokens(List<TokenInfo> toks) {
		List<String> strs = new ArrayList<>();
		for (TokenInfo ti: toks) {
			strs.add(ti.getToken());
		}
		return strs;
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(token + "_" + begin + "_" + end);
		if (pos != null) buf.append("_" + pos);
		if (lemma != null) buf.append("_" + lemma);
		return buf.toString().trim();
	}
	
	/**
	 * Generates a list of <code>TokenInfo</code> objects from  arrays of strings, begin and end offsets.
	 * 
	 * @param tokens		strings of the tokens
	 * @param begins		begin offsets of the tokens
	 * @param ends		end offsets of the tokens
	 * @return		A list of <code>TokenInfo</code> objects corresponding to the input arguments
	 */
	 public static  List<TokenInfo> convert(String[] tokens, int[] begins, int[] ends) {
		 	if (tokens.length != begins.length  || tokens.length != ends.length)
		 		throw new IllegalArgumentException("Array sizes should be the same.");
			 List<TokenInfo> infos = new ArrayList<>();
			 for (int i=0; i < tokens.length; i++) {
				 TokenInfo tsp = new TokenInfo(tokens[i],begins[i],ends[i]);
				 infos.add(tsp);
			 }
			 return infos;
		 }
	 
	 /**
	  * Returns the texts of the token objects as an array.
	  * 
	  * @param infos	the token objects
	  * @return	a string array of texts
	  */
	 public static  String[] getTokensFromInfo(List<TokenInfo> infos) {
		 String[] tokens = new String[infos.size()];
		 int i =0;
		 for (TokenInfo pair: infos) {
			 tokens[i++] = pair.getToken();
		 }
		 return tokens;
	 }
	 	 
	 /**
	  * Returns the POS tags of the token objects as an array.
	  * 
	  * @param infos	the token objects
	  * @return		a string array of texts
	  */
	 public static  String[] getPOSTagsFromInfo(List<TokenInfo> infos) {
		 String[] tags = new String[infos.size()];
		 int i =0;
		 for (TokenInfo pair: infos) {
			 tags[i++] = pair.getPos();
		 }
		 return tags;
	 }
	 
	 /**
	  * Returns the lemmas of the token objects as an array.
	  * 
	  * @param infos	the token objects
	  * @return		a string array of lemmas
	  */
	 public static  String[] getLemmasFromInfo(List<TokenInfo> infos) {
		 String[] tags = new String[infos.size()];
		 int i =0;
		 for (TokenInfo pair: infos) {
			 tags[i++] = pair.getLemma();
		 }
		 return tags;
	 }
}
