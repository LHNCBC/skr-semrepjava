package gov.nih.nlm.semrep.core;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.ling.core.Chunk;
import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.Span;
import gov.nih.nlm.ling.core.Word;
import gov.nih.nlm.umls.lexicon.LexiconMatch;

/**
 * This class extends {@link gov.nih.nlm.ling.core.Sentence} class to include other sentence-related 
 * information relevant to SemRep, such as chunk information.
 * 
 * @author Zeshan Peng
 *
 */
public class SRSentence extends Sentence {

	List<Chunk> chunks;
	List<LexiconMatch> lexicalItems;
	String subsection;
	String sectionAbbreviation;
	String sentenceIDInSection;
	List<Processing> completedProcessing;
	
	public enum Processing {
		SSPLIT, TOKEN, LEXREC, TAG, LEMMA, PARSE, DEPPARSE, CHUNK, NER
	}; 
	
	public SRSentence(String id, String text, Span span) {
		super(id, text, span);
		this.subsection = "";
		this.sectionAbbreviation = "";
		this.sentenceIDInSection = "";
	}
	
	public SRSentence(String id, String text, Span span, String sectionAbbr) {
		super(id, text, span);
		this.sectionAbbreviation = sectionAbbr;
		this.subsection = "";
		this.sentenceIDInSection = "";
	}
	
	public SRSentence(String id, String text, Span span, List<Chunk> chunks, String sectionAbbr) {
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

	public List<LexiconMatch> getLexicalItems() {
		return lexicalItems;
	}

	public void setLexicalItems(List<LexiconMatch> lexicalItems) {
		this.lexicalItems = lexicalItems;
	}

	public List<String> getTags() {
		List<Word> wordList = this.getWords();
		List<String> tagList = new ArrayList<String>();
		for(int i = 0; i < wordList.size(); i++) {
			tagList.add(wordList.get(i).getPos());
		}
		return tagList;
	}
	
	public List<Processing> getCompleted() {
		return completedProcessing;
	}

	public void setCompleted(List<Processing> completedProcessing) {
		this.completedProcessing = completedProcessing;
	}
	
	public void addCompleted(Processing processing) {
		if (completedProcessing == null) completedProcessing = new ArrayList<>();
		completedProcessing.add(processing);
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
	
	public List<String> getRemainingFieldsForTextOutput() {
		List<String> fields = new ArrayList<String>();
		fields.add(Integer.toString(this.getSpan().getBegin()));
		fields.add(Integer.toString(this.getSpan().getEnd()));
		fields.add(this.getText());
		return fields;
	}
	
	/**
	 * Based on the input options, prints additional information, such as chunks or POS tags.
	 * 
	 * @param option	The option to print ("chunk" or "tag")
	 * @return	a string representation of the additional information
	 */
	public String printAdditionalInfo(String option) {
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
