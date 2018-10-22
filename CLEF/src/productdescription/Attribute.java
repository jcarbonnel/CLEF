package productdescription;

import java.util.ArrayList;
import java.util.HashSet;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import ontology.*;
import sun.net.NetworkServer;
import util.Node;

/**
 * This class represents a multi-valued attribute, a sub-class of characteristic.
 * 
 * An Attribute has a name and a list of values.
 * It also has a type, which is either Integer, Double or Literal (see {@link productdescription.CharacteristicType}). * 
 * 
 * @author Jessie Galasso Carbonnel
 *
 */
public class Attribute extends Characteristic {

	/*
	 * ------- ATTRIBUTES ------
	 */
	
	/**
	 * The type of the attribute.
	 * It is either CharacteristicType.integerType, CharacteristicType.doubleType or CharacteristicType.literalType.
	 */
	private CharacteristicType type;
	
	private AbstractTaxonomy ontology;
	
	
	/*
	 * ------- CONSTRUCTORS ------
	 */
	
	/**
	 * Creates an Attribute with a name and a list of values.
	 * This constructor analyses the values to identify the type of the attribute values.
	 * 
	 * @param name a String representing the name of the attribute.
	 * @param values an ArrayList of String representing the values of the characteristic in the product matrix.
	 */
	public Attribute(String name, ArrayList<String> values) {
		
		super(name, values);
		

		boolean isInteger = true;
		for(String v : this.distinctValues){
			if(!v.matches("\\*|[0-9]+")){
				isInteger = false;
			}
		}
		
		boolean isDouble = true;
		for(String v : this.distinctValues){
			if(!v.matches("\\*|[0-9]+\\.?[0-9]*")){
				isDouble = false;
			}
		}
		
		this.type = (isInteger? CharacteristicType.integerType : 
			(isDouble ? CharacteristicType.doubleType : CharacteristicType.literalType));
		
		//System.out.println("Creation of attribute \"" + this.name + "\" of type " + type.getName() + ".");
	}

	/*
	 * ------ IMPLEMENTED METHODS ------
	 */

	/**
	 * Applies binary scaling on the current attribute, which produces a set of boolean features ({@link productdescription.Feature}).
	 * The number of produced boolean features corresponds to the number of distinct values of the attribute.
	 * These features are added to the specified product matrix.
	 * The method returns the specified product matrix extended with the new boolean features.
	 * @param pm the instance of ProductMatrix in which the instances of Feature resulting from the binary scaled characteristic have to be added.
	 */
	@Override
	public ProductMatrix scaling(ProductMatrix pm) {
		
		try {

			System.out.println("Scaling attribute " + name);
			
			/*
			 * Creates the corresponding ontology
			 * TODO: abstract factory?
			 */
			
			if (this.getType().equals("Literal Attribute")) {
				this.ontology = new LiteralTaxonomy(this);
			} else if (this.getType().equals("Double Attribute")) {
				this.ontology = new DefaultDoubleTaxonomy(this);
			} else {
				this.ontology = new DefaultIntegerTaxonomy(this);
			}
			
			ontology.computeMissingOntologyValues();
			ontology.computeHierarchy();

			
			/*
			 * Creates the feature corresponding to each ontology value
			 */
			
			for (HashSet<String> ontologyValueSet : ontology.getOntologyValues()) {

				/*
				 * Computes the feature name
				 */
				
				String valuesAsString = "";
				
				for (String value : ontologyValueSet) {
					valuesAsString += value + ",";
				}
				valuesAsString = valuesAsString.substring(0, valuesAsString.length() - 1);
				
				if (this.getType().equals("Literal Attribute")) {
					valuesAsString = "{" + valuesAsString + "}";
				}
						
				String newFeatureName = this.name + ":=" + valuesAsString;
				
			
				/*
				 * Computes the feature list of values
				 */
				
				ArrayList<String> newFeatureValueList = new ArrayList<String>();
				
				for (String productValue : this.values) {
					
					HashSet<String> productValueSet = new HashSet<String>();
					for (String v : productValue.split(";")) {
						productValueSet.add(v);
					}
					
					/*
					 * Special case of the dissimilarity value *
					 */
					if (productValueSet.contains("*") && !ontology.getRoot().getValue().equals("*")) {
						
						newFeatureValueList.add("");
						
					} else if (ontology.subsume(ontology.getNodeOfValues(productValueSet), ontology.getNodeOfValues(ontologyValueSet))) {
						
						newFeatureValueList.add("X");
						
					} else {
						
						newFeatureValueList.add("");
						
					}					
				}
				
				pm.addCharacteristic(new Feature(newFeatureName, newFeatureValueList));
				
			}
			
			/*
			 * The special case of the dissimilarity value 
			 */
		
			if (!ontology.getRoot().getValue().contains("*") && this.values.contains("*")) {
				
				String newFeatureName = this.name + ":=*";
				ArrayList<String> newFeatureValueList = new ArrayList<String>();
				
				for (String productValue : this.values) {
					
					if (productValue.equals("*")) {
						newFeatureValueList.add("X");
					} else {
						newFeatureValueList.add("");
					}					
				}
				
				pm.addCharacteristic(new Feature(newFeatureName, newFeatureValueList));
			}
			
			
			return pm;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	/**
	 * Returns a String representing the type of the attribute:
	 * "Integer Attribute", "Double Attribute" or "Literal Attribute".
	 */
	public String getType() {
		
		return type.getName();
	}

}
