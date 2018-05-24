package gov.nih.nlm.semrepjava.core;

import java.util.List;

import gov.nih.nlm.ling.core.Word;

public class Chunk {
	
	Word head;
	List<Word> trail;
	String chunkType;
	
	public Chunk(Word head, List<Word> trail, String chunkType) {
		this.head = head;
		this.trail = trail;
		this.chunkType = chunkType;
	}
	
	public Word getHead() {
		return this.head;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (trail != null && trail.size() > 0) {
			for (int i = 0; i < trail.size(); i++) {
				sb.append(wordToString(trail.get(i)));
			}
		}
		return "[ " + wordToString(this.head) + sb.toString() + " {" + this.chunkType + "} ]";
	}
	
	public String wordToString(Word w) {
		return w.getText() + " (" + w.getPos() + ", " + w.getLemma() + ") ";
	}

}
