package gov.nih.nlm.semrep.preprocess;

import java.util.List;

import gov.nih.nlm.semrep.core.TokenInfo;

/**
 *  An interface for tokenization. A tokenizer is given a string as input and populates a list of 
 *  tokens passed as input.
 * 
 * @author Halil Kilicoglu
 *
 */
public interface Tokenization {
	
	/**
	 * The method to implement for tokenization.
	 * 
	 * @param input	Input string to tokenize
	 * @param tokens	 The list of tokens to populate
	 */
    public void tokenize(String input, List<TokenInfo> tokens);

}
