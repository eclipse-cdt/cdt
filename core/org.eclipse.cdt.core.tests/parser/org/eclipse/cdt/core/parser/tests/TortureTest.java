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
import java.io.StringWriter;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
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
		String resourcePath = org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.core.tests").find(new Path("/")).getFile(); //$NON-NLS-1$ //$NON-NLS-2$
		resourcePath += "resources/parser/TortureTest"; //$NON-NLS-1$
	
		try {
			FileInputStream propertiesIn = new FileInputStream(resourcePath + "/TortureTest.properties"); //$NON-NLS-1$
			properties.load (propertiesIn);
		
			isEnabled = properties.getProperty("enabled", "false").equalsIgnoreCase("true"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			quickParse = properties.getProperty("quickParse", "true").equalsIgnoreCase("true"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
			String sourceInfo = properties.getProperty("source", ""); //$NON-NLS-1$ //$NON-NLS-2$
			
			stepSize = Integer.parseInt(properties.getProperty("stepSize", "25000")); //$NON-NLS-1$ //$NON-NLS-2$
			outputFile = properties.getProperty("outputFile", ""); //$NON-NLS-1$ //$NON-NLS-2$
			timeOut = Integer.parseInt(properties.getProperty("timeOut", "60000")); //$NON-NLS-1$ //$NON-NLS-2$
			outputDir = properties.getProperty("outDir", ""); //$NON-NLS-1$ //$NON-NLS-2$
			
			if (sourceInfo.equals("")) //$NON-NLS-1$
				throw new FileNotFoundException();

			StringTokenizer tokenizer = new StringTokenizer(sourceInfo, ","); //$NON-NLS-1$
			String str = null, val = null;
			try {
				while (tokenizer.hasMoreTokens()) {
					str = tokenizer.nextToken().trim();
					val = tokenizer.nextToken().trim();
				
					testSources.put(str, val);
				}
			} catch (NoSuchElementException e){
				//only way to get here is to have a missing val, assume cpp for that str
				testSources.put(str, "cpp"); //$NON-NLS-1$
			}
		} catch (FileNotFoundException e){
			testSources.put(resourcePath, "cpp"); //$NON-NLS-1$
		}
		
		if (!isEnabled) testSources.clear();
	}
	
	
	public static Test suite()
	{
		AutomatedFramework frame = new TortureTest();		
		return frame.createSuite();
	}
	
	
	static protected void reportException (Throwable e, String file, IParser parser){
		String output = null;
		int lineNumber = -1;
		
		lineNumber = parser.getLastErrorLine();
				
		if (e instanceof AssertionFailedError) {
			output = file + ": Parse failed on line "; //$NON-NLS-1$
			output += lineNumber + "\n"; //$NON-NLS-1$
		} else {
			output = file + ": " + e.getClass().toString(); //$NON-NLS-1$
			output += " on line " + lineNumber + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
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
		if (   fileName.equals("init-2.c") //$NON-NLS-1$
			|| fileName.equals("init-3.c") //$NON-NLS-1$
			|| fileName.equals("struct-ini-4.c")) { //$NON-NLS-1$
				
				// gcc-specific (and deprecated) designated initializers
				// struct { int e1, e2; } v = { e2: 0 };
				
				return false;
		}
		
		if (   fileName.equals("stmtexpr3.C")) { //$NON-NLS-1$
		
				// statements in expressions
				// B() : a(({ 1; })) {}
		
				return false;
		}
		
		if (   fileName.equals("widechar-1.c")) { //$NON-NLS-1$
		
				// concatenation of incompatible literals
				// char *s = L"a" "b";

				return false;
		}
		
		if (   fileName.equals("bf-common.h") //$NON-NLS-1$
		    || fileName.equals("class-tests-1.h") //$NON-NLS-1$
			|| fileName.equals("unclaimed-category-1.h")) { //$NON-NLS-1$
		
				// ObjectiveC header file

				return false;
		}
		
		// Process some DejaGNU instructions	
		if (testCode.indexOf("{ dg-error") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("// ERROR") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("- ERROR") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("// XFAIL") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("- XFAIL") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("{ xfail") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("{ dg-preprocess") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("{ dg-do preprocess") >= 0) return false; //$NON-NLS-1$
	
		// gcc extensions
		if (testCode.indexOf("__attribute") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("__extension") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("__restrict") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("__const") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("__declspec") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("__alignof") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("__label") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("__real") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("__imag") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("extern template") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("inline template") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("static template") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("typeof") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf(" asm") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf(") return") >= 0) return false; //$NON-NLS-1$
		if (testCode.indexOf("#ident") >= 0) return false; //$NON-NLS-1$
		
		// These are expected errors (not marked in the code)
		if (testCode.indexOf("#include_next") >= 0) return false; //$NON-NLS-1$
		
		// Long long literals are part of ANSI C99
		// if (containsLongLongLiterals(testCode)) return false;
	
		if (testCode.indexOf("{ dg-do run") >= 0) return true; //$NON-NLS-1$
		if (testCode.indexOf("{ dg-do link") >= 0) return true; //$NON-NLS-1$
		
		return true;
	}
	
	
	public void doFile() throws Throwable {
		int timeOut = FractionalAutomatedTest.timeOut;
		
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
		
		if ( file.getName().equals("concat1.C")) { //$NON-NLS-1$
			// This is a really time-consuming test,
			// override timeout
			timeOut = 600000;
		}
		
		if (isExpectedToPass(testCode, file)) {
			ParseThread thread = new ParseThread();

			thread.quickParse = quickParse;
			thread.code = testCode;
			thread.cppNature = nature.equalsIgnoreCase("cpp"); //$NON-NLS-1$
			thread.file = filePath;
			
			thread.start();
			thread.join(timeOut);

			if (thread.isAlive()) {
				thread.stop();
				reportHang(testCode, filePath);
			} else if (thread.result != null) {
				reportException(thread.result, filePath, thread.parser);
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
	
		public void run(){
			try {           
				ParserMode parserMode = quickParse ? ParserMode.QUICK_PARSE : ParserMode.COMPLETE_PARSE;
				ParserLanguage language = cppNature ? ParserLanguage.CPP : ParserLanguage.C; 
				parser = ParserFactory.createParser( 
						ParserFactory.createScanner( new CodeReader( code.toCharArray() ), new ScannerInfo(), parserMode, language, nullCallback, new NullLogService(), null ), nullCallback, parserMode, language, null);
		
				assertTrue(parser.parse());
			} 
			catch( Throwable e )
			{
				result = e;				
			}
		}
	}
}
