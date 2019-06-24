package gov.nih.nlm.semrep.preprocess;

import java.io.Serializable;
import java.util.List;

import edu.stanford.nlp.util.CoreMap;

public class SentenceAnnsSO implements Serializable {

    List<CoreMap> sentenceAnns;

    public SentenceAnnsSO(List<CoreMap> sentenceAnns) {
	this.sentenceAnns = sentenceAnns;
    }

    public List<CoreMap> getSentenceAnns() {
	return sentenceAnns;
    }
}
