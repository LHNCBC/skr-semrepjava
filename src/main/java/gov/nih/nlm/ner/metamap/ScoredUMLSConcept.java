package gov.nih.nlm.ner.metamap;

import java.util.LinkedHashSet;

import gov.nih.nlm.ling.sem.Concept;

/**
 * This class extends bioscores concept class to include concept string and concept score
 * 
 * @author Zeshan Peng
 *
 */

public class ScoredUMLSConcept extends Concept {
	
	private String conceptString;
	private double score;
	private LinkedHashSet<String> semgroups;
	

	public ScoredUMLSConcept(String cui, String name, LinkedHashSet<String> semtypes) {
		super(cui, name, semtypes);
	}
	
	public ScoredUMLSConcept(String cui, String name, LinkedHashSet<String> semtypes, String source) {
		super(cui, name, semtypes, source);
	}
	public ScoredUMLSConcept(String cui, String name, LinkedHashSet<String> semtypes, String source, String conceptString, double score) {
		super(cui, name, semtypes, source);
		this.conceptString = conceptString;
		this.score = score;
	}
	
	public ScoredUMLSConcept(String cui, String name, LinkedHashSet<String> semtypes, LinkedHashSet<String> semgroups, String source, String conceptString, double score) {
		super(cui, name, semtypes, source);
		this.conceptString = conceptString;
		this.score = score;
		this.semgroups = semgroups;
	}
	
	public void setConceptString(String s) {
		this.conceptString = s;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	
	public String getConceptString() {
		return this.conceptString;
	}
	
	public double getScore() {
		return this.score;
	}
	
	public LinkedHashSet<String> getSemGroups() {
		return this.semgroups;
	}
	
	public void setSemGroups(LinkedHashSet<String> semgroups) {
		this.semgroups = semgroups;
	}

}
