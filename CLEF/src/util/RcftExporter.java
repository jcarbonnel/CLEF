package util;

import java.io.BufferedWriter;
import java.io.FileWriter;

import productdescription.ProductDescription;

/**
 * This class allows to save any kind of product description (entirely binary scaled) in an .rcf file,
 * to later be processed by the tool RCAExplore to obtain a concept lattice.
 * 
 * @author Jessie Carbonnel
 *
 */
public class RcftExporter {

	/*
	 * ----------- ATTRIBUTES ----------
	 */
	
	/**
	 * The product description to be saved in rcft format.
	 */
	private ProductDescription description;
	
	/**
	 * The path of the rcft file to be created.
	 */
	private String path;
	
	/**
	 * Content of the rcft file.
	 */
	private String rcft;
	
	/*
	 * ---------- CONSTRUCTORS -----------
	 */
	
	/**
	 * Created an instance of RcftExporter for a specified description.
	 * 
	 * @param d the product description to be expoter in an rcft file.
	 * @param path the path of the rcft file to be created.
	 */
	public RcftExporter(ProductDescription d, String path) {
		this.description = d;
		this.path = path;
	}
	
	/*
	 * ---------- METHODS -----------
	 */
	
	/**
	 * Creates the content of the rcft file depending on the data of the prodcut description.
	 */
	public void createsRcftFile(){
		
		// Writes the beginning of the rcft file
		rcft = "";
		rcft += "FormalContext ctx\nalgo fca\n| |"; 
		
		// Writes the characteristics
		for (String c : description.getCharacteristics()) {
			rcft += c+"|";
		}
		rcft += "\n";
		
		for (String p : description.getProducts()) {
			
			// Writes the name of the product
			rcft += "|" + p + "|";
			
			// Writes the values of the scaled characteristics
			for (String c : description.getCharacteristics()) {
				if (description.getValues(p, c).equals("X")) {
					rcft += "x|";
				} else if (description.getValues(p, c).equals("")) {
					rcft += "|";
				} else {
					System.err.println("Bad format for rcft: " + c);
				}
			}
			rcft += "\n";
			
		}
		
	}
	
	/**
	 * Exports the product description into a rcft file at the specified path.
	 */
	public void savesRcftFile() {
		try {
			BufferedWriter file = new BufferedWriter(new FileWriter (path));
			file.write(this.rcft);
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
