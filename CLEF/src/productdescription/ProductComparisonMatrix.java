package productdescription;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import similarityoperator.*;
import util.RcftExporter;
import variabilityextraction.AllImplicationExtractor;
import variabilityextraction.CooccurrenceExtractor;
import variabilityextraction.ImplicationExtractor;
import variabilityextraction.MutexExtractor;

/**
 * This class represents product descriptions in the form of product comparison matrices (PCMs).
 * We assume that the handled PCMs are cleaned as defined in the documentation.
 * 
 * @author Jessie Carbonnel
 *
 */
public class ProductComparisonMatrix extends ProductDescription {
	
	/*
	 * ------- ATTRIBUTES ------
	 */
	
	/**
	 * Name of the processed PCM.
	 */
	private String PCM_name;

	/**
	 * Directory containing the produced files.
	 */
	private String PCM_dir;
	
	/**
	 * A list of lists representing the values for each characteristic for a product.
	 * The index of the list corresponds to the product of the same index in the list productNames.
	 */
	private ArrayList<ArrayList<String>> products = new ArrayList<ArrayList<String>>();
	
	/**
	 * Each list represents the set of distinct values for a characteristic.
	 * The index of the list of values corresponds to the characteristic of the same index in the list characteristics.
	 */
	private ArrayList<ArrayList<String>> distinctValues = new ArrayList<ArrayList<String>>();
	
	/**
	 * List of the products' names.
	 */
	private ArrayList<String> productNames = new ArrayList<String>();
	
	/**
	 * List of the characteristics' names.
	 */
	private ArrayList<String> characteristics = new ArrayList<String>();
	
	/**
	 * List of the characteristics' names identified as binary features.
	 */
	private ArrayList<String> features = new ArrayList<String>();
	
	/**
	 * List of the characteristics' names identified as multi-valued attributes.
	 */
	private ArrayList<String> attributes = new ArrayList<String>();
	
	/**
	 * Current PCM after binary scaling.
	 */
	private ProductComparisonMatrix scaledPCM;

	
	/*
	 * ------ CONSTRUCTORS ------
	 */

	/**
	 * Creates an empty PCM.
	 */
	public ProductComparisonMatrix() {
	}
	
	/**
	 * Creates a PCM based on the data contained in the CSV file at the specified path.
	 * 
	 * @param n name of the CSV file.
	 */
	public ProductComparisonMatrix(String n) {
		
		// Imports from CSV file
		this.initFromCSVfile("data/0_clean_PCMs/" + n);
		
		this.PCM_name = n.substring(0, (n.length()-4));
		
		// Creates the directory
		this.PCM_dir = "data/" + PCM_name + "/";
		
		try {
			if(Files.notExists(Paths.get(PCM_dir))){
				Files.createDirectory(Paths.get(PCM_dir));
			}	
			
			if(Files.notExists(Paths.get(PCM_dir + "variability/"))){
				Files.createDirectory(Paths.get(PCM_dir + "variability/"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* 
	 * ------ INHERITED METHODS ------
	 */


	@Override
	/**
	 * Initialises the PCM instance depending on the data contained in the CSV file at the specified path.
	 * Computes the variability percentage of the imported PCM and prints its content.
	 * 
	 * @param path the path to the CSV file.
	 */
	public void initFromCSVfile(String path) {
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(path));
			
			// Retrieves first line containing characteristics' names
			String line = br.readLine();
			
			// Adds in the list of characteristics
			for (String v : line.split(",")) {
				this.addCharacteristic(v);
			}
			this.characteristics.remove("name");
			
			// Retrieves each product and its values
			
			while ((line = br.readLine()) != null) {
				
				// Extracts the current product's name
				String p = line.substring(0, line.indexOf(","));
				
				// Stores the current product's name
				this.addProduct(p);
				
				// Retrieves and stores the values
				String line2 = line.substring(line.indexOf(",")+1, line.length());
				
				while (!line2.equals("")) {
					if (line2.contains(",")) {
						String v = line2.substring(0, line2.indexOf(","));
						line2 = line2.substring(line2.indexOf(",")+1, line2.length());
						//System.out.println("-> "+v); 
						products.get(products.size() - 1).add(v);
					} else {
						//System.out.println("-> "+line2); 
						products.get(products.size() - 1).add(line2);
						line2 = "";
					}
				}
				if (line.endsWith(",")) {
					products.get(products.size() - 1).add("");
					//System.out.println("add -> ");
				}
						
			}
			
			// Closes the buffer
			br.close();
			
		} catch (Exception e) {
			System.err.println("PCM cannot be initialised with the file "+path);
			e.printStackTrace();
		}

		// Prints the imported PCM
		//this.printPCM();
		
		// Computes the variability percentage of each characteristic
		this.computeDistinctValues();
		

	}
	
	@Override
	/**
	 * Exports the PCM in the specified CSV file.
	 * 
	 * @param path the path of the CSV file to be created.
	 */
	public void saveInCSVfile(String path){
		try {
			BufferedWriter file = new BufferedWriter(new FileWriter (path+".csv"));
			
			// Writes first line with characteristic names
			file.write("name");
			for (String c : characteristics) {
				file.write(","+c);
			}
			file.newLine();
			
			// Writes each product
			for (String p : productNames) {
				file.write(p);
				for (String v : products.get(getIndexOfProduct(p))) {
					file.write(","+v);
				}
				file.newLine();
			}
			
			file.close();
			
		} catch (Exception e) {
			System.err.println("PCM cannot be exported to the file "+path);
			e.printStackTrace();
		}
	}

	@Override
	/** 
	 * Returns the list of products' names.
	 * 
	 * @return an ArraList containing the name of the products.
	 */
	public ArrayList<String> getProducts() {
		return productNames;
	}

	@Override
	/**
	 * Returns the list of characteristics' names.
	 * 
	 * @return an ArrayList containing the names of the characteristics.
	 */
	public ArrayList<String> getCharacteristics() {
		return characteristics;
	}

	@Override
	/**
	 * Returns the values of the specified characteristic for the specified product.
	 * 
	 * @param p the name of the product.
	 * @param c the name of the characteristic.
	 * @return the value of the characteristic c for the product p. 
	 */
	public String getValues(String p, String c) {
		
		int pi = this.getIndexOfProduct(p);
		int ci = this.getIndexOfCharacteristic(c);

		return products.get(pi).get(ci);
	}
	
	@Override
	/**
	 * Returns the distinct values (without duplications) of the specified characteristic.
	 * 
	 * @param c the name of the characteristic.
	 * @return an ArrayList containing the distinct values of the characteristic c.
	 */
	public ArrayList<String> getDistinctCharacteristicValues(String c){
		int ci = getIndexOfCharacteristic(c);
		return distinctValues.get(ci);
	}

	@Override
	/**
	 * Adds the specified product to the list of products.
	 * Adds an empty row in the PCM.
	 * 
	 * @param p the name of the new product.
	 */
	public void addProduct(String p) {
		this.productNames.add(p);
		
		// adds an empty row in the PCM
		this.products.add(new ArrayList<String>());
		assert(productNames.size() == products.size());
	}

	@Override
	/**
	 * Adds the specified characteristic to the set of characteristics.
	 * Adds an empty field to the list of values of each product.
	 * 
	 * @param c the name of the new characteristic.
	 */
	public void addCharacteristic(String c) {
		// Adds the name of the new characteristics to the list
		this.characteristics.add(c);
		
		// Adds empty fields to each product
		// Corresponding to the new characteristic
		for (ArrayList<String> p : products) {
			p.add("");
		}
	}

	@Override
	/**
	 * Replaces the value of the specified characteristic for the specified product.
	 * 
	 * @param p the name of the product.
	 * @param c the name of the characteristic.
	 * @param v the new value
	 */
	public void addValue(String p, String c, String v) {
		// Retrieves index of p and c in the matrix
		int pi = this.getIndexOfProduct(p);
		int ci = this.getIndexOfCharacteristic(c);
		
		// Replaces the corresponding cell with v
		if (ci != -1 && pi != -1) {
			this.products.get(pi).set(ci, v);
		}		
	}
	
	@Override
	/**
	 * Removes a characteristic from the PCM:
	 * Erases the characteristic's name from the list.
	 * Removes the characteristic's values from each product.
	 * 
	 * @param c the name of the characteristic to be removed.
	 */
	public void removeCharacteristic(String c) {
		int ci = getIndexOfCharacteristic(c);
		for (ArrayList<String> p : products) {
			p.remove(ci);
		}
		characteristics.remove(c);

		computeDistinctValues();
	}

	@Override
	/**
	 * Returns the list of characteristics' names identified as binary features.
	 * 
	 * @return an ArrayList containing the names of the characteristics identified as binary features.
	 */
	public ArrayList<String> getFeatures() {
		return features;
	}

	@Override
	/**
	 * Returns the list of characteristics' names identified as multi-valued attributes.
	 * 
	 * @return an ArrayList containing the names of the characteristics identified as multi-valued attributes.
	 */
	public ArrayList<String> getAttributes() {
		return attributes;
	}
	
	@Override
	/**
	 * Computes the distinct values of each characteristic.	 * 
	 * The attribute disctinctValues is updated.
	 */
	public void computeDistinctValues() {
	
		distinctValues.clear();
		
		// Distinct values of each characteristics
		for (String c : characteristics) {
			
			// Computes the distinct values of the current characteristic
			ArrayList<String> dv = new ArrayList<String>();
			
			for (String p : productNames) {
				
				// Retrieves the values of the corresponding cell
				String cell = getValues(p, c);
				
				// Splits the different value if there is any
				for(String s : cell.split(";")){
					if (!dv.contains(s)) {
						dv.add(s);
					}
				}
			}
			
			// Keeps the distinct values for later use
			distinctValues.add(new ArrayList<String>());
			distinctValues.get(distinctValues.size() - 1).addAll(dv);
		}	
	}

	@Override
	/**
	 * Separates binary features from multi-valued attributes depending on their variability percentage and the user choices.
	 * The characteristics identified as binary attributes are scaled, i.e., each value of the characteristic becomes a new characteristic with binary values.
	 * Computes the distinct values at the end of the scaling.
	 * Thus, lists attributes, features, characteristics and distinctValues are updated.
	 */
	 public void computeAttributesAndFeatures() {
		
		features.clear(); attributes.clear();
		
		System.out.println("-------------------------------------------");
		System.out.println("--------- SPLITING CHARACTERISTICS --------");
		System.out.println("-------------------------------------------");
		
		// Split features and attributes depending on the user choice
		
		int binChar = 0;
		
		for (String c : this.characteristics) {
			
			int ci = getIndexOfCharacteristic(c);

			// Detects if the current characteristics represents a feature
			if (((distinctValues.get(ci).contains("X")
					|| distinctValues.get(ci).contains("") )
					&& distinctValues.get(ci).size() <= 3)) {
				
				binChar++;
				features.add(characteristics.get(ci));
				System.out.println("-- Characteristic " + c + "is a feature (auto)");
				
			} else {
				// If the current characteristics is not a feature,
				// Asks the user to categorise it
				System.out.println("Does " + c + " represent a features? y/n");
				Scanner sc = new Scanner(System.in);
				String str = sc.nextLine();
				if (str.equals("y")) {
					features.add(characteristics.get(ci));
				} else {
					attributes.add(characteristics.get(ci));
				}
			}
		}
		System.out.println(" ------- Results:");
		System.out.println("#Binary Char. in PCM = " + binChar);
		System.out.println("#Mult. Valued Char. in PCM = " + (characteristics.size() - binChar));
		
		
		System.out.println("-------------------------------------------");
		System.out.println("------------ PCM BINARY SCALING -----------");
		System.out.println("-------------------------------------------");
		
		//System.out.println("--------- Candidates: "+features);
		
		// Applies binary scaling for each identified binary feature
		for (String f : features) {
			
			// Retrieves the index of the current characteristic in the PCM before scaling
			int fi = getIndexOfCharacteristic(f);
			
			// Verifies if the characteristics is not already a binary feature
			if (!((distinctValues.get(fi).contains("X")
					|| distinctValues.get(fi).contains("") )
					&& distinctValues.get(fi).size() <= 3)) {
			
				System.out.println(" -> " + f + " has to be binary scaled");
				
				// Adds characteristics corresponding to the scaled values
				for (String v : distinctValues.get(fi)) {
					this.addCharacteristic(v);
				}
				
				// Fills the cells of the new binary characteristics
				
				// For each new characteristic
				for (String v : distinctValues.get(fi)) {
					
					// For each product
					for (String p : productNames) {
					
						String cell = getValues(p, f);
						
						// If the value for the product of the current scaled characteristic
						// is equal to the new characteristic
						for (String c : cell.split(";")) {
						
							if (c.equals(v)) {
							
								// Adds 'X' to the new characteristic for the current product
								addValue(p, v, "X");
							}
						}
					}
				}
			} 
//			else {
//				System.out.println(f + " is already binary scaled");
//			}
								
				// Replaces the values for the scaled characteristic
				// For each product
				for (String p : productNames) {
					if (getValues(p, f).equals("*") || getValues(p, f).equals("")) {
						addValue(p,f,"");
					} else {
						addValue(p,f,"X");
					}
				}
			//}
			
			// Re-initialises the list of distinct values
			this.computeDistinctValues();
		}
	}

	@Override
	/**
	 * Computes the similarities of each characteristic identified as multi-valued attributes.
	 * Relies on concrete similarityOperators to make the computation.
	 * A copy of the current PCM is made and scaled depending on the obtained taxonomy to obtained a formal context.
	 * Saves the scaled copy of the scaled PCM into a CSV file, and as a class attribute.
	 * 
	 * TODO let the user choose the similarity operator
	 */
	public void computeSimilarities() {
		
		System.out.println("-------------------------------------------");
		System.out.println("---------- TAXONOMY BINARY SCALING --------");
		System.out.println("-------------------------------------------");
		
		try {
			if(Files.notExists(Paths.get(PCM_dir + "taxonomies/"))){
				Files.createDirectory(Paths.get(PCM_dir + "taxonomies/"));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		// Separates the binary features and the multi-valued attributes
		this.computeAttributesAndFeatures();
		
		// Build a fully binary scaled PCM
		ProductComparisonMatrix scaledPCM = new ProductComparisonMatrix();
		scaledPCM = this;
		
		// For each attribute
		for (String a : attributes) {
			
			// Indentifies the type: Integer, Double, String or FEAT
			
			
			boolean isInteger = true;
			for(String v : distinctValues.get(getIndexOfCharacteristic(a))){
				if(!v.matches("\\*|[0-9]+")){
					isInteger = false;
				}
			}
			
			boolean isDouble = true;
			for(String v : distinctValues.get(getIndexOfCharacteristic(a))){
				if(!v.matches("\\*|[0-9]+\\.?[0-9]*")){
					isDouble = false;
				}
			}
			
			System.out.print(a+" type is "+(isInteger? "Integer" : (isDouble ? "Double" : "String")));
			
			// Calls the correct Similarity Operator depending on the identified type of the attribute
			if (isInteger) {
				
				IntegerIntervalSimilarityOperator iiso = new IntegerIntervalSimilarityOperator(scaledPCM, a);
				scaledPCM = (ProductComparisonMatrix) iiso.scaling();
				iiso.computeDotFile(PCM_dir);
				
			} else if (isDouble) {
				
				DoubleIntervalSimilarityOperator diso = new DoubleIntervalSimilarityOperator(scaledPCM, a);
				scaledPCM = (ProductComparisonMatrix) diso.scaling();
				diso.computeDotFile(PCM_dir);
				
			} else {
				
				StringDefaultSimilarityOperator sdso = new StringDefaultSimilarityOperator(scaledPCM, a);
				scaledPCM = (ProductComparisonMatrix) sdso.scaling();
				sdso.computeDotFile(PCM_dir);
				
			}
		}
		scaledPCM.saveInCSVfile(PCM_dir + PCM_name + "_scaling");
		
		this.scaledPCM = scaledPCM;
		
		// Save into RCFT file to be processed by RCAExplore
		RcftExporter re = new RcftExporter(scaledPCM, PCM_dir + PCM_name + ".rcft");
		re.createsRcftFile();
		re.savesRcftFile();
	}

	@Override
	/**
	 * Computes the concept lattice associated with the product description.
	 */
	public void computeLattice() {
		try {
			if(Files.notExists(Paths.get(PCM_dir + "FCA/"))){
				Files.createDirectory(Paths.get(PCM_dir + "FCA/"));
			}
			
			Runtime r = Runtime.getRuntime();
			System.out.println("java -jar rcaexplore-20151012.jar auto "+PCM_dir + PCM_name+".rcft "+ PCM_dir + "FCA/");
			Process p = r.exec("java -jar rcaexplore-20151012.jar auto "+PCM_dir + PCM_name+".rcft "+ PCM_dir + "FCA/");
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((reader.readLine()) != null) {}
			
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/*
	 * ------ ADDED METHODS ------
	 */
	
	public String getPMC_dir(){
		return PCM_dir;
	}

	/**
	 * Returns the index in productNames of the specified product.
	 * -1 if the product's name is not in the list.
	 * 
	 * @param p the name of the product.
	 * @return the index of p in productNames, else -1.
	 */
	public int getIndexOfProduct(String p){
		for (int i = 0; i < productNames.size() ; i++) {
			if (p.equals(productNames.get(i))) {
				return i;
			}
		}
		System.err.println("Product "+p+" not found.");
		return -1;
	}
	
	/**
	 * Returns the index in characteristics of the specified characteristic.
	 * -1 if the characteristic's name is not in the list.
	 * 
	 * @param c the name of the characteristic.
	 * @return the index of c in characteristics, else -1.
	 */
	public int getIndexOfCharacteristic(String c){
		for (int i = 0; i < characteristics.size() ; i++) {
			if (c.equals(characteristics.get(i))) {
				return i;
			}
		}
		System.err.println("Characteristic "+c+" not found.");
		return -1;
	}
	
	/**
	 * Prints the PCM.
	 */
	public void printPCM(){
		System.out.println(("----------"));
		
		// Prints characteristics
	//	System.out.print("      ");
		for (int i = 0 ; i < characteristics.size() ; i++) {
			System.out.print(characteristics.get(i)+"\t\t");
		}
		
		System.out.println();
		
		// Prints products and their values
		for (int i = 0 ; i < products.size() ; i++) {
			
			
			for (int j = 0 ; j < products.get(i).size() ; j++) {
				System.out.print(products.get(i).get(j)+"\t\t");
			}
			
			System.out.print(productNames.get(i)+"      ");
			
			System.out.println();
		}
		
		System.out.println(("----------"));
	}
	
	/**
	 * Prints information about the PCM characteristics.
	 */
	public void printCharacteristicInfo(){
		
		// Number of characteristics
		System.out.println("-------------------------------------------");
		System.out.println("---------- CHARACTERISTICS' INFOS ---------");
		System.out.println("-------------------------------------------");

		System.out.println(" * Number of characteristics: "+characteristics.size());
		
		// Characteristics' name
		System.out.println(" * Characteristics' name: "+characteristics);
		System.out.println("---------- DETAILS:");
		
		// Distinct values of each characteristics
		for (String c : characteristics) {
			
			System.out.print("  -> "+c+": ");

			System.out.println(getDistinctCharacteristicValues(c)+ " ("+getDistinctCharacteristicValues(c).size()+" values / "+productNames.size()+" products)");
		}

	}
	
	/**
	 * Return the fully scaled PCM.
	 * 
	 * @return an instance of {@link ProductComparisonMatrix} fully scaled
	 */
	public ProductComparisonMatrix getScaledPcm(){
		return this.scaledPCM;
	}
	
	
	public static void main (String[] args){
		
		ProductComparisonMatrix pcm = new ProductComparisonMatrix("Comparison_of_CRM_systems_0.csv");
		
		pcm.printCharacteristicInfo();
		
		pcm.computeSimilarities();
				
		pcm.computeLattice();
		
		// Computes implication transitive reduction without redundancy
		ImplicationExtractor ie = new ImplicationExtractor(pcm.getScaledPcm(), pcm.getPMC_dir());
		ie.computesImplications();
		ie.exportsInTextFile();
		
		// Computes implication transitive closure without redundancy
		AllImplicationExtractor aie = new AllImplicationExtractor(pcm.getScaledPcm(), pcm.getPMC_dir());
		aie.extractAllImplications();
		aie.exportsInTextFile();
		
		// Computes co-occurrences
		CooccurrenceExtractor ce = new CooccurrenceExtractor(pcm.getScaledPcm(), pcm.getPMC_dir());
		ce.computesCoOccurrences();
		ce.exportsInTextFile();
		
		// Computes Mutex with and without redundancy
		MutexExtractor me = new MutexExtractor(pcm.getScaledPcm(), pcm.getPMC_dir());
		me.computesMutex();
		me.computePertinentMutex();
		me.exportsInTextFile();
		
	}
}
