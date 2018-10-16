package gov.nih.nlm.semrep.core;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.Span;
import gov.nih.nlm.ling.core.Word;

/**
 * This class extends bioscores sentence class to include chunk information
 * 
 * @author Zeshan Peng
 *
 */
public class ChunkedSentence extends Sentence{

	List<Chunk> chunks;
	
	public ChunkedSentence(String id, String text, Span span) {
		super(id, text, span);
	}
	
	public ChunkedSentence(String id, String text, Span span, List<Chunk> chunks) {
		super(id,text,span);
		this.chunks = chunks;
	}
	
	public void setChunks(List<Chunk> chunks) {
		this.chunks = chunks;
	}
	
	public List<Chunk> getChunks() {
		return this.chunks;
	}
	
	public List<String> getTags() {
		List<Word> wordList = this.getWords();
		List<String> tagList = new ArrayList<String>();
		for(int i = 0; i < wordList.size(); i++) {
			tagList.add(wordList.get(i).getPos());
		}
		return tagList;
	}

}
