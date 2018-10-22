package productdescription;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a characteristic (i.e., a column) of a {@link productdescription.ProductMatrix}.
 * 
 * A characteristic has a name and a list of values.
 * 
 * Each value corresponds to a product of the {@link productdescription.ProductMatrix}. 
 * More precisely, the index of a value correspond to the product having the same index in the {@link productdescription.ProductMatrix}.
 * 
 * We keep a set of distinct values to ease the characteristic management.
 * 
 * A characteristic is either a boolean feature ({@link productdescription.Feature}) or a multi-valued attribute ({@link productdescription.Attribute}) of type integer, double or literal.
 *  
 * @author Jessie Galasso Carbonnel
 *
 */
public abstract class Characteristic {

	/*
	 * ------- ATTRIBUTES ------
	 */
	
	/**
	 * Name of the characteristic.
	 */
	protected String name;
	
	/**
	 * List of values of the characteristic.
	 * The value at index i corresponds to the product at index i in the list of {@link productdescription.ProductMatrix}.
	 */
	protected ArrayList<String> values = new ArrayList<String>();
	
	/**
	 * Set of distinct values of the characteristic.
	 */
	protected Set<String> distinctValues = new HashSet<String>();

	
	/*
	 * ------ CONSTRUCTORS ------
	 */
	
	/**
	 * Help creates a characteristic with a name and a list of values.
	 * @param name a String representing the name of the characteristic.
	 * @param values an ArrayList of String representing the values of the characteristic in the product matrix.
	 */
	public Characteristic(String name, ArrayList<String> values) {
		this.name = name;
		this.values.addAll(values);
		this.distinctValues.addAll(values);
		this.distinctValues.remove("*");
	}
	
	
	/*
	 * ------ GETTERS AND SETTERS ------
	 */

	/**
	 * Returns the name of the characteristic
	 * @return a String representing the name of the characteristic
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the list of values of the characteristic.
	 * @return an ArrayList of String representing all the values of the characteristic.
	 */
	public ArrayList<String> getValues() {
		return values;
	}

	/**
	 * Returns the list of values of the characteristic.
	 * @return an ArrayList of String representing the distinct values of the characteristic.
	 */
	public Set<String> getDistinctValues() {
		return distinctValues;
	}
	
	
	/*
	 * ------ ABSTRACT METHODS ------
	 */
	
	/**
	 * Applies binary scaling on the current characteristic, which produces a set of boolean features ({@link productdescription.Feature}).
	 * These features are added to the specified product matrix.
	 * The method returns the specified product matrix extended with the new boolean features.
	 * @param pm the instance of ProductMatrix in which the instances of Feature resulting from the binary scaled characteristic have to be added.
	 */
	public abstract ProductMatrix scaling(ProductMatrix pm);
	
	/**
	 * Returns the type of the characteristic amongst
	 * "Boolean feature", "Integer Attribute", "Double Attribute" or "Literal Attribute".
	 * @return a String representing the type of the characteristic.
	 */
	public abstract String getType();
	

	/*
	 * ------ METHODS ------
	 */
	
	public String toString() {
		return "Name: " + this.name + " / Type: " + this.getType() + " / Values: " + this.values;
	}

}
