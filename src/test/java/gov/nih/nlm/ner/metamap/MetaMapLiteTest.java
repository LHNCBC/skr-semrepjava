package gov.nih.nlm.ner.metamap;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.ling.util.FileUtils;
import gov.nih.nlm.ner.gnormplus.GNormPlusClientTest;
import gov.nih.nlm.ner.gnormplus.GNormPlusConcept;
import gov.nih.nlm.ner.metamap.MetaMapLiteClient;
import gov.nih.nlm.semrep.SemRep;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for MetaMapLite server
 */
public class MetaMapLiteTest 
    extends TestCase
{
	private static Logger log = Logger.getLogger(MetaMapLiteTest.class.getName());	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MetaMapLiteTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MetaMapLiteTest.class );
    }

    /**
     * Rigourous Test :-)
     * @throws IOException 
     */
    public void testMetaMapLite() throws IOException
    {
    	SemRep.initLogging();
    	Properties props = FileUtils.loadPropertiesFromFile("semrepjava.properties");
    	MetaMapLiteClient client = new MetaMapLiteClient(props);
		Map<SpanList, LinkedHashSet<Ontology>> annotations = new HashMap<SpanList, LinkedHashSet<Ontology>>();
		Document doc = SemRep.lexicoSyntacticAnalysis("0", "breast cancer");
		client.annotate(doc, System.getProperties(), annotations);
    	for (SpanList s1 : annotations.keySet()) {
    	    Set<Ontology> onts = annotations.get(s1);
    	    for (Ontology ont : onts) {
    	    	if (ont instanceof ScoredUMLSConcept) {
    	    		ScoredUMLSConcept gpc = (ScoredUMLSConcept) ont;
    	    		log.info("UMLS Concept: "  + s1.toString() + "\t" + gpc.toString());
    	    	}
    		}    
    	}
        assertTrue( annotations.size() != 0 );
    }
    
}
