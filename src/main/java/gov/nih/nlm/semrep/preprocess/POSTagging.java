package gov.nih.nlm.semrep.preprocess;

import java.util.List;

import gov.nih.nlm.semrep.core.TokenInfo;

/**
 *  An interface for part-of-speech tagging. A POS tagger is given a list of <code>TokenInfo</code> objects
 *  as input (where presumably the part-of-speech field is empty) and updates these objects. 
 * 
 * @author Halil Kilicoglu
 *
 */
public interface POSTagging {
	
   /**
    * Method to implement for POS tagging. 
    * 
    * @param tokens	Tokens to tag for POS.
    */
    public void tag( List<TokenInfo> tokens);

}
