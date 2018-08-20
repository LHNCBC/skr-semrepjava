package gov.nih.nlm.ner.gnormplus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import bioc.BioCCollection;
import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.io.BioCCollectionWriter;
import bioc.io.standard.BioCFactoryImpl;
import gov.nih.nlm.ling.core.Span;

public class BioCConverter {

	public BioCConverter() {
		// TODO Auto-generated constructor stub

	}
	
	//// !!! NOT GENERAL, ASSUMES TITLE/ABSTRACT separated by a newline
	private static List<Span> getSectionSpans(String text) {
		List<Span> spans = new ArrayList<Span>();
		String title = text.substring(0,text.indexOf("\n"));
		spans.add(new Span(0,title.length()));
		spans.add(new Span(title.length()+1,text.length()));
		return spans;
	}
	
	public static BioCDocument convert(String text) {
		BioCDocument document = new BioCDocument();
		document.setID("TMP" + text.hashCode());
		List<Span> sectSpans = getSectionSpans(text);
		for (Span sectSpan: sectSpans) {
			BioCPassage passage = new BioCPassage();
			passage.setOffset(sectSpan.getBegin());
			if (sectSpan.getBegin() == 0) passage.getInfons().put("type", "title");
			else passage.getInfons().put("type", "abstract");
			// without trim at the end, I ran into a lot of problems. 
			// Need to be careful
			passage.setText(text.substring(sectSpan.getBegin(),sectSpan.getEnd()).trim());
			document.addPassage(passage);
		}
		return document;
	}
	
	public static void convertAndWrite(String text, String outFile) throws FileNotFoundException,IOException,XMLStreamException {
		BioCFactoryImpl f = new BioCFactoryImpl();
		BioCCollectionWriter w = f.createBioCCollectionWriter(new PrintWriter(outFile));
		BioCCollection c = new BioCCollection();
		c.setSource("PubTator");
		c.setDate("08/03/2017");
		c.setKey("PubTator.key");
		c.addDocument(convert(text));
		w.writeCollection(c);
		w.close();
	}

}
