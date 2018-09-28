package gov.nih.nlm.ner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Ontology;

/**
 * A class to combine/filter annotations  based on their span lengths.
 * Annotations with longer spans have higher priority.
 * 
 * @author Halil Kilicoglu
 *
 */
public class LargestSpanFilter implements AnnotationFilter {
	private static Logger log = Logger.getLogger(LargestSpanFilter.class.getName());	

	@Override
	public Map<SpanList, LinkedHashSet<Ontology>> filter(Map<SpanList, LinkedHashSet<Ontology>> inAnnotations) {
		if (inAnnotations == null || inAnnotations.size() == 0) 
			return new HashMap<SpanList,LinkedHashSet<Ontology>>();
		List<SpanList> sortedSpans = new ArrayList<>( inAnnotations.keySet());
		Collections.sort(sortedSpans, new Comparator<SpanList>() {
			public int compare(SpanList a, SpanList b) {
				if (a.length() > b.length()) return -1;
				else if (a.length() < b.length()) return 1;
				else {
					return -1;
				}
			}
		});
		Map<SpanList,LinkedHashSet<Ontology>> outAnnotations = new HashMap<>();
		for (SpanList sp: sortedSpans) {
			log.finest("Annotations sorted by span: " + sp.toString() + " " + inAnnotations.get(sp));
			if (overlap(sp,outAnnotations.keySet())) continue;
			outAnnotations.put(sp, inAnnotations.get(sp));
		}
		return outAnnotations;
	}
	
	private boolean overlap(SpanList sp, Set<SpanList> spans) {
		for (SpanList s: spans) {
			if (SpanList.overlap(s, sp)) return true;
		}
		return false;
	}

}
