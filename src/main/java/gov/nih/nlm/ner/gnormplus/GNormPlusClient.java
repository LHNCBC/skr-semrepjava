package gov.nih.nlm.ner.gnormplus;

import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.semrep.utils.SemRepUtils;

/**
 * Implementation of client for GNormPlus server.
 * 
 * @author Dongwook Shin
 * @author Halil Kilicoglu
 *
 */

public class GNormPlusClient {
    private static Logger log = Logger.getLogger(GNormPlusClient.class.getName());

    private String gnpServerName;
    private int gnpServerPort;

    public GNormPlusClient(Properties props) {
	this.gnpServerPort = Integer.parseInt(props.getProperty("gnormplus.server.port"));
	this.gnpServerName = props.getProperty("gnormplus.server.name");
    }

    /*
     * private Socket setEnvironment(Properties props) { this.GserverPort =
     * Integer.parseInt(props.getProperty("gserver.port")); this.GserverName =
     * props.getProperty("gserver.name"); // this.serverPort = 30000;
     * //this.serverName = "indsrv2";
     * 
     * try { return new Socket(this.GserverName, this.GserverPort); } catch
     * (UnknownHostException e) { // TODO Auto-generated catch block
     * e.printStackTrace(); return null; } catch (IOException e) { // TODO
     * Auto-generated catch block e.printStackTrace(); return null; } }
     */

    /*
     * private String queryServer(Socket socket, String input) { StringBuilder sb =
     * new StringBuilder(); try { // write text to the socket DataInputStream bis =
     * new DataInputStream(socket.getInputStream()); BufferedReader br = new
     * BufferedReader(new InputStreamReader(bis)); PrintWriter bw = new
     * PrintWriter(socket.getOutputStream(), true); bw.println(input); bw.flush();
     * String line = br.readLine(); do { //System.out.println(line); sb.append(line
     * + "\n"); line = br.readLine(); } while (line != null && line.length() > 0);
     * bis.close(); br.close();
     * 
     * } catch (IOException ioe) { System.err.println("Socket error"); } return
     * sb.toString(); }
     */

    /*- public Map<SpanList, LinkedHashSet<Ontology>> annotate(Document document, Properties props) {
    
    Map<SpanList, LinkedHashSet<Ontology>> annotations = new HashMap<SpanList, LinkedHashSet<Ontology>>();
    Socket s = setEnvironment(props);
    String inputText = document.getText();
    String answer = s == null ? null : queryServer(s, inputText);
    // System.out.println(answer);
    
    if (answer != null) {
    String[] entities = answer.split("\n");
    for (String entity : entities) {
    // System.out.println(entity);
    String compo[] = entity.split("\t");
    int start = Integer.parseInt(compo[0]);
    int end = Integer.parseInt(compo[1]);
    SpanList s1 = new SpanList(start, end);
    int geneId = Integer.parseInt(compo[4]);
    
    GNormPlusConcept gcon = new GNormPlusConcept(compo[2], compo[3], geneId);
    LinkedHashSet<Ontology> onts = annotations.get(s1);
    if (onts != null)
        onts.add(gcon);
    else {
        onts = new LinkedHashSet<Ontology>();
        onts.add(gcon);
        annotations.put(s1, onts);
    }
    
    }
    }
    
    return annotations;
    
    } */

    public void annotate(Document document, Properties props, Map<SpanList, LinkedHashSet<Ontology>> annotations) {
	Socket s = SemRepUtils.getSocket(gnpServerName, gnpServerPort);
	if (s == null)
	    return;
	long beg = System.currentTimeMillis();
	log.finest("Processing document " + document.getId() + " with GNormPlus..");
	String inputText = document.getText();
	String answer = SemRepUtils.queryServer(s, inputText);
	if (answer != null && answer.length() > 0) {
	    String[] entities = answer.split("\n");
	    for (String entity : entities) {
		// System.out.println(entity);
		String compo[] = entity.split("\t");
		int start = Integer.parseInt(compo[0]);
		int end = Integer.parseInt(compo[1]);
		SpanList s1 = new SpanList(start, end);
		//				int geneId = Integer.parseInt(compo[4]);

		GNormPlusConcept gcon = new GNormPlusConcept(compo[4], compo[2], compo[3]);
		LinkedHashSet<Ontology> onts = annotations.get(s1);
		if (onts != null)
		    onts.add(gcon);
		else {
		    onts = new LinkedHashSet<>();
		    onts.add(gcon);
		    annotations.put(s1, onts);
		}

	    }
	}
	long end = System.currentTimeMillis();
	log.info("Completed processing document with GNormPlus " + document.getId() + ".." + (end - beg) + " msec.");
	SemRepUtils.closeSocket(s);
    }

    // use unit test instead
    /*
     * public static void main(String[] args) throws IOException { Socket socket =
     * new Socket("indsrv2", 30000); // Map<SpanList, LinkedHashSet<Ontology>>
     * annotations = new HashMap<SpanList, LinkedHashSet<Ontology>>(); String cit =
     * new String(
     * "Efficacy, safety, and tolerance of the non-ergoline dopamine agonist pramipexole in the treatment of advanced Parkinson's disease: a double blind, placebo controlled, randomised, multicent re study."
     * ); String cit2 = new String(
     * "Effects of coenzyme Q10 in early Parkinson disease: evidence of slowing of the functional decline.\n"
     * ); String cit3 = new String(
     * "IMPORTANCE: Coenzyme Q10 (CoQ10), an antioxidant that supports mitochondrial function, has been shown in preclinical Parkinson disease (PD) models to reduce the loss of dopamine neurons, and was safe and well tolerated in early-phase human studies. A previous phase II study suggested possible clinical benefit. OBJECTIVE: To examine whether CoQ10 could slow disease progression in early PD. DESIGN, SETTING, AND PARTICIPANTS: A phase III randomized, placebo-controlled, double-blind clinical trial at 67 North American sites consisting of participants 30 years of age or older who received a diagnosis of PD within 5 years and who had the following inclusion criteria: the presence of a rest tremor, bradykinesia, and rigidity; a modified Hoehn and Yahr stage of 2.5 or less; and no anticipated need for dopaminergic therapy within 3 months. Exclusion criteria included the use of any PD medication within 60 days, the use of any symptomatic PD medication for more than 90 days, atypical or drug-induced parkinsonism, a Unified Parkinson's Disease Rating Scale (UPDRS) rest tremor score of 3 or greater for any limb, a Mini-Mental State Examination score of 25 or less, a history of stroke, the use of certain supplements, and substantial recent exposure to CoQ10. Of 696 participants screened, 78 were found to be ineligible, and 18 declined participation. INTERVENTIONS: The remaining 600 participants were randomly assigned to receive placebo, 1200 mg/d of CoQ10, or 2400 mg/d of CoQ10; all participants received 1200 IU/d of vitamin E. MAIN OUTCOMES AND MEASURES: Participants were observed for 16 months or until a disability requiring dopaminergic treatment. The prospectively defined primary outcome measure was the change in total UPDRS score (Parts I-III) from baseline to final visit. The study was powered to detect a 3-point difference between an active treatment and placebo. RESULTS: The baseline characteristics of the participants were well balanced, the mean age was 62.5 years, 66% of participants were male, and the mean baseline total UPDRS score was 22.7. A total of 267 participants required treatment (94 received placebo, 87 received 1200 mg/d of CoQ10, and 86 received 2400 mg/d of CoQ10), and 65 participants (29 who received placebo, 19 who received 1200 mg/d of CoQ10, and 17 who received 2400 mg/d of CoQ10) withdrew prematurely. Treatments were well tolerated with no safety concerns. The study was terminated after a prespecified futility criterion was reached. At study termination, both active treatment groups showed slight adverse trends relative to placebo. Adjusted mean changes (worsening) in total UPDRS scores from baseline to final visit were 6.9 points (placebo), 7.5 points (1200 mg/d of CoQ10; P = .49 relative to placebo), and 8.0 points (2400 mg/d of CoQ10; P = .21 relative to placebo). CONCLUSIONS AND RELEVANCE: Coenzyme Q10 was safe and well tolerated in this population, but showed no evidence of clinical benefit. TRIAL REGISTRATION: clinical"
     * );
     * //"In a clinical trial that is still in progress, we studied the ability of deprenyl and tocopherol, antioxidative agents that act through complementary mechanisms, to delay the onset of disability necessitating levodopa therapy (the primary end point) in patients with early, untreated Parkinson's disease. Eight hundred subjects were randomly assigned in a two-by-two factorial design to receive deprenyl, tocopherol, a combination of both drugs, or placebo, and were followed up to determine the frequency of development of the end point. The interim results of independent monitoring prompted a preliminary comparison of the 401 subjects assigned to tocopherol or placebo with the 399 subjects assigned to deprenyl, alone or with tocopherol. Only 97 subjects who received deprenyl reached the end point during an average 12 months of follow-up, as compared with 176 subjects who did not receive deprenyl (P less than 10(-8). The risk of reaching the end point was reduced by 57 percent for the subjects who received deprenyl (Cox hazard ratio, 0.43; 95 percent confidence limits, 0.33 and 0.55; P less than 10(-10]. The subjects who received deprenyl also had a significant reduction in their risk of having to give up full-time employment (P = 0.01). We conclude from these preliminary results that the use of deprenyl (10 mg per day) delays the onset of disability associated with early, otherwise untreated cases of Parkinson's disease."
     * ); Document doc = new Document("1", cit3); Properties prop = new
     * Properties(); prop.setProperty("gnormplus.server.port", "30000");
     * prop.setProperty("gnormplus.server.name", "indsrv2"); // Map<SpanList,
     * LinkedHashSet<Ontology>> annotations = new GNormPlusClient().annotate(doc,
     * prop); Map<SpanList, LinkedHashSet<Ontology>> annotations = new HashMap<>();
     * new GNormPlusClient(prop).annotate(doc, prop, annotations); Set<SpanList> set
     * = annotations.keySet(); for (SpanList s1 : set) { Set<Ontology> onts =
     * annotations.get(s1); for (Ontology ont : onts) { GNormPlusConcept gpc =
     * (GNormPlusConcept) ont; System.out.println(s1.toString() + " : " +
     * gpc.toString()); } } // System.out.println(gnormplusout); }
     */
}
