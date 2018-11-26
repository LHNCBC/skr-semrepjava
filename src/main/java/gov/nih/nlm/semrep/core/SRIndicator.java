package gov.nih.nlm.semrep.core;

import gov.nih.nlm.ling.sem.Indicator;
import nu.xom.Element;

public class SRIndicator extends Indicator{

	private String type;
	
	public SRIndicator(Element el) {
		super(el);
		this.type = el.getAttributeValue("type");
	}
	
	public String getType() {
		return this.type;
	}
}
