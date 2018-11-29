package gov.nih.nlm.semrep.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

import gov.nih.nlm.ling.sem.Indicator;
import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class SRIndicator extends Indicator{

	private static Logger log = Logger.getLogger(SRIndicator.class.getName());
	private String type;
	
	public SRIndicator(Element el) {
		super(el);
		this.type = el.getAttributeValue("type");
	}
	
	public String getType() {
		return this.type;
	}
	
	/**
	 * Loads indicators from an XML file. The indicators can be filtered by corpus frequency,
	 * if specified in the XML file. 
	 * 
	 * @param fileName  the indicator XML file
	 * @param count  	the corpus frequency threshold
	 * @return 			the set of indicators in the XML file
	 * 
	 * @throws FileNotFoundException	if the XML file cannot be found
	 * @throws IOException				if the XML file cannot be read
	 * @throws ParsingException			if the XML cannot be parsed
	 * @throws ValidityException		if the XML is invalid
	 */
	public static LinkedHashSet<Indicator> loadSRIndicatorsFromFile(String fileName, int count) 
			throws FileNotFoundException, IOException, ParsingException, ValidityException {
		LinkedHashSet<Indicator> indicators = new LinkedHashSet<Indicator>();
		Builder builder = new Builder();
		log.info("Loading indicator file " + fileName);
		nu.xom.Document xmlDoc = builder.build(new FileInputStream(fileName));
		Element docc = xmlDoc.getRootElement();
		Elements indEls = docc.getChildElements("SRIndicator");
		for (int i=0; i < indEls.size(); i++) {
			Element ind = indEls.get(i);
			if (ind.getAttribute("corpusCount") != null) {
				int corpusCount = Integer.parseInt(ind.getAttributeValue("corpusCount"));
				if (corpusCount < count) continue;
			}
			SRIndicator indicator = new SRIndicator(ind);
			indicators.add(indicator);
		}
		log.info("The number of all indicators loaded from the file " + fileName + " is " + indicators.size() + ".");
		return indicators;		
	}
}
