package gov.nih.nlm.semrepjava.core;

import java.util.List;

import gov.nih.nlm.ling.core.Sentence;
import gov.nih.nlm.ling.core.Span;
import gov.nih.nlm.semrep.core.Chunk;

public class ChunkedSentence extends Sentence {

    List<Chunk> chunks;

    public ChunkedSentence(String id, String text, Span span) {
	super(id, text, span);
    }

    public ChunkedSentence(String id, String text, Span span, List<Chunk> chunks) {
	super(id, text, span);
	this.chunks = chunks;
    }

    public void setChunks(List<Chunk> chunks) {
	this.chunks = chunks;
    }

    public List<Chunk> getChunks() {
	return this.chunks;
    }

}
