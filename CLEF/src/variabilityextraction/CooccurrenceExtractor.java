package variabilityextraction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import productdescription.ProductDescription;

/**
 * This class allows to extract co-occurences between features and/or attribute values, i.e., features and/or attribute values that always appear together.
 * Co-occurrent elements (i.e., features and attribute values) are introduced in the same concept in the concept lattice.
 * 
 * The extraction method is complete, i.e., it allows to extract all existing co-occurrences.
 *  
 * The set of co-occurrences can be saved in a text file named "cooccurrences.txt"
 * 
 * The support of each co-occurrence is computed and written next to it in the text file.
 * Co-occurrences are sorted depending on their support.
 * 
 * @author Jessie Carbonnel
 *
 */
public class CooccurrenceExtractor {
	
	/*
	 * ---------- ATTRIBUTES ----------
	 */
	
	/**
	 * List of extracted co-occurrences.
	 */
	private ArrayList<String> cooccurrences = new ArrayList<String>();
	
	/**
	 * List of concepts from the lattice.
	 */
	private ArrayList<String> concepts = new ArrayList<String>();
		
	/**
	 * path of the dot file used to extract co-occurrences.
	 */
	private String path;
	
	/**
	 * Product description entirely binary scaled
	 */
	private ProductDescription p;
	
	

	/*
	 * ---------- CONSTRUCTORS ----------
	 */
	
	/**
	 * Creates a new instance of co-occurrence extractor
	 * 
	 * @param path the path of the dot file used to extract co-occurrences
	 */
	public CooccurrenceExtractor(ProductDescription p, String path) {		
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
	public void extractsDataFromDotFile() {
		
		// Clears the lists
		
		this.concepts.clear();
		
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
            .map(c -> c.replaceAll("\\[[0-9]*\\|", "")) 
            .map(c -> c.replaceAll("\\|.*", ""))
            .map(c -> c.replace("\\n", ";"))
            .filter(c -> c.contains(";"))
            .forEach(c -> concepts.add(c.substring(0,c.length() - 1)));
			
		}		
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * This method uses the information extracted from the concepts of the lattice to detect co-occurences between attributes.
	 * It detects concepts introducing more than one attribute, and create a co-occurrence between each pair of attributes.
	 */
	public void computesCoOccurrences(){
	
		// Extracts the information about attribute concepts' intent
		
		this.extractsDataFromDotFile();
		
		// Clears the set of co-occurrences
		
		cooccurrences.clear();
		
		// For each concepts of the lattice
		
		for (String c : concepts) {
			
			// If the concept introduce more than one attribute
			
			if (c.contains(";")) {
				
				// Removes the concept number
				
				String attSet = c.substring(c.indexOf(" ") + 1,c.length());
				
				// For each pair of attribute
				
				for (String att1 : attSet.split(";")) {
					
					for (String att2 : attSet.split(";")) {
				
						// If the two attributes are different
						
						if (!att1.equals(att2)) {
				
							// Computes support
							double sup = 0;
							
							for (String prod : p.getProducts() ) {
								if (p.getValues(prod, att1).equals("X")) {
									sup++;
								}
							}
							
							if(sup != 0) {
								
								sup = sup / p.getProducts().size();
								
								// Avoids to add the same co-occurrence twice
								
								if (!cooccurrences.contains("(" + sup + ")\t" + att2 + " <-> "+ att1)) {
																		
									cooccurrences.add("(" + sup + ")\t" + att1 + " <-> "+ att2);
									
								} 
							} 
						}
					}
				}	
			}
		}
		
		// Displays the number of cooccurrences
		System.out.println("==================================================");
		System.out.println("|| Total number of cooccurrences: " + cooccurrences.size());
		System.out.println("==================================================");
	}
	
	/**
	 * This method exports all the extracted co-occurrences in a text file.
	 */
	public void exportsInTextFile() {
		assert(!cooccurrences.isEmpty());
		
		Collections.sort(cooccurrences, Collections.reverseOrder());

		String filename = path + "variability/cooccurrences.txt";
		
		try {
			BufferedWriter f_impl = new BufferedWriter(new FileWriter (filename));
			
			for (String impl : cooccurrences) {
				f_impl.write(impl); f_impl.newLine();
			}
			f_impl.close();
		//	System.out.println("Co-occurrence set (" + cooccurrences.size() + ") exported into " + filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void exportsInCsvFileForAnnotations(){
		
		assert(!cooccurrences.isEmpty());
		
		String filename = path + "variability/cooccurrences_annotations.csv";
		
		Collections.sort(cooccurrences, Collections.reverseOrder());
		
		try {
			BufferedWriter f_cooc = new BufferedWriter(new FileWriter (filename));
			
			f_cooc.write("support,pertinence,cooccurrence"); f_cooc.newLine();
			
			for (String cooc : cooccurrences) {
				
				String sup = cooc.substring(1, cooc.indexOf(")"));
				String cooc2 = cooc.substring(cooc.indexOf("\t"), cooc.length());
				
				f_cooc.write(sup + ",," + cooc2); f_cooc.newLine();
			}
			f_cooc.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
public void computePertinentCooccurrences() {
		
		String preoc = "General";
		
		ArrayList<String> infl = new ArrayList<>();
		ArrayList<String> pert = new ArrayList<>();
		pert.add("cooccurrence,pertinence,preoccupations");

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
		for (String imp : cooccurrences) {
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
	
		
		String filename = path + preoc + "_cooccurrences_annotated.csv";
		
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

}
