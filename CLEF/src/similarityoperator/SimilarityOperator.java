package similarityoperator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import productdescription.ProductDescription;

/**
 * This abstract class represents Similarity Operators and their behaviours.
 * 
 * It allows to scale the characteristics identified as multi-valued attributes and to save the similarities into a dot file.
 * 
 * @author Jessie Carbonnel
 *
 */
public abstract class SimilarityOperator {
	
	/*
	 * ---------- ATTRIBUTES ----------
	 */
	/**
	 * The product description.
	 */
	protected ProductDescription description;
	
	/** 
	 * The name of the attribute on which the similarities will be computed.
	 */
	protected String attribute;
	
	/**
	 * The distinct values of the attribute.
	 */
	protected ArrayList<String> values;
	
	/**
	 * A String containing the content of the dot file.
	 */
	protected String dot;
	
	/*
	 * ---------- CONSTRUCTORS ----------
	 */
	
	/**
	 * Creates an instance of Similarity Operator referencing the Product Description 
	 * and the attribute on which similarities have to be computed.
	 * 
	 * @param d any kind of product description.
	 * @param a the name of the attribute.
	 */
	public SimilarityOperator(ProductDescription d, String a){
		assert(d.getCharacteristics().contains(a));
		
		this.description = d;
		this.attribute = a;
		
		this.values = new ArrayList<String>();
		this.values.addAll(d.getDistinctCharacteristicValues(a));
	}

	/*
	 * ---------- METHODS ----------
	 */
	
	/**
	 * Applies binary scaling on the attribute in the product description.
	 * 
	 * @return the product description after the binary scaling.
	 */
	public abstract ProductDescription scaling();

	/**
	 * Computes and saves the dot file representing the similarities of the attribute.
	 * 
	 * @param p the path of the dot file to be created.
	 */
	public void computeDotFile(String p) {
		try {
			BufferedWriter file = new BufferedWriter(new FileWriter (p+"/taxonomies/"+attribute+".dot"));
			file.write(this.dot);
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
