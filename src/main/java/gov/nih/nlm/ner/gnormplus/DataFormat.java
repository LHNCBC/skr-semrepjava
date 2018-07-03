package gov.nih.nlm.ner.gnormplus;

public class DataFormat {
	String Data;
	String Location;
	String Output;

	public DataFormat(String d, String l) {
		this.Data = d;
		this.Location = l;
	}

	public DataFormat(String d, String l, String o) {
		this.Data = d;
		this.Location = l;
		this.Output = o;
	}
}
