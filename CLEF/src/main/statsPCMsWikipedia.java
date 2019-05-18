package main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import multivaluedcontext.MultivaluedContext;
import relationshipextraction.AllBinaryImplicationExtractor;
import relationshipextraction.BinaryImplicationExtractor;
import relationshipextraction.CooccurrenceExtractor;
import relationshipextraction.MutexExtractor;

public class statsPCMsWikipedia {
	
	public static void main(String[] args) {
	
		ArrayList<String> pcmList = new ArrayList<>();
		
		try {
			
			Files.lines(Paths.get("data/list_pcm"))
			.map(line -> line.split("\\n")) 
			.flatMap(Arrays::stream) 					
            .distinct()
            .forEach(s -> pcmList.add(s));
			
			String csv = "name, RM, M, RTC, TC, RTR, TR, C\n";
			
			for (String pcm : pcmList) {
				
				MultivaluedContext pm = new MultivaluedContext(pcm);
				String path = "data/" + pm.getName() + "/";
				pm.computeLattice();
				
				csv += pcm + ",";
				
				MutexExtractor me = new MutexExtractor(path);
				csv += me.computeRelationships() + ",";
				
				AllBinaryImplicationExtractor abie = new AllBinaryImplicationExtractor(path);
				csv += abie.computeRelationships() + ",";
				
				BinaryImplicationExtractor bie = new BinaryImplicationExtractor(path);
				csv += bie.computeRelationships() + ",";
				
				CooccurrenceExtractor ce = new CooccurrenceExtractor(path);
				csv += ce.computeRelationships() + "\n";
								
				bie.exportsInTextFile();
				ce.exportsInTextFile();
				abie.exportsInTextFile();
				me.exportsInTextFile();
			}
			
			System.out.println(csv);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

}
