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
 * This class allows to extract the transitive closure of all implications that hold in the initial dataset.
 * The extracted implications can be between two features, between two attribute values or between a feature and an attribute value.
 * The extraction method is complete, i.e., it allows to extract all existing implications.
 * 
 * The set of implications can be saved in a text file named "implications.txt"
 * 
 * Implications between two different values of the same attribute are not taken into account (represents the taxonomy).
 * 
 * Implications involved in co-occurrences are not extracted. 
 * 
 * The support of each implication is computed and written next to it in the text file.
 * Implications are sorted depending on their support.
 *  
 * @author Jessie Carbonnel
 *
 */
public class ImplicationExtractor {

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
	 * path of the dot file used to extract implications.
	 */
	private String path;
	
	/**
	 * Product description
	 */
	private ProductDescription p;
	
	/**
	 * Number of all implications of the transitive reduction, even the one not taken into account
	 */
	int nb_impl = 0;
	
	/*
	 * ---------- CONSTRUCTORS ----------
	 */
	
	/**
	 * Creates a new instance of implication extractor.
	 * 
	 * @param path the path of the dot file used to extract implications.
	 */
	public ImplicationExtractor(ProductDescription p, String path) {
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
            .map(c -> c.replaceAll("\\[[0-9]*\\|", "")) 
            .map(c -> c.replaceAll("\\|.*", ""))
            .map(c -> c.replace("\\n", ";"))
            .filter(c -> c.contains(";"))
            .forEach(c -> concepts.add(c));
			
			// Extracts sub-order
			// Each element of the list "suborder" represents an arrow in the concept lattice
			// i.e., a sub-order relationship between two concepts.
			// Each element is composed of the number of the sub-concept, an arrow "->", then the number of the super concept.
			// Ex: 5->23
			// The sub-order relationships can be seen as a binary implication graph.
			// As the lattice represents the transitive closure of these implications, the sub-order extracted here preserve this property.
			
			Files.lines(Paths.get(path + "FCA/step0-0.dot"))
            .map(line -> line.split("\\r\\n|\\n|\\r")) 		// Stream<String[]>
            .flatMap(Arrays::stream) 					// Stream<String>
            .distinct()
            .filter(c -> c.contains("->"))
            .map(c -> c.replace(":p", ""))
            .map(c -> c.replace("\t",""))
            .map(c -> c.replace(" ", ""))
            .forEach(c -> suborder.add(c));
	
		}		
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * This method uses the information extracted in the AraryLists "concepts" and "suborder" 
	 * to compute the transitive reduction of all the implications between binary attributes that holds in the concept lattice.
	 * Implications between two different values of the same attribute are not taken into account.
	 * Implications with a dissimilarity value "*" as a conclusion are not taken into account either.
	 * Implications involved in co-occurrences are not extracted.
	 */
	public void computesImplications() {
		
		// Extracts the information about attribute concepts' intent
		// and sub-order of the concept lattice from the dot file.
		
		this.extractsDataFromDotFile();
		
		
		// Clears the set of implications
		
		implications.clear();
		
		// For each sub-order relationship
		
		for(String s : suborder){
			
			// Retrieves the concept numbers of the premise and the conclusion
			
			String p_id = s.substring(0, s.indexOf("-"));
			String c_id = s.substring(s.indexOf(">") + 1, s.length());
			
			String pr = "";
			String cl = "";
					
			// Finds the intents of the premise and conclusion concepts 
			
			for (String c : concepts) {
				
				if (c.substring(0, c.indexOf(" ")).equals(p_id)) {
					pr = c.substring(p_id.length() + 1, c.length());
				}
				
				if (c.substring(0, c.indexOf(" ")).equals(c_id)) {
					cl = c.substring(c_id.length() + 1, c.length());;
				}
			}
			
			// As the intent of a concept can possess several binary attributes
			// We make sure to go through all values
			
			if (!pr.equals("") && !cl.equals("")) {
				
				for (String p_att : pr.split(";")) {
				
					for (String c_att : cl.split(";")) {
						
						// Count all the implications, even the ones not taken into account
						
						nb_impl ++;	
						int freq = 0;
						for (String prod : p.getProducts() ) {
							if (p.getValues(prod, p_att).equals("X")) {
								freq++;
							}
						}
						if(freq == 0) {
							nb_impl--;
						}
						
						// Remove implications between two values of the same attribute
						
						// If the two elements are attribute values
						if (p_att.contains(":") && c_att.contains(":")) {
							
							// If they are not values of the same attribute
							if (!p_att.substring(0,p_att.indexOf(":")).equals(c_att.substring(0,c_att.indexOf(":")))) {
								
								// If the premisse is not an attribute value of a co-occurring attribute value of the conclusion
								
								if (!cl.contains(p_att.substring(0,p_att.indexOf(":")))) {
									
									// If the conclusion is not an attribute value of a co-occurring attribute value of the premisse
									
									if (!pr.contains(c_att.substring(0,c_att.indexOf(":")))) {
										
										// Computes support
										double sup = 0;
										for (String prod : p.getProducts() ) {
											if (p.getValues(prod, p_att).equals("X")) {
												sup++;
											}
										}
										if (sup != 0) {
											sup = sup / p.getProducts().size();
											
											// And adds the implication
											
											implications.add("(" + sup + ")\t" + p_att+" -> "+c_att);
										}
									}
								}
								
							}
						} else if (p_att.contains(":")) {
							
							// If the premisse is an attribute value
							// If the premisse is not an attribute value of a co-occurring attribute value of the conclusion
							
							if (!cl.contains(p_att.substring(0,p_att.indexOf(":")))) {
								
								// Computes support
								double sup = 0;
								for (String prod : p.getProducts() ) {
									if (p.getValues(prod, p_att).equals("X")) {
										sup++;
									}
								}
								
								if (sup != 0) {
									sup = sup / p.getProducts().size();
									
									// And adds the implication
									
									implications.add("(" + sup + ")\t" + p_att+" -> "+c_att);
								}
							}
							
						} else if (c_att.contains(":")) {
							
							// If the conclusion is an attribute value
							// If the conclusion is not an attribute value of a co-occurring attribute value of the premisse
							
							if (!pr.contains(c_att.substring(0,c_att.indexOf(":")))) {
								
								// Computes support
								double sup = 0;
								for (String prod : p.getProducts() ) {
									if (p.getValues(prod, p_att).equals("X")) {
										sup++;
									}
								}
								
								if (sup != 0) {
									sup = sup / p.getProducts().size();
									
									// And adds the implication
									
									implications.add("(" + sup + ")\t" + p_att+" -> "+c_att);
								}
							}
							
						} else {
							// If the two elements are features 
							
							// Computes support
							double sup = 0;
							for (String prod : p.getProducts() ) {
								if (p.getValues(prod, p_att).equals("X")) {
									sup++;
								}
							}
							if (sup != 0) {
								sup = sup / p.getProducts().size();
								
								// And adds the implication
								
								implications.add("(" + sup + ")\t" + p_att+" -> "+c_att);
							}

							
						}
					}
				}
			}			
		}
		
		// Displays the number of implications
		System.out.println("==================================================");
		System.out.println("|| Total number of implications: " + implications.size() + "/" + nb_impl);
		System.out.println("==================================================");
	}
	
	/**
	 * This method exports all the extracted implications in a text file.
	 */
	public void exportsInTextFile() {
		assert(!implications.isEmpty());
		
		Collections.sort(implications, Collections.reverseOrder());
		
		String filename = path + "variability/implications.txt";
		
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
	
	/**
	 * This methods exports in a CSV file
	 * the extracted implications, with their support 
	 * to be later manually annotated.
	 */
	public void exportsInCsvFileForAnnotations(){
		
		assert(!implications.isEmpty());
		
		String filename = path + "variability/implications_annotations.csv";
		
		Collections.sort(implications, Collections.reverseOrder());
		
		try {
			BufferedWriter f_impl = new BufferedWriter(new FileWriter (filename));
			
			f_impl.write("support,pertinence,implication"); f_impl.newLine();
			
			for (String impl : implications) {
				
				String support = impl.substring(1, impl.indexOf(")"));
				String impl2 = impl.substring(impl.indexOf("\t"), impl.length());
				
				f_impl.write(support + ",," + impl2); f_impl.newLine();
			}
			
			f_impl.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
