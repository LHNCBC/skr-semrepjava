package gov.nih.nlm.umls;

import java.io.FileNotFoundException;
import java.util.LinkedHashSet;

import com.sleepycat.je.DatabaseException;

import gov.nih.nlm.ling.core.Chunk;
import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SurfaceElement;
import gov.nih.nlm.ling.sem.Entity;
import gov.nih.nlm.ling.sem.SemanticItem;
import gov.nih.nlm.ner.metamap.ScoredUMLSConcept;

public class HypernymProcessing {
	
	static String hierarchyDB = "berkeleyDB";
	
	public static String intraNP(Chunk chunk) {
		if(!chunk.getChunkType().equalsIgnoreCase("NP"))
			return null;
		SurfaceElement head = null,modifier = null;
		for(SurfaceElement se: chunk.getSurfaceElementList()) {
			if(se.getChunkRole() == 'H') head = se;
			if(se.getChunkRole() == 'M') modifier = se;
		}
		if (head == null || modifier == null) return null;
		Document doc = head.getSentence().getDocument();
		LinkedHashSet<SemanticItem> headEntities = Document.getSemanticItemsBySpan(doc, head.getSpan(), true);
		if(headEntities.size() == 0) return null;
		LinkedHashSet<SemanticItem> modEntities = Document.getSemanticItemsBySpan(doc, modifier.getSpan(), true);
		if(modEntities.size() == 0) return null;
		String headCUI = null, modCUI = null;
		ScoredUMLSConcept headConcept = null, modConcept = null;
		for(SemanticItem si: headEntities) {
			 if(((Entity)si).getSense() instanceof ScoredUMLSConcept) {
				 headConcept = (ScoredUMLSConcept)((Entity)si).getSense();
				 headCUI = ((Entity)si).getSense().getId();
			 }
		}
		for(SemanticItem si: modEntities) {
			 if(((Entity)si).getSense() instanceof ScoredUMLSConcept) {
				 modConcept = (ScoredUMLSConcept)((Entity)si).getSense();
				 modCUI = ((Entity)si).getSense().getId();
			 }
		}
		if (headConcept == null || modConcept == null) return null;
		if(!semGroupMatch(headConcept.getSemGroups(), modConcept.getSemGroups())) return null;
		HierarchyDatabase hdb;
		try {
			hdb = new HierarchyDatabase(hierarchyDB, true);
			if(hdb.getData(headCUI+modCUI)) return headCUI+modCUI;
			if(hdb.getData(modCUI+headCUI)) return modCUI+headCUI;
			return null;
		} catch (DatabaseException | FileNotFoundException e) {
			System.out.println("Unable to open hierarchy database.");
		}
		return null;
	}
	
	public static boolean semGroupMatch(LinkedHashSet<String> head, LinkedHashSet<String> mod) {
		for(String s : head) {
			if(mod.contains(s)) return true;
		}
		return false;
	}

}
