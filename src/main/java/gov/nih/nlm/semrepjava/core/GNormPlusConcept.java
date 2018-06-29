package gov.nih.nlm.semrepjava.core;

import gov.nih.nlm.ling.sem.Ontology;

public class GNormPlusConcept implements Ontology {

    String name;
    String gnormType;
    int geneId;

    public GNormPlusConcept(String name, String gnormType, int geneId) {
	this.name = name;
	this.gnormType = gnormType;
	this.geneId = geneId;

    }

    @Override
    public String toString() {
	return new String(name + ":" + gnormType + ":" + geneId);
    }

}
