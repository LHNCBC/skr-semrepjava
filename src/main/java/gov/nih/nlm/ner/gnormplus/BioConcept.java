package gov.nih.nlm.ner.gnormplus;

import java.util.HashMap;
import java.util.Map;

public class BioConcept {
    int start;
    int last;
    String mention;
    Map<String, String> AnnoInfons = new HashMap<String, String>();

    public BioConcept(int s, int l, String m, Map<String, String> infon) {
	this.start = s;
	this.last = l;
	this.mention = m;
	this.AnnoInfons = infon;
    }
}
