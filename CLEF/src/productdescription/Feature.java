package productdescription;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a boolean characteristic, also called a Feature.
 * 
 * A Feature has a name and a list of values.
 * These values may be "X" or "".
 *  
 * @author Jessie Galasso Carbonnel
 *
 */
public class Feature extends Characteristic {

	/*
	 * ------ CONSTRUCTORS ------
	 */
	
	/**
	 * Creates a boolean characteristic with a name and a list of values.
	 * This constructor verifies that the possible values may only be "X" or ""
	 * and throws Exception if there is more than two distinct values
	 * and when there are other values than "X" and "".
	 * 
	 * @param name a String representing the name of the Feature.
	 * @param values an ArrayList of String representing the values of the Feature in the product matrix.
	 * @throws Exception an Exception about too many distinct values or non-boolean values.
	 */
	public Feature(String name, ArrayList<String> values) throws Exception {
		super(name, values);
		
		if (this.distinctValues.size() > 3) {
			throw new Exception("[Feature Bad Format] \"" + name + "\" has too many distinct values: " + distinctValues + " (at most 2 expected)");
		}

		Set<String> dv = new HashSet<String>();
		dv.addAll(this.distinctValues);
		dv.remove("X");
		dv.remove("");
		dv.remove("*");

		if (dv.size() != 0) {
			throw new Exception("[Feature Bad Format] \"" + name + "\" has non-boolean values: " + dv + " (should only contain \"X\" and/or \"\")");
		}		
	}
	
	/*
	 * ------ INHERITED METHODS ------
	 */

	@Override
	/**
	 * Applies binary scaling on the current Feature and adds the resulting boolean features to the specified ProductMatrix.
	 * As it is already a binary characteristic, just adds the current Feature to the specified ProductMatrix.
	 * The method returns the specified product matrix extended with the new boolean feature.
	 * @param pm the instance of ProductMatrix in which the Feature has to be added.
	 */
	public ProductMatrix scaling(ProductMatrix pm) {
		try {
			if (this.values.contains("*")) {
				ArrayList<String> values2 = new ArrayList<String>();
				for (String v : values) {
					if (v.equals("*")) {
						values2.add("");
					} else {
						values2.add(v);
					}
				}
				pm.addCharacteristic(new Feature(this.name, values2));
			} else {
				pm.addCharacteristic(new Feature(this.name, this.values));
			}
			return pm;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	/**
	 * Returns the type of the Feature.
	 * @return a String representing the type of the characteristics (here "Boolean Feature").
	 */
	public String getType() {
		return CharacteristicType.featureType.getName();
	}

}
