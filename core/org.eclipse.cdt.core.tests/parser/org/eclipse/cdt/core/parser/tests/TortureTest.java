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
import org.eclipse.cdt.internal.core.parser.ScannerInfo;
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
		String resourcePath = org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile();
		resourcePath += "resources/parser/TortureTest";
	
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
			testSources.put(resourcePath, "cpp");
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

	
	static protected boolean isExpectedToPass (String testCode, File file) 
	{	
		String fileName = file.getName();
		
		// Filter out gcc-specific tests that are not easy to detect automatically
		if (   fileName.equals("init-2.c")
			|| fileName.equals("init-3.c")
			|| fileName.equals("struct-ini-4.c")) {
				
				// gcc-specific (and deprecated) designated initializers
				// struct { int e1, e2; } v = { e2: 0 };
				
				return false;
		}
		
		if (   fileName.equals("stmtexpr3.C")) {
		
				// statements in expressions
				// B() : a(({ 1; })) {}
		
				return false;
		}
		
		if (   fileName.equals("widechar-1.c")) {
		
				// concatenation of incompatible literals
				// char *s = L"a" "b";

				return false;
		}
		
		if (   fileName.equals("bf-common.h")
		    || fileName.equals("class-tests-1.h")
			|| fileName.equals("unclaimed-category-1.h")) {
		
				// ObjectiveC header file

				return false;
		}
		
		// Process some DejaGNU instructions	
		if (testCode.indexOf("{ dg-error") >= 0) return false;
		if (testCode.indexOf("// ERROR") >= 0) return false;
		if (testCode.indexOf("- ERROR") >= 0) return false;
		if (testCode.indexOf("// XFAIL") >= 0) return false;
		if (testCode.indexOf("- XFAIL") >= 0) return false;
		if (testCode.indexOf("{ xfail") >= 0) return false;
		if (testCode.indexOf("{ dg-preprocess") >= 0) return false;
		if (testCode.indexOf("{ dg-do preprocess") >= 0) return false;
	
		// gcc extensions
		if (testCode.indexOf("__attribute") >= 0) return false;
		if (testCode.indexOf("__extension") >= 0) return false;
		if (testCode.indexOf("__restrict") >= 0) return false;
		if (testCode.indexOf("__const") >= 0) return false;
		if (testCode.indexOf("__declspec") >= 0) return false;
		if (testCode.indexOf("__alignof") >= 0) return false;
		if (testCode.indexOf("__label") >= 0) return false;
		if (testCode.indexOf("__real") >= 0) return false;
		if (testCode.indexOf("__imag") >= 0) return false;
		if (testCode.indexOf("extern template") >= 0) return false;
		if (testCode.indexOf("inline template") >= 0) return false;
		if (testCode.indexOf("static template") >= 0) return false;
		if (testCode.indexOf("typeof") >= 0) return false;
		if (testCode.indexOf(" asm") >= 0) return false;
		if (testCode.indexOf(") return") >= 0) return false;
		if (testCode.indexOf("#ident") >= 0) return false;
		
		// These are expected errors (not marked in the code)
		if (testCode.indexOf("#include_next") >= 0) return false;
		
		// Long long literals are part of ANSI C99
		// if (containsLongLongLiterals(testCode)) return false;
	
		if (testCode.indexOf("{ dg-do run") >= 0) return true;
		if (testCode.indexOf("{ dg-do link") >= 0) return true;
		
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
		
		if (isExpectedToPass(testCode, file)) {
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
				reportException(thread.result, filePath, thread.parser, thread.mapping);
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
		public ILineOffsetReconciler mapping = null;
        public String 		code;
		public boolean 		cppNature;
		public String 		file;
		public Throwable 	result = null;
		public IParser 		parser = null;
		public boolean 		quickParse = true;
	
		public void run(){
			try {           
				DOMBuilder domBuilder = new DOMBuilder(); 
				parser = ParserFactory.createParser( 
						ParserFactory.createScanner( new StringReader( code ), null, new ScannerInfo(), ParserMode.QUICK_PARSE, nullCallback ), nullCallback, ParserMode.QUICK_PARSE);
		
				parser.setCppNature(cppNature);
				mapping = ParserFactory.createLineOffsetReconciler( new StringReader( code ) );
	            
				assertTrue(parser.parse());
			} 
			catch( Throwable e )
			{
				result = e;				
			}
		}
	}
}
