package gov.nih.nlm.semrepjava;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import gov.nih.nlm.ling.core.Document;
import gov.nih.nlm.ling.core.SpanList;
import gov.nih.nlm.ling.sem.Ontology;
import gov.nih.nlm.ner.metamap.MetaMapLiteClient;
import gov.nih.nlm.semrepjava.core.GNormPlusConcept;

public class MultiThreadClient {

    private int MserverPort;
    private String MserverName;
    private int GserverPort;
    private String GserverName;

    private Socket setGEnvironment(Properties props) {
	this.GserverPort = Integer.parseInt(props.getProperty("gserver.port"));
	this.GserverName = props.getProperty("gserver.name");
	// this.serverPort = 30000;
	//this.serverName = "indsrv2";

	try {
	    return new Socket(this.GserverName, this.GserverPort);
	} catch (UnknownHostException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return null;
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return null;
	}
    }

    private Socket setMEnvironment(Properties props) {
	this.MserverPort = Integer.parseInt(props.getProperty("mserver.port"));
	this.MserverName = props.getProperty("mserver.name");
	// this.serverPort = 30000;
	//this.serverName = "indsrv2";

	try {
	    return new Socket(this.MserverName, this.MserverPort);
	} catch (UnknownHostException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return null;
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return null;
	}
    }

    private String queryServer(Socket socket, String input) {
	StringBuilder sb = new StringBuilder();
	try {
	    // write text to the socket
	    DataInputStream bis = new DataInputStream(socket.getInputStream());
	    BufferedReader br = new BufferedReader(new InputStreamReader(bis));
	    PrintWriter bw = new PrintWriter(socket.getOutputStream(), true);
	    bw.println(input);
	    bw.flush();
	    String line = br.readLine();
	    do {
		//System.out.println(line);
		sb.append(line + "\n");
		line = br.readLine();
	    } while (line != null && line.length() > 0);
	    bis.close();
	    br.close();

	} catch (IOException ioe) {
	    System.err.println("Socket error");
	}
	return sb.toString();
    }

    /*public Map<SpanList, LinkedHashSet<Ontology>> annotate(final Document document, final Properties props) {

	// Socket s = setGEnvironment(props);
	// String inputText = document.getText();
	// Document doc = document;
	// final Properties pro = props;

	int threadNum = 2;
	Map<SpanList, LinkedHashSet<Ontology>> annotations = new HashMap<>();
	try {
	    ExecutorService executor = Executors.newFixedThreadPool(threadNum);
	    List<FutureTask<Map<SpanList, LinkedHashSet<Ontology>>>> taskList = new ArrayList<>();

	    FutureTask<Map<SpanList, LinkedHashSet<Ontology>>> futureTask_1 = new FutureTask<>(
		    new Callable<Map<SpanList, LinkedHashSet<Ontology>>>() {

			// @Override
			@Override
			public Map<SpanList, LinkedHashSet<Ontology>> call() {
			    final Map<SpanList, LinkedHashSet<Ontology>> anno = new HashMap<>();
			    new GNormPlusClient().annotate(document, props, anno);
			    return anno;
			}
		    });
	    taskList.add(futureTask_1);

	    FutureTask<Map<SpanList, LinkedHashSet<Ontology>>> futureTask_2 = new FutureTask<>(
		    new Callable<Map<SpanList, LinkedHashSet<Ontology>>>() {

			// @Override
			@Override
			public Map<SpanList, LinkedHashSet<Ontology>> call() {
			    final Map<SpanList, LinkedHashSet<Ontology>> anno = new HashMap<>();
			    new MetaMapLiteClient().annotate(document, props, anno);
			    return anno;
			}
		    });

	    taskList.add(futureTask_2);
	    executor.execute(futureTask_1);
	    executor.execute(futureTask_2);

	    FutureTask<Map<SpanList, LinkedHashSet<Ontology>>> futureTask_r1 = taskList.get(0);
	    annotations = futureTask_r1.get();

	    FutureTask<Map<SpanList, LinkedHashSet<Ontology>>> futureTask_r2 = taskList.get(1);
	    Map<SpanList, LinkedHashSet<Ontology>> secondMap = futureTask_r2.get();

	    Set<SpanList> set = secondMap.keySet();
	    for (SpanList s1 : set) {
		LinkedHashSet<Ontology> onts1 = annotations.get(s1);
		LinkedHashSet<Ontology> onts2 = secondMap.get(s1);
		if (onts1 == null) {
		    annotations.put(s1, onts2);
		} else {
		    onts1.addAll(onts2);
		}
	    }

	    executor.shutdown();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	// returnannos = annotations;
	return annotations;
    }

    public static void main(String[] args) throws IOException {
	Socket socket = new Socket("indsrv2", 30000);
	// Map<SpanList, LinkedHashSet<Ontology>> annotations = new HashMap<SpanList, LinkedHashSet<Ontology>>();
	String cit = new String(
		"Efficacy, safety, and tolerance of the non-ergoline dopamine agonist pramipexole in the treatment of advanced Parkinson's disease: a double blind, placebo controlled, randomised, multicent re study.");
	String cit2 = new String(
		"Effects of coenzyme Q10 in early Parkinson disease: evidence of slowing of the functional decline.\n");
	String cit3 = new String(
		"IMPORTANCE: Coenzyme Q10 (CoQ10), an antioxidant that supports mitochondrial function, has been shown in preclinical Parkinson disease (PD) models to reduce the loss of dopamine neurons, and was safe and well tolerated in early-phase human studies. A previous phase II study suggested possible clinical benefit. OBJECTIVE: To examine whether CoQ10 could slow disease progression in early PD. DESIGN, SETTING, AND PARTICIPANTS: A phase III randomized, placebo-controlled, double-blind clinical trial at 67 North American sites consisting of participants 30 years of age or older who received a diagnosis of PD within 5 years and who had the following inclusion criteria: the presence of a rest tremor, bradykinesia, and rigidity; a modified Hoehn and Yahr stage of 2.5 or less; and no anticipated need for dopaminergic therapy within 3 months. Exclusion criteria included the use of any PD medication within 60 days, the use of any symptomatic PD medication for more than 90 days, atypical or drug-induced parkinsonism, a Unified Parkinson's Disease Rating Scale (UPDRS) rest tremor score of 3 or greater for any limb, a Mini-Mental State Examination score of 25 or less, a history of stroke, the use of certain supplements, and substantial recent exposure to CoQ10. Of 696 participants screened, 78 were found to be ineligible, and 18 declined participation. INTERVENTIONS: The remaining 600 participants were randomly assigned to receive placebo, 1200 mg/d of CoQ10, or 2400 mg/d of CoQ10; all participants received 1200 IU/d of vitamin E. MAIN OUTCOMES AND MEASURES: Participants were observed for 16 months or until a disability requiring dopaminergic treatment. The prospectively defined primary outcome measure was the change in total UPDRS score (Parts I-III) from baseline to final visit. The study was powered to detect a 3-point difference between an active treatment and placebo. RESULTS: The baseline characteristics of the participants were well balanced, the mean age was 62.5 years, 66% of participants were male, and the mean baseline total UPDRS score was 22.7. A total of 267 participants required treatment (94 received placebo, 87 received 1200 mg/d of CoQ10, and 86 received 2400 mg/d of CoQ10), and 65 participants (29 who received placebo, 19 who received 1200 mg/d of CoQ10, and 17 who received 2400 mg/d of CoQ10) withdrew prematurely. Treatments were well tolerated with no safety concerns. The study was terminated after a prespecified futility criterion was reached. At study termination, both active treatment groups showed slight adverse trends relative to placebo. Adjusted mean changes (worsening) in total UPDRS scores from baseline to final visit were 6.9 points (placebo), 7.5 points (1200 mg/d of CoQ10; P = .49 relative to placebo), and 8.0 points (2400 mg/d of CoQ10; P = .21 relative to placebo). CONCLUSIONS AND RELEVANCE: Coenzyme Q10 was safe and well tolerated in this population, but showed no evidence of clinical benefit. TRIAL REGISTRATION: clinical");
	//"In a clinical trial that is still in progress, we studied the ability of deprenyl and tocopherol, antioxidative agents that act through complementary mechanisms, to delay the onset of disability necessitating levodopa therapy (the primary end point) in patients with early, untreated Parkinson's disease. Eight hundred subjects were randomly assigned in a two-by-two factorial design to receive deprenyl, tocopherol, a combination of both drugs, or placebo, and were followed up to determine the frequency of development of the end point. The interim results of independent monitoring prompted a preliminary comparison of the 401 subjects assigned to tocopherol or placebo with the 399 subjects assigned to deprenyl, alone or with tocopherol. Only 97 subjects who received deprenyl reached the end point during an average 12 months of follow-up, as compared with 176 subjects who did not receive deprenyl (P less than 10(-8). The risk of reaching the end point was reduced by 57 percent for the subjects who received deprenyl (Cox hazard ratio, 0.43; 95 percent confidence limits, 0.33 and 0.55; P less than 10(-10]. The subjects who received deprenyl also had a significant reduction in their risk of having to give up full-time employment (P = 0.01). We conclude from these preliminary results that the use of deprenyl (10 mg per day) delays the onset of disability associated with early, otherwise untreated cases of Parkinson's disease.");
	Document doc = new Document("1", cit3);
	Properties prop = new Properties();
	prop.setProperty("gserver.port", "30000");
	prop.setProperty("gserver.name", "indsrv2");
	prop.setProperty("mserver.port", "12345");
	prop.setProperty("mserver.name", "indsrv2");
	Map<SpanList, LinkedHashSet<Ontology>> annotations = new MultiThreadClient().annotate(doc, prop);
	// MultiThreadClient MTC = new MultiThreadClient();
	// MTC.annotate(doc, prop, annotations);
	Set<SpanList> set = annotations.keySet();
	for (SpanList s1 : set) {
	    Set<Ontology> onts = annotations.get(s1);
	    System.out.println(s1.toString());
	    System.out.print("\t");
	    for (Ontology ont : onts) {
		if (ont.getClass().equals(GNormPlusConcept.class)) {
		    GNormPlusConcept gpc = (GNormPlusConcept) ont;
		    System.out.print(" *** GNormPLus : " + gpc.toString());
		} else {
		    // Ontology gpc = (Concept) ont;
		    System.out.print(" --- MetaMap Lite : " + ont.toString());
		}
	    }
	    System.out.println();
	}

	// System.out.println(gnormplusout);
    }*/
}
