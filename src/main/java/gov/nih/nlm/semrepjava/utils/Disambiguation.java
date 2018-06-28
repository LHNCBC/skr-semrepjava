package gov.nih.nlm.semrepjava.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Entity;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.nls.wsd.algorithms.AEC.AECMethod;
import gov.nih.nlm.nls.wsd.algorithms.MRD.CandidateCUI;
import gov.nih.nlm.semrepjava.core.UMLSConcept;

public class Disambiguation {
	
	AECMethod disambiguationMethod = new AECMethod();
	
	public Disambiguation() {
		
	}
	
	public UMLSConcept disambiguateEntities(Document doc, Map<SpanList, LinkedHashSet<Ontology>> annotations) {
		LinkedHashSet<Ontology> onts;
		Iterator<Ontology> itr;
		UMLSConcept concept;
		for(SpanList sl: annotations.keySet()) {
			onts = annotations.get(sl);
			itr = onts.iterator();
			if(onts.size() <= 1) {
				//return (UMLSConcept) itr.next();
			}else {
				List<CandidateCUI> cuis = new ArrayList<CandidateCUI>();
				Map<String, UMLSConcept> nameConceptMap = new HashMap<String, UMLSConcept>();
				while(itr.hasNext()) {
					concept = (UMLSConcept) itr.next();
					nameConceptMap.put(concept.getName(), concept);
					cuis.add(new CandidateCUI(concept.getName(), concept.getId()));
				}
				System.out.println(doc.getText() + "123");
				//List<String> filteredNames = AECMethod.disambiguate(null, null);
				//AECMethod.similarity(null, null);
//				if(filteredNames.size() == 1) {
//					System.out.println("haha");
//					//Entity en = doc.getSemanticItemFactory().newEntity(doc, sp, headSp, type);
//				}

			}
		}
		return null;
	}

}
