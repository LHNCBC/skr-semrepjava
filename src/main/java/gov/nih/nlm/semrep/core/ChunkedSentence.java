package gov.nih.nlm.semrep.core;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.ling.core.Chunk;
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
	String subsection;
	String sectionAbbreviation;
	String sentenceIDInSection;
	
	public ChunkedSentence(String id, String text, Span span) {
		super(id, text, span);
		this.subsection = "";
		this.sectionAbbreviation = "";
		this.sentenceIDInSection = "";
	}
	
	public ChunkedSentence(String id, String text, Span span, String sectionAbbr) {
		super(id, text, span);
		this.sectionAbbreviation = sectionAbbr;
		this.subsection = "";
		this.sentenceIDInSection = "";
	}
	
	public ChunkedSentence(String id, String text, Span span, List<Chunk> chunks, String sectionAbbr) {
		super(id,text,span);
		this.chunks = chunks;
		this.sectionAbbreviation = sectionAbbr;
		this.subsection = "";
		this.sentenceIDInSection = "";
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
	
	public void setSubsection(String subsection) {
		this.subsection = subsection;
	}
	
	public String getSubsection() {
		return this.subsection;
	}
	
	public void setSectionAbbreviation(String abbr) {
		this.sectionAbbreviation = abbr;
	}
	
	public String getSectionAbbreviation() {
		return this.sectionAbbreviation;
	}
	
	public void setSentenceIDInSection(String id) {
		this.sentenceIDInSection = id;
	}
	
	public String getSentenceIDInSection() {
		return this.sentenceIDInSection;
	}
	
	public List<String> getRemainFieldsForTextOutput() {
		List<String> fields = new ArrayList<String>();
		fields.add(Integer.toString(this.getSpan().getBegin()));
		fields.add(Integer.toString(this.getSpan().getEnd()));
		fields.add(this.getText());
		return fields;
	}
	
	public String getIncludeInfo(String option) {
		StringBuilder sb = new StringBuilder();
		if(option.equalsIgnoreCase("chunk")) {
			List<Chunk> chunkList = this.getChunks();
			for(int j = 0; j < chunkList.size(); j++) {
				sb.append(chunkList.get(j).toString() + "\n");
			}
		} else if(option.equalsIgnoreCase("tag")) {
			List<String> tagList = this.getTags();
			sb.append(String.join(" ", tagList) + "\n");
		}
		return sb.toString();
	}

}
