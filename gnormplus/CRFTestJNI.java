package gov.nih.nlm.ner.gnormplus;

public class CRFTestJNI {
    static {
	System.loadLibrary("crfpp");
    }

    public native String crftest(int nbest, String modelName, String data);

    public static void main(String[] args) {
	new CRFTestJNI();
    }
}
