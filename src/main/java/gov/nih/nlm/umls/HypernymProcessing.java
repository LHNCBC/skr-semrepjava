package gov.nih.nlm.umls;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

import com.sleepycat.je.DatabaseException;

import gov.nih.nlm.ling.core.Chunk;
import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SurfaceElement;
import gov.nih.nlm.ling.sem.Argument;
import gov.nih.nlm.ling.sem.Entity;
import gov.nih.nlm.ling.sem.SemanticItem;
import gov.nih.nlm.ner.metamap.ScoredUMLSConcept;

/**
 * Class for hypernymy resolution.
 * 
 * @author Zeshan Peng
 *
 */
public class HypernymProcessing {
    private static Logger log = Logger.getLogger(HypernymProcessing.class.getName());
	
	String hierarchyDB = "hierarchyDB";
	HierarchyDatabase hdb;
	
	public HypernymProcessing(String dbDir) {
		try {
			hdb = new HierarchyDatabase(dbDir, true);		
		} catch (DatabaseException e) {
			log.severe("Unable to open the UMLS concept hierarchy DB.");
		}
	}
	
	
	public List<Argument> intraNP(Chunk chunk) {
		if(!chunk.getChunkType().equalsIgnoreCase("NP"))
			return null;
		SurfaceElement first = null, second = null;
		List<SurfaceElement> seList = chunk.getSurfaceElementList();
		List<Argument> result;
		int size = seList.size();
		for(int i = size - 1; i >= 0; i--) {		
			if(first == null) {
				if(seList.get(i).getChunkRole() == 'H') {
					first = seList.get(i);
				}
			}else {
				if(seList.get(i).getChunkRole() == 'M') {
					second = seList.get(i);
					result = findIntraNPBetweenSurfaceElements(first, second);
					if (result != null) return result;
					else {
						first = second;
						second = null;
					}
				}
			}
		}
		return null;
	}
	
	public List<Argument> findIntraNPBetweenSurfaceElements(SurfaceElement first, SurfaceElement second) {
		if(first == null || second == null) return null;
		Document doc = first.getSentence().getDocument();
		LinkedHashSet<SemanticItem> firstEntities = Document.getSemanticItemsBySpan(doc, first.getSpan(), true);
		if(firstEntities.size() == 0) return null;
		LinkedHashSet<SemanticItem> secondEntities = Document.getSemanticItemsBySpan(doc, second.getSpan(), true);
		if(secondEntities.size() == 0) return null;
		String firstCUI = null, secondCUI = null;
		ScoredUMLSConcept firstConcept = null, secondConcept = null;
		SemanticItem firstEntity = null, secondEntity = null;
		Argument subject, object;
		List<Argument> args = new ArrayList<>();
		for(SemanticItem si: firstEntities) {
			 if(((Entity)si).getSense() instanceof ScoredUMLSConcept) {
				 firstEntity = si;
				 firstConcept = (ScoredUMLSConcept)((Entity)si).getSense();
				 firstCUI = ((Entity)si).getSense().getId();
			 }
		}
		for(SemanticItem si: secondEntities) {
			 if(((Entity)si).getSense() instanceof ScoredUMLSConcept) {
				 secondEntity = si;
				 secondConcept = (ScoredUMLSConcept)((Entity)si).getSense();
				 secondCUI = ((Entity)si).getSense().getId();
			 }
		}
		if (firstConcept == null || secondConcept == null) return null;
		if (firstCUI.equals("C1457887") || secondCUI.equals("C1457887")) return null;
		if (firstCUI.equals(secondCUI)) return null;
		if(!semGroupMatch(firstConcept.getSemGroups(), secondConcept.getSemGroups())) return null;
		if(hdb.contains(firstCUI+secondCUI)) {
			subject = new Argument("subject", firstEntity);
			object = new Argument("object", secondEntity);
			args.add(subject);
			args.add(object);
			return args;
		}
		if(hdb.contains(secondCUI+firstCUI)) {
			subject = new Argument("subject", secondEntity);
			object = new Argument("object", firstEntity);
			args.add(subject);
			args.add(object);
			return args;
		}
		return null;
	}
	
	public static boolean semGroupMatch(LinkedHashSet<String> first, LinkedHashSet<String> second) {
		for(String s : first) {
			if(!s.contains("conc") && !s.contains("anat") && second.contains(s)) return true;
		}
		return false;
	}

}
