package gov.nih.nlm.semrep.preprocess;

import java.util.List;

import gov.nih.nlm.semrep.core.SRSentence;
import gov.nih.nlm.semrep.core.TokenInfo;

/**
 *  An interface for chunking a sentence. A chunker is given a sentence and a list of <code>TokenInfo</code> objects 
 *  extracted from that sentence. The <code>TokenInfo</code> objects are expected to have token and POS tags. 
 *  The implementation is expected to associate the resulting chunks with the sentence. 
 * 
 * @author Halil Kilicoglu
 *
 */
public interface Chunking {
	
	/**
	 * Method to implement for chunking a sentence. <var>tokens</var> are expected to have POS tags. 
	 * Chunking result should be associated with the sentence.
	 * 
	 * @param sentence		The sentence to chunk
	 * @param tokens		The corresponding token list to chunk
	 */
    public void chunk(SRSentence sentence, List<TokenInfo> tokens);

}
