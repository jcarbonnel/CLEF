package similarityoperator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import productdescription.ProductDescription;

/**
 * This class is an implementation of a similarity operator based on intervals of Double values.
 * The number of class intervals is automatically computed using the Sturgis formula.
 * The created patterns are all intervals, i.e., the initial Double values are not kept as patterns.
 * 
 * @author Jessie Carbonnel
 *
 */
public class DoubleIntervalSimilarityOperator extends SimilarityOperator {

	/*
	 * ---------- CONSTRUCTORS -----------
	 */
	
	/**
	 * Creates an instance of Double Interval Similarity Operator referencing the Product Description 
	 * and the attribute of type double on which similarities based on intervals have to be computed.
	 * 
	 * @param d any kind of product description.
	 * @param a the attribute of type Integer.
	 */
	public DoubleIntervalSimilarityOperator(ProductDescription d, String a) {
		super(d, a);
	}

	/*
	 * ---------- INHERITED METHODS ----------
	 */
	
	@Override
	/**
	 * Computes the similarities, and add them as binary features in a copy of the product description.
	 * Stores the content of the dot file.
	 * 
	 * @return a copy of product description with the similarities of the attribute as binary features.
	 */
	public ProductDescription scaling() {

		// Writing the beginning of dot file
		this.dot="";
		this.dot+="digraph G { \n rankdir=BT; \n subgraph ba { \n";
		
		// Array lists used to construct the meet-semilattice
		ArrayList<String> array1 = new ArrayList<String>();
		ArrayList<String> array2 = new ArrayList<String>();
		
		// Computes the number of class intervals and their range
		values.sort(null);
		
		// printing the number of values
		System.out.println("      -> "+values.size()+" values found");
		
		// printing the values
		System.out.println("      -> "+values);
		
		values.remove("*");
		Double min = Double.parseDouble(values.get(0));
		Double max = Double.parseDouble(values.get(values.size() - 1));
		int nbClassInterval = (int) Math.ceil(1 + 3.3 * Math.log10(values.size()));
		Double range = (max - min) / nbClassInterval;
		range = BigDecimal.valueOf(range)
			    .setScale(1, RoundingMode.HALF_UP)
			    .doubleValue();
		
			
		// Computes the pattern values of the first floor
		// Adds them as new characteristics in the description
		// And writes them in the dot files
		for (int i = 0 ; i < nbClassInterval ; i++) {
			
			array2.add("["+ min + "-" + (min + range) + "]");
			
			description.addCharacteristic(attribute + ":["+ min + "-" + (min + range) + "]");
			
			this.dot += String.valueOf(i+1);
			this.dot += " [shape=record,label=\"{" + "["+ min + "-" + (min + range) + "]"+"}\"]; \n";
			
			min += range;
		}
				
		int sizeArray = 0;
		
		/** Automating generation until the end **/
		
		boolean finish = false;
		while(!finish){
			
			//System.out.println(array2);
			
			// array for floor n
			array1.clear();
			array1.addAll(array2);
			
			// array for floor n+1
			array2.clear();
			sizeArray += array1.size();
			
			
			/** Computing interval of n+1 floor **/
			
    		for (int i = 0 ; i < (array1.size()-1) ; i++) {
    			String i1 = array1.get(i).substring(1 , array1.get(i).indexOf("-"));
    			String i2 = array1.get(i+1).substring((array1.get(i+1).indexOf("-")+1) , (array1.get(i+1).length()-1));
    			array2.add("[" + i1 + "-" + i2 + "]");
    		}
    		
    		/** Writing n+1 floor **/
    		
    		for (int i = 0 ; i < array2.size() ; i++) {
    			this.dot += (i + 1 + sizeArray) + " [shape=record,label=\"{" + array2.get(i) + "}\"];\n";
    			
    			// adding the new obtained patterns as new columns in the product description for later scaling
    			description.addCharacteristic(attribute + ":" + array2.get(i));
    		}
    		
    		/** Writing arrows between the two floors **/
    		
    		for (int i = 0 ; i < array2.size() ; i++) {
    			this.dot += ( i + 1 + (sizeArray-array1.size())) + " -> " + (i + 1 + sizeArray) + "\n";
    			this.dot += (i + 2 + (sizeArray-array1.size())) + " -> " + (i + 1 + sizeArray) + "\n";	
    		}
    		
    		// stop the recursive treatment if the floor n+1 is the last one, i.e., it contains only one pattern
     		finish = (array2.size() == 1) ? true : false;
    		
     		// if the last floor has been processed, we add the dissimilarity pattern 
    		if(finish){
    			sizeArray += array2.size();
    			this.dot += (1 + sizeArray) + " [shape=record,label=\"{*}\"];\n";
        		this.dot += (sizeArray + " -> " + (sizeArray + 1));
    		}
			
		}
		
		// now adding the dissimilarity pattern as a new column 
		description.addCharacteristic(attribute + ":*");
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
				
				// If the characteristic represents a pattern of "attribute"
				if ( c.length() > attribute.length() && c.substring(0,attribute.length()).equals(attribute)) {
				
					// For each value of the corresponding cell
					for (String s : description.getValues(p, attribute).split(";")) {		
						
						// if the name of the characteristic c represents an interval 
						// we check if the processed value s is in the interval
						// then ok = true
						boolean ok = false;
						if (c.substring(c.length()-1,c.length()).equals("]") && !s.equals("*")) {
							
							double v = Double.parseDouble(s);
							String inter = c.substring(c.indexOf("["),c.length());
							//System.out.println(inter);
							
							// retrieving the first value of the interval
							double first = Double.parseDouble(inter.substring(1,inter.indexOf("-")));
							
							// retrieving the second value of the interval
							double second = Double.parseDouble(inter.substring(inter.indexOf("-")+1,inter.length()-1));
							
							// if the processed value is in the interval
							if (v>=first && v <=second) {
								ok = true;
							}
						}
						
						// if the characteristic corresponds to the value
						// or if it corresponds to the dissimilarity value
						// or if it is in the interval
						// X is added for the corresponding pattern
						if (c.equals(attribute+":"+s) 
								//|| c.equals(attribute+":*")
								|| ok) {
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
