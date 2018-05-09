package gov.nih.nlm.semrepjava.core;

import gov.nih.nlm.ling.core.Word;
import gov.nih.nlm.ling.core.WordLexeme;

public class ChunkedWord extends Word{
	
	String chunkerTag;
	
	public ChunkedWord(String text, String pos, WordLexeme lex, String tag) {
		super(text, pos, lex);
		this.chunkerTag = tag;
		// TODO Auto-generated constructor stub
	}
	
	public String getChunkerTag() {
		return this.chunkerTag;
	}

}
