package main;

import productdescription.ProductMatrix;
import variabilityextraction.AllBinaryImplicationExtractor;
import variabilityextraction.BinaryImplicationExtractor;
import variabilityextraction.CooccurrenceExtractor;
import variabilityextraction.MutexExtractor;

public class testExtraction {

	public static void main(String[] args) {
		long currentTime = java.lang.System.currentTimeMillis();

		ProductMatrix pm = new ProductMatrix("jhipster3.6.1-testresults_2000.csv");
		
		String path = "data/" + pm.getName() + "/";
		
		pm.computeLattice();
		
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
