package org.eclipse.cdt.internal.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * generates the parser from the CPLUSPLUS.jj file. after running, import the sources
 * from the given temporary output directory
 * needs javacc in the class path (www.metamata.com)
 * version used for this release 1.2
 */
public class RunParserGenerator {
	
	private static final String tempOutputDir="c:\\temp\\jccout";
	
	
	public static void main(String[] args) {
	/*	URL url= (new RunParserGenerator()).getClass().getResource("/com/ibm/cdt/parser/generated/CPLUSPLUS.jj");
		File file= new File(url.getFile());
		
		String[] arguments= new String[] {
			"-OUTPUT_DIRECTORY=" + tempOutputDir,
			file.getPath()
		};
		try {
			System.out.println("start javacc...");
			COM.sun.labs.javacc.Main.main(arguments);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("javacc finished..."); */
	}
		
	


}