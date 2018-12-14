package gov.nih.nlm.semrep.preprocess;

import java.util.List;

import gov.nih.nlm.semrep.core.TokenInfo;

/**
 *  An interface for lemmatization. A lemmatizer is given a list of <code>TokenInfo</code> objects as input, where
 *  tokens and POS tags have been identified and lemma field is presumably empty and updates these objects accordingly. 
 * 
 * @author Halil Kilicoglu
 *
 */
public interface Lemmatization {
	
	/**
	 * Method to implement for lemmatization. <var>tokens</var> are expected to have POS tags.
	 * 
	 * @param tokens		Tokens to lemmatize.
	 */
    public void lemmatize( List<TokenInfo> tokens);

}
