package gov.nih.nlm.ner;

import java.util.LinkedHashSet;
import java.util.Map;

import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Ontology;

/**
 * An interface for classes that perform merging and filtering of annotations
 * from different named entity recognizers.
 * 
 * @author Halil Kilicoglu
 *
 */

public interface AnnotationFilter {

	/**
	 * Method to implement to define heuristics to combine/filter named entity 
	 * annotations, potentially from different recognizers
	 * 
	 * @param inputAnnotations	annotations keyed by their spans
	 * @return	a new set of annotations where unwanted <code>Ontology</code> objects are filtered out.
	 */
	public Map<SpanList,LinkedHashSet<Ontology>> filter(Map<SpanList,LinkedHashSet<Ontology>> inputAnnotations);
	
}
