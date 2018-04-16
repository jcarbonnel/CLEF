package similarityoperator;

import java.util.ArrayList;

import productdescription.ProductDescription;

/**
 * This class is an implementation of a similarity operator for String values.
 * It defines that there is no similarity between two values (i.e., the similarity returned is *)
 * 
 * @author Jessie Carbonnel
 *
 */
public class StringDefaultSimilarityOperator extends SimilarityOperator {

	/*
	 * ---------- CONSTRUCTOR ----------
	 */
	
	/**
	 * Creates an instance of String Default Similarity Operator referencing the Product Description 
	 * and the attribute of type String.
	 * 
	 * @param d any kind of product description.
	 * @param a the attribute of type Integer.
	 */
	public StringDefaultSimilarityOperator(ProductDescription d, String a) {
		super(d, a);
	}
	
	/* ---------- INHERITED METHODS ----------

	@Override
	/**
	 * Computes the similarities, and add them as binary features in a copy of the product description.
	 * Stores the content of the dot file.
	 * 
	 * @return a copy of product description with the similarities of the attribute as binary features.
	 */
	public ProductDescription scaling() {
	      	
		// retrieving distinct values of the processed attribute in the array list 'values'
		ArrayList<String> values = new ArrayList<String>();
		values.addAll(this.values);
		
		// printing the number of values
		System.out.println("      -> " + values.size() + " values found");
		
		// printing the values
		System.out.println("      -> " + values);
		
		// adds the distinct values as new characteristics in the product description
		for (String v : values) {
			description.addCharacteristic(attribute+":" + v);
		}
		
		// adds a value/column for dissimilarity description
		if (!values.contains("*")) {
			values.add("*");
			description.addCharacteristic(attribute + ":*");
		}
		
		// writing floors 1 and 2
		this.dot="digraph G { \n rankdir=BT;\n subgraph ba { \n";
		for (int i=0 ; i < values.size() ; i++) {
			this.dot += (i + 1) + " [shape=record,label=\"{" + values.get(i) + "}\"];\n";
		}
		
		// adding arrows in dot file
		for (int i=0 ; i < values.size() ; i++) {
			if (!values.get(i).equals("*")) {
				this.dot += (i + 1) + " -> " + (values.indexOf("*") + 1) + "\n";
			}
		}
		this.dot += "}}";        	
		

		/*
		 * At this point, the dot file is computed
		 * and the obtained patterns are added as new characteristics in the product description.
		 * These new characteristics are not filled for now.
		 */
		
		// For each product of the description
		for (String p : description.getProducts()) {
			
			// For each characteristic of the description
			for (String c : description.getCharacteristics()) {
				
				// If the characteristic is a pattern of "attribute"
				if (c.length() > attribute.length() && c.substring(0,attribute.length()).equals(attribute)) {
					
					// For each value of the corresponding cell
					for (String s : description.getValues(p, attribute).split(";")) {	
						
						// if the pattern c corresponds to a value of p for the characteristic "attribute"
						// or if c corresponds to "*"
						if (c.equals(attribute + ":" + s)) {
							
							// Adds "X" to the corresponding pattern
							description.addValue(p, c, "X");
						}
					}
				}
			}	
		}
		
		// removing the processed characteristic now entirely scaled 
		description.removeCharacteristic(attribute);	
		
		return description;		
	}

}
