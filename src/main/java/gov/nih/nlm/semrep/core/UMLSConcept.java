package gov.nih.nlm.semrep.core;

import java.util.LinkedHashSet;

import gov.nih.nlm.ling.sem.Concept;

/**
 * This class extends bioscores concept class to include concept string and concept score
 * 
 * @author Zeshan Peng
 *
 */

public class UMLSConcept extends Concept {
	
	private String conceptString;
	private double score;
	

	public UMLSConcept(String cui, String name, LinkedHashSet<String> semtypes) {
		super(cui, name, semtypes);
	}
	
	public UMLSConcept(String cui, String name, LinkedHashSet<String> semtypes, String source) {
		super(cui, name, semtypes, source);
	}
	public UMLSConcept(String cui, String name, LinkedHashSet<String> semtypes, String source, String conceptString, double score) {
		super(cui, name, semtypes, source);
		this.conceptString = conceptString;
		this.score = score;
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

}
