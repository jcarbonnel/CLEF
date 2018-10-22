package productdescription;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class ProductMatrix {
	
	/*
	 * ------ ATTRIBUTES ------
	 */
	
	/**
	 * The name of the product matrix.
	 */
	private String name;
	
	/**
	 * The list of product names.
	 */
	private ArrayList<String> productNames;
	
	/**
	 * The list of characteristics.
	 */
	private ArrayList<Characteristic> characteristics;
	
	/**
	 * The list of product, a product being represented by the list of its values for each characteristics.
	 */
	private ArrayList<ArrayList<String>> products;
	
	/**
	 * A scaled product matrix, containing only boolean features.
	 */
	private ProductMatrix scaledPM;

	
	/*
	 * ------ CONSTRUCTORS ------
	 */
	
	/**
	 * Creates an empty product matrix.
	 */
	public ProductMatrix() {
		
		this.name = name;
		this.productNames = new ArrayList<String>();
		this.products = new ArrayList<ArrayList<String>>();
		this.characteristics = new ArrayList<Characteristic>();
		
	}
	
	/**
	 * Creates a new Product matrix w.r.t the csv file specified in parameter.
	 * The specified csv file must be in the directory "data/0_clean_PCMs/".
	 * @param fileName the name of the csv file in the directory data/0_clean_PCMs.
	 */
	public ProductMatrix(String fileName) {
		
		this.name = fileName.substring(0, fileName.length()-4);
		this.productNames = new ArrayList<String>();
		this.products = new ArrayList<ArrayList<String>>();
		this.characteristics = new ArrayList<Characteristic>();
		
		this.initFromCSVfile(fileName);
		
		scaledPM = new ProductMatrix();
		scaledPM.setName("scaled_" + name);
		scaledPM.setProductNameList(this.productNames);
		
		try {
			if(Files.notExists(Paths.get("data/" + name))){
				Files.createDirectory(Paths.get("data/" + name));
			}	
			
			if(Files.notExists(Paths.get("data/" + name + "/variability/"))){
				Files.createDirectory(Paths.get("data/" + name + "/variability/"));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	

	/*
	 * ------ GETTERS AND SETTERS  ------
	 */
	
	/**
	 * Initialises the name of the product matrix.
	 * @param n a String representing the new name of the matrix.
	 */
	public void setName(String n) {
		this.name = n;
	}
	
	/**
	 * Returns the name of the product matrix.
	 * @return a String representing the name of the matrix.
	 */
	public String getName() { 
		return this.name;
	}
	
	/**
	 * Initialises the product name list.
	 * Initialises the list of products.
	 * @param list an ArrayList of String containing the product name list.
	 */
	public void setProductNameList(ArrayList<String> list) {
		this.productNames.addAll(list);
		
		for (int i = 0 ; i < productNames.size() ; i++) {
			products.add(new ArrayList<String>());
		}
	}
	
	/**
	 * Returns an integer representing the number of products in the matrix.
	 */
	public int getNumberOfProducts() {
		return productNames.size();
	}
	
	/**
	 * Returns an integer representing the number of characteristics in the matrix.
	 */
	public int getNumberOfCharacteristics() {
		return characteristics.size();
	}
	
	public ArrayList<String> getProductNameList() {
		return productNames;
	}
	
	public ArrayList<Characteristic> getCharacteristicList() {
		return characteristics;
	}
	
	public ArrayList<ArrayList<String>> getProducts() {
		return this.products;
	}
	
	
	/*
	 * ------ METHODS------
	 */

	/**
	 * Reads the specified csv file and initialises the product matrix.
	 * @param pcmName a String representing the name of the csv file from which initialise the product matrix.
	 */
	private void initFromCSVfile(String pcmName) {
		try {
			
			BufferedReader br = new BufferedReader(new FileReader("data/0_clean_PCMs/" + pcmName));
			
			String line = br.readLine();
			
			String charateristicList = line.substring(line.indexOf(",") + 1, line.length());
			System.out.println("Characteristic list: " + charateristicList);
						
			while ((line = br.readLine()) != null) {
				
				String currentProductName = line.substring(0, line.indexOf(","));			
				this.productNames.add(currentProductName);
				
				String stringValueList = line.substring(line.indexOf(",") + 1, line.length());
				
				ArrayList<String> valueList = new ArrayList<String>();
								
				for (String value : stringValueList.split(",",-1)) {
					valueList.add(value);
				}
								
				this.products.add(valueList);
			}
			
			br.close();
			
			int i = 0;
			for (String characteristicName : charateristicList.split(",")) {
				
				ArrayList<String> valueList = new ArrayList<String>();
				
				for (ArrayList<String> product : products) {
					valueList.add(product.get(i));
				}
				
				HashSet<String> distinctValues = new HashSet<String>();
				distinctValues.addAll(valueList);
				distinctValues.remove("X"); distinctValues.remove(""); distinctValues.remove("*");
				
				Characteristic c;
				
				if (distinctValues.size() == 0) {
					c = new Feature (characteristicName, valueList);
				} else {
					c = new Attribute (characteristicName, valueList);
				}
				
				this.characteristics.add(c); 
				
				i++;
			}
			
		} catch (Exception e) {
			System.err.println("PCM cannot be initialised with the file " + pcmName);
			e.printStackTrace();
		}
	}
	
	private ProductMatrix computeScaledMatrix() {
		
		for (Characteristic c : this.characteristics) {
			scaledPM = c.scaling(scaledPM);
		}
		return scaledPM;
	}
	
	/**
	 * Prints the product matrix information.
	 */
	public void printMatrix() {
	
		System.out.println("############ " + this.name + " ############");
		
		System.out.println("############ Products ############");
		
		for (int i = 0 ; i < productNames.size() ; i++) {
			
			System.out.println("# " + productNames.get(i) + ": " + products.get(i));
		}
		
		System.out.println("############ Characteristics ############");
		
		for (Characteristic c : this.characteristics) {
			System.out.println("# " + c);
		}
		
		System.out.println("####################################");
		
		System.out.println();
	}
	
	public void printMatrix2(){
		System.out.println("############ Characteristics ############");
		
		for (Characteristic c : this.characteristics) {
			HashSet<String> dv = new HashSet<>();
			for (String v : c.getDistinctValues()) {
				for (String v2 : v.split(";")) {
					dv.add(v2);
				}
			}
			ArrayList<String> dvsorted = new ArrayList<>();
			dvsorted.addAll(dv);
			Collections.sort(dvsorted);
			System.out.println("# " + c.name + ": " + dvsorted);
			System.out.println();
		}
	}
	
	/**
	 * Adds a new characteristic (and its values) to the product matrix.
	 * @param c the new characteristic.
	 */
	public void addCharacteristic(Characteristic c){
		
		this.characteristics.add(c);
		
		int i = 0;
		for (ArrayList<String> product : this.products) {
			
			product.add(c.getValues().get(i));
			i++;
			
		}
	}
	
	/**
	 * This methods exports the scaled PCM into an rcft file.
	 */
	private void exportInRcft() {
		
		String rcft = "FormalContext ctx\nalgo fca\n| |"; 
		
		for (Characteristic c : this.scaledPM.getCharacteristicList()) {
			rcft += c.getName() + "|";
		}
		rcft += "\n";
		
		int productIndex = 0;
		for (String product : this.scaledPM.getProductNameList()) {
			
			rcft += "|" + product + "|";
			
			for (String value : this.scaledPM.getProducts().get(productIndex)) {
				
				rcft += value.toLowerCase() + "|";
			}
			rcft += "\n";
			productIndex++;
		}
		
		try {
			BufferedWriter file = new BufferedWriter(new FileWriter ("data/" + this.name + "/" + this.name + ".rcft"));
			file.write(rcft);
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method computes the concept lattice and AC-poset of the rcft corresponding to the scaled PCM.
	 */
	public void computeLattice() {
		
		computeScaledMatrix();
		
		exportInRcft();
		
		try {
			
			String PCM_dir = "data/" + name + "/";
			if(Files.notExists(Paths.get(PCM_dir + "FCA/"))){
				Files.createDirectory(Paths.get(PCM_dir + "FCA/"));
			}
			
			if(Files.notExists(Paths.get(PCM_dir + "FCA/AC-poset"))){
				Files.createDirectory(Paths.get(PCM_dir + "FCA/AC-poset"));
			}
			
			Runtime r = Runtime.getRuntime();
			System.out.println("java -jar rcaexplore-20151012.jar auto " + PCM_dir + name + ".rcft " + PCM_dir + "FCA/");
			Process p = r.exec("java -jar rcaexplore-20151012.jar auto "+ PCM_dir + name+".rcft " + PCM_dir + "FCA/");
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((reader.readLine()) != null) {}
			
			p.waitFor();
			
			System.out.println("java -jar rcaexplore-20151012.jar auto "+PCM_dir + name+".rcft "+ PCM_dir + "FCA/AC-poset/ --follow-path=trace1.csv");
			p = r.exec("java -jar rcaexplore-20151012.jar auto "+PCM_dir + name+".rcft "+ PCM_dir + "FCA/AC-poset/ --follow-path=trace.csv");
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((reader.readLine()) != null) {}
			
			p.waitFor();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
