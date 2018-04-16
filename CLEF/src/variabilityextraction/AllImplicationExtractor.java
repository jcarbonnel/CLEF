package variabilityextraction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import productdescription.ProductDescription;

public class AllImplicationExtractor {

	/*
	 * ---------- ATTRIBUTES ----------
	 */
	
	/**
	 * List of extracted implications.
	 */
	private ArrayList<String> implications = new ArrayList<String>();
	
	/**
	 * List of concepts from the lattice.
	 */
	private ArrayList<String> concepts = new ArrayList<String>();
	
	/**
	 * List of suborder relations from the lattice.
	 */
	private ArrayList<String> suborder = new ArrayList<String>();
	
	/**
	 * Complete suborder relation
	 */
	private ArrayList<HashSet<String>> completeSuborder = new ArrayList<>();
		
	/**
	 * path of the dot file used to extract implications.
	 */
	private String path;
	
	/**
	 * Product description
	 */
	private ProductDescription p;
	
	/**
	 * Number of all implications, even the ones not taken into account
	 */
	int nb_impl = 0;
	
	
	/*
	 * ------------ CONSTRUCTORS ----------
	 */
	/**
	 * Creates a new instance of (all) implication extractor.
	 * 
	 * @param path the path of the dot file used to extract implications.
	 * @param p the binary scaled product description
	 */
	public AllImplicationExtractor(ProductDescription p, String path) {
		this.path = path;
		this.p = p;
	}
	
	/*
	 * ---------- METHODS ----------
	 */

	/**
	 * This method goes through the dot file created by the tool RCAExplore
	 * and extracts the information concerning the attribute concepts (intent and id) in the ArrayList "concepts"
	 * as well as the information concerning the sub-order in the ArrayList "suborder"
	 */
	private void extractsDataFromDotFile() {
		
		// Clears the lists
		
		this.concepts.clear();
		this.suborder.clear();
		
		try{
			
			// Extracts concept attributes' intent and their number in the dot file
			// Each element of the list "concepts" is composed of the number of the concept
			// Followed by a with space and then the list of elements from the intent separated by semicolons.
			// Ex: "5 elt1;elt2;elt3"
			
			Files.lines(Paths.get(path + "FCA/step0-0.dot"))
            .map(line -> line.split("\\r\\n|\\n|\\r")) 		// Stream<String[]>
            .flatMap(Arrays::stream) 					// Stream<String>
            .distinct()
            .filter(c -> !c.contains("->") && c.matches("^[0-9].*$"))
            .map(c -> c.replace("</td></tr><tr><td>", "|"))
            .map(c-> c.replace("<br/>", "\\n"))
            .map(c -> c.replace("shape=none,label=<<table border=\"0\" cellborder=\"1\" cellspacing=\"0\" port=\"p\"><tr><td>Concept_ctx_", ""))
            .map(c -> c.replace("</td></tr></table>>", ""))
            .map(c -> c.replace("|\\n", "|"))
            // more or less the good format
            .map(c -> c.replace("\\n", ";"))
            .map(c -> c.replaceAll("[0-9]+.\\[", "")) 
            .map(c -> c.replaceAll(";\\|.*\\]", ""))
            .map(c -> c.replaceAll("\\|\\|.*", " "))
            .map(c -> c.replaceAll("\\|", " "))
            .map(c -> c.replaceAll(" ];$", ""))
            .forEach(c -> concepts.add(c));
			
			// Sorts the concepts depending on their number
			
			Collections.sort(concepts, new Comparator<String>() {
				public int compare(String s1, String s2) {
					return Integer.parseInt(s1.substring(0,s1.indexOf(" "))) - Integer.parseInt(s2.substring(0,s2.indexOf(" ")));
				}
			});
			
		
			
			// The map named "corr" represents the correspondence between the real number of the concepts
			// And their number in the dot file
					
			HashMap<String, String> corr = new HashMap<>();			
			
			Files.lines(Paths.get(path + "FCA/step0-0.dot"))
            .map(line -> line.split("\\r\\n|\\n|\\r")) 		// Stream<String[]>
            .flatMap(Arrays::stream) 					// Stream<String>
            .distinct()
            .filter(c -> !c.contains("->") && c.matches("^[0-9].*$"))
            .map(c -> c.replace("</td></tr><tr><td>", "|"))
            .map(c-> c.replace("<br/>", "\\n"))
            .map(c -> c.replace("shape=none,label=<<table border=\"0\" cellborder=\"1\" cellspacing=\"0\" port=\"p\"><tr><td>Concept_ctx_", ""))
            .map(c -> c.replace("</td></tr></table>>", ""))
            .map(c -> c.replace("|\\n", "|"))
            // more or less the good format
            .map(c -> c.replace("\\n", ";"))
            .map(c -> c.replaceAll("\\[", "")) 
            .map(c -> c.replaceAll(";\\|.*\\]", ""))
            .map(c -> c.replaceAll("\\|.*", ""))
            .forEach(c -> corr.put(c.substring(0,c.indexOf(" ")), c.substring(c.indexOf(" ") + 1, c.length())));
			
			// Extracts sub-order
			// Each element of the list 'suborder" represents an arrow in the concept lattice
			// i.e., a suborder relationship between two concepts.
			// Each element is composed of the number of the sub-concept, an arrow "->", then the number of the super concept.
			// Ex: 5->23
			
			Files.lines(Paths.get(path + "FCA/step0-0.dot"))
            .map(line -> line.split("\\r\\n|\\n|\\r")) 		// Stream<String[]>
            .flatMap(Arrays::stream) 					// Stream<String>
            .distinct()
            .filter(c -> c.contains("->"))
            .map(c -> c.replace(":p", ""))
            .map(c -> c.replace("\t",""))
            .map(c -> c.replace(" ", ""))
            .forEach(c -> suborder.add(c));
			
			System.out.println("sub before corr " + suborder);
			ArrayList<String> suborder2 = new ArrayList<>();
			
			// Switches the number of the concepts in the dot file
			// with their real number in the lattice
			for (String s : suborder) {
				String p = s.substring(0,s.indexOf("-"));
				String c = s.substring(s.indexOf(">") + 1, s.length());
				
				suborder2.add(corr.get(p) + "->" + corr.get(c));
			}
			
			suborder.clear();
			suborder.addAll(suborder2);
				
		}		
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	private void computeCompleteSuborder() {
		
		System.out.println("Compute the complete suborder ---");
		
		// for each "p - > c" in suborder
		// places "c p" in suborder2
		// c is the super-concept of p
		ArrayList<String> suborder2 = new ArrayList<>();
		
		for (String s : suborder) {
			String p_id = s.substring(0, s.indexOf("-"));
			String c_id = s.substring(s.indexOf(">") + 1, s.length());
			suborder2.add(c_id + " " + p_id);
		}
		
		Collections.sort(suborder2, new Comparator<String>() {
		    public int compare(String o1, String o2) {
		        return Integer.parseInt(o1.substring(0,o1.indexOf(" "))) - Integer.parseInt(o2.substring(0,o2.indexOf(" ")));
		    }
		});
		
		// Transitive reduction in completeSuborder at first
		int id = 0;
		for (int i = 0 ; i < concepts.size() ; i++) {
			
			completeSuborder.add(new HashSet<String>());
			while (i < concepts.size() && id < suborder2.size() && suborder2.get(id).substring(0, suborder2.get(id).indexOf(" ")).equals(i+"")) {
				
				completeSuborder.get(i).add(suborder2.get(id).substring(suborder2.get(id).indexOf(" ") + 1, suborder2.get(id).length()));
				id++;
			}
		}
			

		// Propagation
		
		for (int i = 0 ; i < concepts.size() ; i++) {
			
			HashSet<String> toBeAdded = new HashSet<String>();
			for (String e : completeSuborder.get(i)) {
				toBeAdded.addAll(completeSuborder.get(Integer.parseInt(e)));
			}
			completeSuborder.get(i).addAll(toBeAdded);
			toBeAdded.clear();
		}
	}
	
	/**
	 * This method computes all implications that holds in the pattern structure representing the PCM.
	 * It computes the total number of possible implications,
	 * But keeps only the ones that are not redundant.
	 */
	public void extractAllImplications() {
		
		extractsDataFromDotFile();
		
		computeCompleteSuborder();
		
		// Clears the set of implications
		implications.clear();
		
		int totalNumberImpl = 0;
		for (int i = 1 ; i < concepts.size() ; i++) {
			
			// Current concept without its number
			// Just the intent
			String superConcept = concepts.get(i).substring(concepts.get(i).indexOf(" ") + 1, concepts.get(i).length());
			
			// For each sub concept of the current concept
			for (String sousConcept : completeSuborder.get(i)) {
				
				// Removes the concept number
				sousConcept = concepts.get(Integer.parseInt(sousConcept));
				if (!sousConcept.substring(0, sousConcept.indexOf(" ")).equals("0")) {
					sousConcept = sousConcept.substring(sousConcept.indexOf(" ") + 1, sousConcept.length());
					
					// If the two concepts are not plain
					if (!superConcept.equals("") && !sousConcept.equals("")) {
										
						for (String elt_p : sousConcept.split(";")) {
							
							for (String elt_c : superConcept.split(";")) {
								totalNumberImpl ++;
								
								/**
								 * Filtering implications to avoid redundancy
								 */
								
								// If the two elements are attribute values
								if (elt_p.contains(":") && elt_c.contains(":")) {
									
									// If they are not values of the same attribute
									if (!elt_p.substring(0,elt_p.indexOf(":")).equals(elt_c.substring(0,elt_c.indexOf(":")))) {
										
										// If the premisse is not an attribute value of a co-occurring attribute value of the conclusion
										
										if (!superConcept.contains(elt_p.substring(0,elt_p.indexOf(":")))) {
											
											// If the conclusion is not an attribute value of a co-occurring attribute value of the premisse
											
											if (!sousConcept.contains(elt_c.substring(0,elt_c.indexOf(":")))) {
												
												// Adds the implication
												implications.add(elt_p + " -> " + elt_c);
												
											}
										}
										
									}
								} else if (elt_p.contains(":")) { // If the premise is an attribute value
									
									
									// If the premise is not an attribute value of a co-occurring attribute value of the conclusion
									if (!superConcept.contains(elt_p.substring(0,elt_p.indexOf(":")))) {

										// Adds the implication
										implications.add(elt_p + " -> " + elt_c);
									}
									
								} else if (elt_c.contains(":")) { // If the conclusion is an attribute value
									
									
									// If the conclusion is not an attribute value of a co-occurring attribute value of the premisse
									if (!sousConcept.contains(elt_c.substring(0,elt_c.indexOf(":")))) {
										
										// Adds the implication
										implications.add(elt_p + " -> " + elt_c);
									}
									
								} else { // If the two elements are features 
									
									// Adds the implication
									implications.add(elt_p + " -> " + elt_c);
								}
							}
						}
					}
				}
				
			}
		}
		
		System.out.println("-----------------------------------------------");
		System.out.println("All implications without trans. red.");
		System.out.println("(red) " + implications.size() + "/ (all) " + totalNumberImpl);
		System.out.println("-----------------------------------------------");
		
	}
	
	
	
	private void computePertinentImplications() {
		
		String preoc = "General";
		
		ArrayList<String> infl = new ArrayList<>();
		ArrayList<String> pert = new ArrayList<>();
		pert.add("implications,pertinence,preoccupations");

		try {
			
			Files.lines(Paths.get(path + preoc + "_graph.txt"))
	        .map(line -> line.split("\\r\\n|\\n|\\r")) 		// Stream<String[]>
	        .flatMap(Arrays::stream) 					// Stream<String>
	        .distinct()
	        .forEach(c -> infl.add(c));
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}

			
		//For each filtered implications
		for (String imp : implications) {
			boolean isPert = false;
			
			for (String i : infl) {
				
				String elt1 = i.substring(0,i.indexOf(";"));
				i = i.substring(i.indexOf(";") + 1, i.length());
				String elt2 = i.substring(0,i.indexOf(";"));
				i = i.substring(i.indexOf(";") + 1, i.length());
				String degres = i;
				
						
				if (imp.contains(elt1) && imp.contains(elt2)) {
					
					pert.add(imp + "," + (degres.equals("1") ? "maybe" : "pertinent") + "," + preoc);
					isPert = true;
				} 
			}
			if (!isPert) {
				pert.add(imp + "," + "not pertinent" + "," + preoc);
			}
		}
	
		
		String filename = path + preoc + "_implications_annotated.csv";
		
		try {
			BufferedWriter f_impl = new BufferedWriter(new FileWriter (filename));
			
			for (String impl : pert) {
				f_impl.write(impl); f_impl.newLine();
			}
			f_impl.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This method exports all the extracted implications in a text file.
	 */
	public void exportsInTextFile() {
		assert(!implications.isEmpty());
		
		Collections.sort(implications, Collections.reverseOrder());
		
		String filename = path + "variability/Allimplications.txt";
		
		try {
			BufferedWriter f_impl = new BufferedWriter(new FileWriter (filename));
			
			for (String impl : implications) {
				f_impl.write(impl); f_impl.newLine();
			}
			f_impl.close();
			System.out.println("Implication set (" + implications.size() +") exported into " + filename);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
