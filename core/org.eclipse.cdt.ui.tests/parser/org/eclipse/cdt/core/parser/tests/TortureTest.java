/*******************************************************************************
 * Copyright (c) 2001 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.cdt.core.parser.ILineOffsetReconciler;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.dom.DOMBuilder;
import org.eclipse.core.runtime.Path;


/**
 * @author vmozgin
 *
 * Automated parser test framework, to use with GCC testsuites
 */
public class TortureTest extends FractionalAutomatedTest {
	
	static protected boolean isEnabled = false;
	static protected boolean quickParse = true;

	public TortureTest () {
		super();
	}

	public TortureTest (String name) {
		super(name);
	}
	
	protected AutomatedFramework newTest (String name){
		return new TortureTest (name);
	}
	
	protected void loadProperties() throws Exception{
		String resourcePath = org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.ui.tests").find(new Path("/")).getFile();
		resourcePath += "/parser/org/eclipse/cdt/core/parser/resources";
	
		try {
			FileInputStream propertiesIn = new FileInputStream(resourcePath + "/TortureTest.properties");
			properties.load (propertiesIn);
		
			isEnabled = properties.getProperty("enabled", "false").equalsIgnoreCase("true");
			quickParse = properties.getProperty("quickParse", "true").equalsIgnoreCase("true");
			
			String sourceInfo = properties.getProperty("source", "");
			
			stepSize = Integer.parseInt(properties.getProperty("stepSize", "25000"));
			outputFile = properties.getProperty("outputFile", "");
			timeOut = Integer.parseInt(properties.getProperty("timeOut", "60000"));
			outputDir = properties.getProperty("outDir", "");
			
			if (sourceInfo.equals(""))
				throw new FileNotFoundException();
			else {
				StringTokenizer tokenizer = new StringTokenizer(sourceInfo, ",");
				String str = null, val = null;
				try {
					while (tokenizer.hasMoreTokens()) {
						str = tokenizer.nextToken().trim();
						val = tokenizer.nextToken().trim();
					
						testSources.put(str, val);
					}
				} catch (NoSuchElementException e){
					//only way to get here is to have a missing val, assume cpp for that str
					testSources.put(str, "cpp");
				}
			
			}
		} catch (FileNotFoundException e){
			testSources.put(resourcePath + "/torture", "cpp");
		}
		
		if (!isEnabled) testSources.clear();
	}
	
	
	public static Test suite()
	{
		AutomatedFramework frame = new TortureTest();		
		return frame.createSuite();
	}
	
	
	static protected void reportException (Throwable e, String file, IParser parser, ILineOffsetReconciler mapping){
		String output = null;
		int lineNumber = -1;
		
		try {
			lineNumber = mapping.getLineNumberForOffset(parser.getLastErrorOffset());
		} catch (Exception ex) {}
		
		if (e instanceof AssertionFailedError) {
			output = file + ": Parse failed on line ";
			output += lineNumber + "\n";
		} else {
			output = file + ": " + e.getClass().toString();
			output += " on line " + lineNumber + "\n";
		}
		try {
		if (report != null) {
			report.write(output.getBytes());
		}
		} catch (IOException ex) {}

		fail(output);
	}
	
	
	static protected boolean isExpectedToPass (String testCode) 
	{
		// Process some DejaGNU instructions	
		if (testCode.indexOf("{ dg-do run") >= 0) return true;
		if (testCode.indexOf("{ dg-do link") >= 0) return true;
		if (testCode.indexOf("{ dg-error") >= 0) return false;
		if (testCode.indexOf("// ERROR") >= 0) return false;
		if (testCode.indexOf("- ERROR") >= 0) return false;
		if (testCode.indexOf("// XFAIL") >= 0) return false;
		if (testCode.indexOf("{ xfail") >= 0) return false;
		if (testCode.indexOf("{ dg-preprocess") >= 0) return false;
		
		return true;
	}
	
	
	public void doFile() throws Throwable {
		assertNotNull (fileList);
		
		File file = (File)fileList.removeFirst();
		FileInputStream stream = new FileInputStream(file);

		String filePath = file.getCanonicalPath();
		String nature = (String)natures.get(filePath);
	
		StringWriter code = new StringWriter(); 
			
		byte b[] = new byte[stepSize]; 
		int n = stream.read(b);
		while( n != -1 ){
			code.write(new String(b));
			n = stream.read(b);
		}
		
		String testCode = code.toString();
		
		if (isExpectedToPass(testCode)) {
			ParseThread thread = new ParseThread();

			thread.quickParse = quickParse;
			thread.code = testCode;
			thread.cppNature = nature.equalsIgnoreCase("cpp");
			thread.file = filePath;
			
			thread.start();
			thread.join(timeOut);

			if (thread.isAlive()) {
				thread.stop();
				reportHang(testCode, filePath);
			} else if (thread.result != null) {
				reportException(thread.result, filePath, thread.parser, thread.mapping );
			}
		} else {
			// gcc probably didn't expect this test to pass.
			// It doesn't mean that it should pass CDT parser,
			// as it is more relaxed
			// Result - 'inconclusive', but we report 'pass'
			assertTrue(true);                                                                         
		}
	}
	
			
	
	static class ParseThread extends Thread {
		public String 		code;
		public boolean 		cppNature;
		public String 		file;
		public Throwable 	result = null;
		public IParser 		parser = null;
		public boolean 		quickParse = true;
		public ILineOffsetReconciler mapping = null;
	
		public void run(){
			try {           
				DOMBuilder domBuilder = new DOMBuilder(); 
				IParser parser = ParserFactory.createParser( 
					ParserFactory.createScanner( new StringReader( code ), null, null, null, ParserMode.QUICK_PARSE ), nullCallback, ParserMode.QUICK_PARSE);
				mapping = ParserFactory.createLineOffsetReconciler( new StringReader( code ) );
	
				parser.setCppNature(cppNature);
	            
				assertTrue(parser.parse());
			} 
			catch( Throwable e )
			{
				result = e;				
			}
		}
	}
}
