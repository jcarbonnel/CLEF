package productdescription;

import java.util.ArrayList;
/**
 * This abstract class represents product descriptions and their behaviours. 
 * It does not depend on the product description format.
 * 
 * From any kind of product descriptions,
 * one can be able to retrieve the set of products, the set of characteristics, and the values of a characteristic for a product,
 * as well as editing its content.
 * 
 * Also, one can be able to retrieve the set of distinct values of a given characteristic.
 * 
 * The concrete product descriptions should allow to detect features from attributes. 
 * 
 * Finally, they should compute taxonomies of their characteristics based on Similarity Operator implementation.
 * 
 * @author Jessie Carbonnel
 *
 */
public abstract class ProductDescription {

	/*
	 * ---------- METHODS ----------
	 */
	
	/**
	 * Imports data from a CSV file to initialise the product description.
	 * 
	 * @param path the path of the CSV file.
	 */
	public abstract void initFromCSVfile(String path);
	
	/**
	 * Exports the product description into a CSV file.
	 * 
	 * @param path the path of the CSV file to be created.
	 */
	public abstract void saveInCSVfile(String path);
	
	/**
	 * Returns the list of products of the product description.
	 * 
	 * @return an ArrayList containing the products' names.
	 */
	public abstract ArrayList<String> getProducts();
	
	/**
	 * Returns the list of characteristics of the product description.
	 * 
	 * @return an ArrayList containing the characteristics' names.
	 */
	public abstract ArrayList<String> getCharacteristics();
	
	/**
	 * Returns a String representing the values of the characteristics c for the product p.
	 * The product and the characteristic are represented by their name.
	 * If they are several values, they are returned in a single String, separated by semicolons.
	 *  
	 * @param p the name of the product.
	 * @param c the name of the characteristic.
	 * @return a String representing the values of the characteristic c for the product p.
	 */
	public abstract String getValues(String p, String c);
	
	/**
	 * Returns the set of distinct values for the specified characteristic.
	 * 
	 * @param c the name of the characteristic.
	 * @return an ArrayList containing the set of distinct values of the specified characteristic, with no duplicates.
	 */
	public abstract ArrayList<String> getDistinctCharacteristicValues(String c);
	
	/**
	 * Adds the specified product to the list of products.
	 * 
	 * @param p the new product's name.
	 */
	public abstract void addProduct(String p);
	
	/**
	 * Adds the specified characteristic to the list of characteristics.
	 * 
	 * @param c the new characteristic's name.
	 */
	public abstract void addCharacteristic(String c);
	
	/**
	 * Adds the specified value for the characteristic c and the product p.
	 * 
	 * @param p the name of the product.
	 * @param c the name of the characteristic.
	 * @param v the new value.
	 */
	public abstract void addValue(String p, String c, String v);
	
	/**
	 * Removes the specified characteristic from the list of characteristics.
	 * 
	 * @param c the name of the characteristic to be removed.
	 */
	public abstract void removeCharacteristic(String c);
	
	/**
	 * Retrieves the characteristics that have been identified as binary features.
	 * 
	 * @return an ArrayList containing the names of the characteristics identified as features.
	 */
	public abstract ArrayList<String> getFeatures();
	
	/**
	 * Retrieves the characteristics that have been identified as multi-valued attributes.
	 * 
	 * @return an ArrayList containing the names of the characteristics identified as multi-valued attributes. 
	 */
	public abstract ArrayList<String> getAttributes();
	
	/**
	 * Computes the distinct values of each characteristic of the product description.
	 */
	public abstract void computeDistinctValues();
	
	/**
	 * Identifies binary features and multi-valued attributes among the list of characteristics of the product description.
	 */
	public abstract void computeAttributesAndFeatures();
	
	/**
	 * Computes the similarities of the characteristics identified as multi-valued attributes
	 * based on a Similarity operator,
	 * to build a taxonomy.
	 */
	public abstract void computeSimilarities();
	
	/**
	 * Computes the concept lattice associated with the product description.
	 */
	public abstract void computeLattice();
	
	
}
