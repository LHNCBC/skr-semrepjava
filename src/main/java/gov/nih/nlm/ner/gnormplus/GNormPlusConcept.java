package gov.nih.nlm.ner.gnormplus;

import java.util.Arrays;
import java.util.LinkedHashSet;

import gov.nih.nlm.ling.sem.Concept;

public class GNormPlusConcept extends Concept {

	public GNormPlusConcept(String id, String name, String type) {
		super(id,name,new LinkedHashSet<>(Arrays.asList(type)),"gnormplus");
	}
}
