package ontology;

import java.util.ArrayList;
import java.util.HashSet;

import productdescription.Attribute;
import util.Node;

/**
 * This class represent an ontology, i.e., a sub semi-lattice of values.
 * It is independent of the type of the values and thus of the similarity operator.
 * 
 * @author Jessie Galasso Carbonnel
 *
 */
public abstract class AbstractTaxonomy {

	/*
	 * ------ ATTRIBUTES ------
	 */
	
	/**
	 * The attribute for which we build an ontology
	 */
	protected Attribute attribute;
	
	/**
	 * The list of value sets organised in the ontology.
	 * It contains all values of the attribute, 
	 * and possibly other values obtained with a similarity operation applied on each pair of values.
	 */
	protected ArrayList<HashSet<String>> ontologyValues = new ArrayList<HashSet<String>>();
	
	/**
	 * The root of the sub semi-lattice representing the ontology.
	 */
	protected Node root;
	
	
	/*
	 * ------ CONSTRUCTORS ------
	 */
	
	/**
	 * Creates a new ontology depending on the specified attribute.
	 * @param attribute an attribute of any type (except Feature).
	 */
	public AbstractTaxonomy(Attribute attribute) {
		this.attribute = attribute;		
	}

	
	/*
	 * ------ GETTERS AND SETTERS ------
	 */
	
	public String getType() {
		return attribute.getType();
	}

	public ArrayList<HashSet<String>> getOntologyValues(){
		return this.ontologyValues;
	}
	
	/*
	 * ------ ABSTRACT METHODS ------
	 */
	
	/**
	 * Ths methods computes the missing values necessary for building the sub semi-lattice representing the ontology.
	 * These missing values are computed by applying a similarty operator on each pair of attribute values.
	 * This ensures that all pairs of values have a similarity.
	 */
	public abstract void computeMissingOntologyValues();
	
	
	/**
	 * This methods states it a node n1 of the sub semi-lattice subsumes another node n2.
	 * 
	 * @param n1 a node of the sub semi-lattice
	 * @param n2 a node of the sub semi-lattice
	 * @return true if n1 <= n2, else false.
	 */
	public abstract boolean subsume(Node n1, Node n2);
	
	/*
	 * ------ METHODS ------
	 */
	
	/**
	 * This method computes the sub semi-lattice starting from the ontology values.
	 */
	public void computeHierarchy() {
		
		/*
		 * Creates the node parent representing the dissimilarity value;
		 */
		
		this.root = new Node();
		HashSet<String> dissimilarityValue = new HashSet<String>();
		dissimilarityValue.add("*");
		root.setValue(dissimilarityValue);
		root.addParent(null);
		
		/*
		 * Creates the hierarchy
		 */
		
		for (HashSet<String> values : ontologyValues) {
			addNodeInHierarchy(values);
		}
		
		if (root.getChildren().size() == 1) {
			this.root = root.getChildren().get(0);
		} 
	}
	
	/**
	 * This methods creates a node with the specified values, 
	 * and add it at the correct place in the hierarchy.
	 * 
	 * @param newNodeValues a set of String representing the values associated with the node to add.
	 */
	public void addNodeInHierarchy(HashSet<String> newNodeValues) {
		
		Node nodeToAdd = new Node();
		nodeToAdd.setValue(newNodeValues);
				
		ArrayList<Node> directParents = new ArrayList<Node>();
		
		directParents.addAll(getDirectParents(nodeToAdd, root));
				
		for (Node n : directParents) {
			n.addChild(nodeToAdd);
			nodeToAdd.addParent(n);
		}
	}
	
	/**
	 * This method is a private recursive method retrieving the direct parents of nodeToAdd.
	 * If the node to add subsumes none of the children of p, p is a parent of the node to add.
	 * If the node to add subsumes at least a child of p, p is not a parent of the node to add, 
	 * and the method is called on all children of p that the node to add subsumes.
	 */
	private ArrayList<Node> getDirectParents(Node nodeToAdd, Node p) {
		
		ArrayList<Node> directParents = new ArrayList<Node>();
		
		boolean subsumesAtLeastAChildOfP = false;
		
		for (Node c : p.getChildren()) {
			if (subsume(nodeToAdd, c)) {
				directParents.addAll(getDirectParents(nodeToAdd, c));
				subsumesAtLeastAChildOfP = true;
			}
		}
		
		if (!subsumesAtLeastAChildOfP) {
			directParents.add(p);
		} 
		
		return directParents;
	}
	
	/**
	 * This method returns the node representing the similarity of n1 and n2.
	 * 
	 * @param n1 a node of the sub semi-lattice.
	 * @param n2 a node of the sub semi-lattice.
	 * @return the node representing the similarity of the two specified nodes.
	 */
	public Node similarity(Node n1, Node n2) {
		Node similarity = null;
		
		if (subsume(n1, n2)) {
			return n2;
		} else if(subsume(n2, n1)) {
			return n1;
		} else {
		
			boolean similarityFound = false;
			ArrayList<Node> supNodesToCheck = new ArrayList<Node>();
			ArrayList<Node> newNodesToCheck = new ArrayList<Node>();
			supNodesToCheck.addAll(n1.getParents());
			
			while (!similarityFound) {
				for (Node n : supNodesToCheck) {
					if (subsume(n2, n)) {
						similarity = n;
						similarityFound = true;
					} else {
						newNodesToCheck.addAll(n.getParents());
					}
				}
				supNodesToCheck.clear();
				supNodesToCheck.addAll(newNodesToCheck);
				newNodesToCheck.clear();
			}
			return similarity;
		}
	}
	
	
	/**
	 * This method returns a String representing the hierarchy as follows :
	 * "[parentNode] -> [list of child nodes]".
	 * @return a String representing the sub semi-lattice.
	 */
	public String hierarchyToString(){
		
		return nodeToString(root);
	}
	
	/**
	 * Private recursive method to visit all the nodes of the sub semi-lattice.
	 * @param n a node
	 * @return a String representing the specified node and its children.
	 */
	private String nodeToString(Node n) {
		String result = n + " -> " + n.getChildren() + "; ";
		
		for (Node c : n.getChildren()) {
			result += nodeToString(c);
		}
		return result;
	}
	
	
	public Node getNodeOfValues(HashSet<String> values) {
		Node n = searchChildNode(root, values);
		if (n == null) {
			System.err.println("Node with value " + values + " not find.");
			return null;
		} else {
			return n;
		}
	}
	
	private Node searchChildNode(Node n, HashSet<String> values) {
		if (n.getValue().equals(values)) {
			return n;
		} 
		
		for (Node child : n.getChildren()) {
			if (searchChildNode(child, values) != null) {
				return searchChildNode(child, values);
			}
		}
		
		return null;
	}
	
	
	public Node getRoot() {
		return this.root;
	}
	
	
	
	
}
