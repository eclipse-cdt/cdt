/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.scanner;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;

public class PreprocessorSpeedTest  {
	
	private PrintStream stream;

	public static void main(String[] args) {
		try {
			PrintStream stream = null;
			if (args.length > 0)
				stream = new PrintStream(new FileOutputStream(args[0]));

			new PreprocessorSpeedTest().runTest(stream, 30);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void test() throws Exception {
		runTest(10);
	}

	public void runTest(PrintStream stream, int n) throws Exception {
		this.stream = stream;
		runTest(n);
	}
	
	private void runTest(int n) throws Exception {
		String code = 
			"#include <windows.h>\n" +
			"#include <stdio.h>\n" +
			"#include <iostream>\n";
		
		CodeReader reader = new CodeReader(code.toCharArray());
		IScannerInfo info = getScannerInfo();
		long totalTime = 0;
		for (int i = 0; i < n; ++i) {
			long time = testScan(reader, false, info, ParserLanguage.CPP);
			if (i > 0)
				totalTime += time;
		}
		
		if (n > 1) {
			System.out.println("Average Time: " + (totalTime / (n - 1)) + " millisecs");
		}
	}

	protected long testScan(CodeReader reader, boolean quick, IScannerInfo info, ParserLanguage lang) throws Exception {
		ICodeReaderFactory readerFactory= FileCodeReaderFactory.getInstance();
		IScannerExtensionConfiguration scannerConfig;
	    if (lang == ParserLanguage.C) {
	    	scannerConfig= GCCScannerExtensionConfiguration.getInstance();
	    }
	    else {
	    	scannerConfig= GPPScannerExtensionConfiguration.getInstance();
	    }
		ParserMode mode = ParserMode.COMPLETE_PARSE;
		CPreprocessor cpp= new CPreprocessor(reader, info, lang, new NullLogService(), scannerConfig, readerFactory);
		cpp.getLocationMap().setRootNode(new CPPASTTranslationUnit());
		long startTime = System.currentTimeMillis();
		int count = 0;
		try {
			while (true) {
					IToken t = cpp.nextToken();
					
					if (stream != null)
						stream.println(t.getImage());
					
					if (t == null)
						break;
					++count;
				
			}
		} catch (EndOfFileException e2) {
		}
		long totalTime = System.currentTimeMillis() - startTime;
		System.out.println( "Resulting scan took " + totalTime + " millisecs " +
				count + " tokens");
		return totalTime;
	}
	
	protected IScannerInfo getScannerInfo() {
		String config = System.getProperty("speedTest.config"); 

		if (config == null)
			return mingwScannerInfo();

		if (config.equals("msvc"))
			return msvcScannerInfo();
		else if (config.equals("msvc98"))
			return msvc98ScannerInfo();
		else if (config.equals("ydl"))
			return ydlScannerInfo();
		else
			return mingwScannerInfo();
	}

	private IScannerInfo msvcScannerInfo() {
		Map definitions = new Hashtable();
		//definitions.put( "__GNUC__", "3" );  //$NON-NLS-1$ //$NON-NLS-2$

		String [] includePaths = new String[] {
			"C:\\Program Files\\Microsoft SDK\\Include",
			"C:\\Program Files\\Microsoft Visual C++ Toolkit 2003\\include"
		};
		return new ScannerInfo( definitions, includePaths );
	}

	protected IScannerInfo msvc98ScannerInfo() {
		Map definitions = new Hashtable();
		String [] includePaths = new String[] {
			"C:\\Program Files\\Microsoft Visual Studio\\VC98\\Include"
		};
		return new ScannerInfo( definitions, includePaths );
	}
	
	protected IScannerInfo mingwScannerInfo() {
		// TODO It would be easier and more flexible if we used discovery for this
		Map definitions = new Hashtable();
		definitions.put("__GNUC__", "3");
		definitions.put("__GNUC_MINOR__", "2");
		definitions.put("__GNUC_PATCHLEVEL__", "3");
		definitions.put("__GXX_ABI_VERSION", "102");
		definitions.put("_WIN32", "");
		definitions.put("__WIN32", "");
		definitions.put("__WIN32__", "");
		definitions.put("WIN32", "");
		definitions.put("__MINGW32__", "");
		definitions.put("__MSVCRT__", "");
		definitions.put("WINNT", "");
		definitions.put("_X86_", "1");
		definitions.put("__WINNT", "");
		definitions.put("_NO_INLINE__", "");
		definitions.put("__STDC_HOSTED__", "1");
		definitions.put("i386", "");
		definitions.put("__i386", "");
		definitions.put("__i386__", "");
		definitions.put("__tune_i586__", "");
		definitions.put("__tune_pentium__", "");
		definitions.put("__stdcall", "__attribute__((__stdcall__))");
		definitions.put("__cdecl", "__attribute__((__cdecl__))");
		definitions.put("__fastcall", "__attribute__((__fastcall__))");
		definitions.put("_stdcall", "__attribute__((__stdcall__))");
		definitions.put("_cdecl", "__attribute__((__cdecl__))");
		definitions.put("_fastcall", "__attribute__((__fastcall__))");
		definitions.put("__declspec(x)", "__attribute__((x))");
		definitions.put("__DEPRECATED", "");
		definitions.put("__EXCEPTIONS", "");
		
		String [] includePaths = new String[] {
			"c:/mingw/include/c++/3.2.3",
			"c:/mingw/include/c++/3.2.3/mingw32",
			"c:/mingw/include/c++/3.2.3/backward",
			"c:/mingw/include",
			"c:/mingw/lib/gcc-lib/mingw32/3.2.3/include"
		};

		return new ScannerInfo( definitions, includePaths );
	}

	protected IScannerInfo ydlScannerInfo() {
		// TODO It would be easier and more flexible if we used discovery for this
		Map definitions = new Hashtable();
		definitions.put("__GNUC__", "3");
		definitions.put("__GNUC_MINOR__", "3");
		definitions.put("__GNUC_PATCHLEVEL__", "3");
		definitions.put("_GNU_SOURCE", "");
		definitions.put("__unix__", "");
		definitions.put("__gnu_linux__", "");
		definitions.put("__linux__", "");
		definitions.put("unix", "");
		definitions.put("__unix", "");
		definitions.put("linux", "");
		definitions.put("__linux", "");
		definitions.put("__GNUG__", "3");
		
		String [] includePaths = new String[] {
			"/usr/include/g++",
			"/usr/include/g++/powerpc-yellowdog-linux",
			"/usr/include/g++/backward",
			"/usr/local/include",
			"/usr/lib/gcc-lib/powerpc-yellowdog-linux/3.3.3/include",
			"/usr/include"
		};

		return new ScannerInfo( definitions, includePaths );
	}

}
