package variabilityextraction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import productdescription.ProductComparisonMatrix;
import productdescription.ProductDescription;

public class MutexExtractor {

	/*
	 * ---------- ATTRIBUTES ----------
	 */
	
	/**
	 * List of extracted mutex.
	 */
	private ArrayList<String> mutex = new ArrayList<String>();
	
	/**
	 * List of filtered extracted mutex.
	 */
	private ArrayList<String> filteredMutex = new ArrayList<String>();
	
	/**
	 * Product description, entirely binary scaled, from which mutex have to be extracted.
	 */
	private ProductDescription p;
	
	/**
	 * Path where to save the file containing the extracted mutex.
	 */
	private String path;
	
	/*
	 * ---------- CONSTRUCTORS ----------
	 */
	
	/**
	 * Creates a new instance of mutex extractor
	 * 
	 * @param path the path to the scaled product description from which the mutex have to be extracted.
	 */
	public MutexExtractor(ProductDescription p, String path) {
		
		this.p = p;
		
		this.path = path;
		
		
		
	}
	
	/*
	 * ---------- METHODS ----------
	 */
	
	/**
	 * Computes mutex between 2 features, a feature and an attribute value, and 2 attribute values.
	 * Do not try to compute mutex between values of the same attribute.
	 * The mutex are computed from the PCM entirely binary scaled.
	 */
	public void computesMutex(){
		
		// Clears the list of mutex
		
		mutex.clear();
		
		// For each pair of characteristics
		
		for (int i = 0 ; i < p.getCharacteristics().size()-1 ; i++) {
			
			for (int j = i+1 ; j < p.getCharacteristics().size() ; j++) {
		
				// Retrieves the name of the 2 characteristics
				
				String c1 = p.getCharacteristics().get(i);
				String c2 = p.getCharacteristics().get(j);
				
				// If the 2 characteristics are attribute values
				
				if (c1.contains(":") && c2.contains(":")) {
					
					// If they are values of different attributes
					
					if (!c1.substring(0, c1.indexOf(":")).equals(c2.substring(0, c2.indexOf(":")))) {
						
						boolean isMutex = true;
						int k = 0;
						
						// Find out if they are shared by the same product
						
						while (k < p.getProducts().size() && isMutex) {
							
							if (p.getValues(p.getProducts().get(k), c1).equals("X") &&
									p.getValues(p.getProducts().get(k), c2).equals("X")) {
								
								isMutex = false;
							}
							k++;
						}
						
						// If not, adds them as mutex
						
						if (isMutex) {
							
							// Computes support
							
							double sup1 = 0;
							double sup2 = 0;
							for (String prod : p.getProducts()) {
								if (p.getValues(prod, c1).equals("X")) {
									sup1 ++;
								}
								if (p.getValues(prod, c2).equals("X")) {
									sup2 ++;
								}
							}
							
							if (Math.min(sup1,sup2) != 0) {
								sup1 = Math.min(sup1,sup2) / p.getProducts().size();
								
								mutex.add("(" + sup1 + ")\t" + c1+" ->! "+c2);
							}
							
							
							
							
						}
					}
				} else  {
					
					// Else, if the two characteristics are not two attribute values
					
					boolean isMutex = true;
					int k = 0;
					
					// Find out if they are shared by the same product
					
					while (k < p.getProducts().size() && isMutex) {
						
						if (p.getValues(p.getProducts().get(k), c1).equals("X") &&
								p.getValues(p.getProducts().get(k), c2).equals("X")) {
							
							isMutex = false;
						}
						k++;
					}
					
					// If not, adds them as mutex
					
					if (isMutex) {
						
						// Computes support
						
						double sup1 = 0;
						double sup2 = 0;
						for (String prod : p.getProducts()) {
							if (p.getValues(prod, c1).equals("X")) {
								sup1 ++;
							}
							if (p.getValues(prod, c2).equals("X")) {
								sup2 ++;
							}
						}
					
						if (Math.min(sup1,sup2) != 0) {
							
							sup1 = Math.min(sup1,sup2) / p.getProducts().size();
							
							mutex.add("(" + sup1 + ")\t" + c1+" ->! "+c2);
						}
									
					}
				}
				
			}
		}
		
		filtersMutex();
	}
	
	/**
	 * This method filters mutex to keep the most general ones.
	 * 
	 * @return a subset of mutex after filtering.
	 */
	public void filtersMutex() {
			
		// Cleans the list or filtered mutex
		
		this.filteredMutex.clear();
		
		// Will contains the unnecessary mutex
		
		ArrayList<String> mutexToRemove = new ArrayList<>();
		
		
		// For each attribute
		
		for (String att : p.getAttributes()) {
			
		//	System.out.println("----------\n-- Processing attribute " + att + "\n----------\n");
			
			// We retrieve the mutex involving the current attribute in mutexInvolvingAtt
			
			ArrayList<String> mutexInvolvingAtt = new ArrayList<>();
			
			for (String m : this.mutex) {
				
				if (m.contains(att)) {
					
					mutexInvolvingAtt.add(m);
				}
			}
			
			// Keeps the processed 2nd value to not process it 2 times
			
			ArrayList<String> processed2ndValue = new ArrayList<>();
			
			// For each mutex involving the current attribute
			
			for (int i = 0 ; i < mutexInvolvingAtt.size() ; i++ ) {
				
				// Retrieves the 2nd value in the mutex
				
				String val = mutexInvolvingAtt.get(i);
				
				val = val.substring(val.indexOf("\t") + 1, val.length());
				
				if (val.startsWith(att)) {
					val = val.substring(val.indexOf("!") + 2, val.length());
				} else {
					val = val.substring(0, val.indexOf("!") - 3);
				}
				
				//System.out.println("[o] 2nd value is "+val);
				
				
				// Checks if the 2nd value has not been already processed for this attribute
				
				if (processed2ndValue.contains(val)) {
					
				//	System.out.println(" ------- > already processed");
					
				} else {
					
					processed2ndValue.add(val);
					
					// For each other mutex involving the current attribute
					// Retrieves all mutex involving val as second value
					
					ArrayList<String> mutexInvolvingVal = new ArrayList<>();
					mutexInvolvingVal.add(mutexInvolvingAtt.get(i));
					
					for (int j = i + 1 ; j < mutexInvolvingAtt.size() ; j++) {
						
						if (mutexInvolvingAtt.get(j).contains(val)) {
							mutexInvolvingVal.add( mutexInvolvingAtt.get(j) );
							
						}
					}
			//		System.out.println(" All mutex involving the 2nd value : " + mutexInvolvingVal);
					
					
					// If there are more than one mutex involving the attribute and the same 2nd value
					// Maybe we can remove unnecessary mutex, based on the attribute value
					// We compare attribute value of the mutex 2 by 2, and if one is more specialised than the other, we remove the first one.
					
					if (mutexInvolvingVal.size() > 1) {
				//		System.out.println(" [TEST]");
						
						for (int k = 0 ; k < mutexInvolvingVal.size() ; k++) {
							
							String att1 = mutexInvolvingVal.get(k);
							
							// Retrieves attribute value of the first mutex
							
							att1 = att1.substring(att1.indexOf("\t") + 1, att1.length());
							if (att1.startsWith(att)) {
								att1 = att1.substring(0, att1.indexOf("!") - 3);
							} else {
								att1 = att1.substring(att1.indexOf("!") + 2, att1.length());
							}
							
							// Retrieves objects having this attribute value
							
							ArrayList<String> alphaAtt1 = new ArrayList<>();
							
							for (String o : p.getProducts()) {
								
								if (p.getValues(o, att1).equals("X")) {
									alphaAtt1.add(o);
								}
							}
							
							for (int l = k + 1 ; l < mutexInvolvingVal.size() ; l++) {
								
								String att2 = mutexInvolvingVal.get(l);
								
								// Retrieves attribute value of the second mutex
								
								att2 = att2.substring(att2.indexOf("\t") + 1, att2.length());
								if (att2.startsWith(att)) {
									att2 = att2.substring(0, att2.indexOf("!") - 3);
								} else {
									att2 = att2.substring(att2.indexOf("!") + 2, att2.length());
								}
								
								// Retrieves objects having this attribute value
								
								ArrayList<String> alphaAtt2 = new ArrayList<>();
								
								for (String o : p.getProducts()) {
									
									if (p.getValues(o, att2).equals("X")) {
										alphaAtt2.add(o);
									}
								}
								
								// COMPARISON
								
								
								if (alphaAtt1.containsAll(alphaAtt2)) {
									
									// Case where att1 is more general
									// sup mutex having att2
									mutexToRemove.add(mutexInvolvingVal.get(l));
						//			System.out.println(" Add to the mutex to be removed : " + mutexInvolvingVal.get(l));
									
								} else if (alphaAtt2.containsAll(alphaAtt1)) {
									
									// Case where att2 is more general
									// sup mutex having att1
									mutexToRemove.add(mutexInvolvingVal.get(k));
							//		System.out.println(" Add to the mutex to be removed : " + mutexInvolvingVal.get(k));
								}
								// Else, go on							
								
							}
							
						}
					}
					
				}
			}
		
		}
		
		filteredMutex.addAll(mutex);
		filteredMutex.removeAll(mutexToRemove);
		
		// Displays the number of mutex
		System.out.println("==================================================");
		System.out.println("|| Total number of mutex: " + filteredMutex.size() + "/" + mutex.size() + " (without mutex between values of the same attribute)");
		System.out.println("==================================================");
		
	}
	
	
	/**
	 * This method exports all the extracted mutex in a text file.
	 */
	public void exportsInTextFile() {
		assert(!filteredMutex.isEmpty());
		
		Collections.sort(filteredMutex, Collections.reverseOrder());

		
		String filename = this.path + "variability/mutex.txt";
		
		try {
			BufferedWriter f_impl = new BufferedWriter(new FileWriter (filename));
			
			for (String impl : filteredMutex) {
				f_impl.write(impl); f_impl.newLine();
			}
			
			f_impl.close();
			System.out.println("(filtered) Mutex set exported into " + filename);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void exportsInCsvFileForAnnotations(){
		
		assert(!filteredMutex.isEmpty());
		
		String filename = path + "variability/filteredMutex_annotations.csv";
				
		Collections.sort(filteredMutex, Collections.reverseOrder());
		
		try {
			BufferedWriter f_mutex = new BufferedWriter(new FileWriter (filename));
			
			f_mutex.write("support,pertinence,mutex"); f_mutex.newLine();

			
			for (String mut : filteredMutex) {
				
				String sup = mut.substring(1, mut.indexOf("\t") - 1 );
				String mut2 = mut.substring(mut.indexOf("\t"), mut.length());
				
				f_mutex.write(sup + ",," + mut2); f_mutex.newLine();
			}
			
			f_mutex.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void computePertinentMutex() {
		
		String preoc = "General";
		
		ArrayList<String> infl = new ArrayList<>();
		ArrayList<String> pert = new ArrayList<>();
		pert.add("mutex,pertinence,preoccupations");

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
		for (String imp : filteredMutex) {
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
	
		
		String filename = path + preoc + "_mutex_annotated.csv";
		
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
