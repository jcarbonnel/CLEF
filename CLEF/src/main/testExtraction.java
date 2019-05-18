package main;

import multivaluedcontext.MultivaluedContext;
import relationshipextraction.AllBinaryImplicationExtractor;
import relationshipextraction.BinaryImplicationExtractor;
import relationshipextraction.CooccurrenceExtractor;
import relationshipextraction.MutexExtractor;

public class testExtraction {

	public static void main(String[] args) {
		long currentTime = java.lang.System.currentTimeMillis();

		MultivaluedContext pm = new MultivaluedContext("RobocodeSPL_botsDocumentedInRobowiki.csv");
		
		String path = "data/" + pm.getName() + "/";
		
		pm.computeLattice();
		
		//pm.printContext();
		
		long timeToComputeACposet = java.lang.System.currentTimeMillis() - currentTime;
		
		BinaryImplicationExtractor bie = new BinaryImplicationExtractor(path);
		bie.computeRelationships();
		
		AllBinaryImplicationExtractor abie = new AllBinaryImplicationExtractor(path);
		abie.computeRelationships();
		
		CooccurrenceExtractor ce = new CooccurrenceExtractor(path);
		ce.computeRelationships();
		
		MutexExtractor me = new MutexExtractor(path);
		me.computeRelationships();
		
		bie.exportsInTextFile();
		ce.exportsInTextFile();
		abie.exportsInTextFile();
		me.exportsInTextFile();
		
		long timeToComputeRelationships = java.lang.System.currentTimeMillis() - currentTime - timeToComputeACposet;

		System.out.println("Time to compute the AC-poset: " + timeToComputeACposet);
		System.out.println("Time to compute the relationships: " + (timeToComputeRelationships));
	}

}
