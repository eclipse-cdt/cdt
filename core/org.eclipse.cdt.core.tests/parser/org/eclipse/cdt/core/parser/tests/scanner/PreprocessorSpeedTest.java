/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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

import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.FileContent;
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

			new PreprocessorSpeedTest().runTest(stream, 200);
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
		
		FileContent reader = FileContent.create("<test-code>", code.toCharArray());
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

	protected long testScan(FileContent reader, boolean quick, IScannerInfo info, ParserLanguage lang) throws Exception {
		FileCodeReaderFactory readerFactory= FileCodeReaderFactory.getInstance();
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
		else if (config.equals("cygwin"))
			return cygwinScannerInfo();
		else
			return mingwScannerInfo();
	}

	private IScannerInfo cygwinScannerInfo() {
		// TODO It would be easier and more flexible if we used discovery for this
		Map definitions = new Hashtable();
		definitions.put("i386", "1");
		definitions.put("unix", "1");
		definitions.put("_cdecl", "__attribute__((__cdecl__))");
		definitions.put("_fastcall", "__attribute__((__fastcall__))");
		definitions.put("_stdcall", "__attribute__((__stdcall__))");
		definitions.put("_X86_", "1");
		definitions.put("__CHAR_BIT__", "8");
		definitions.put("__cplusplus", "1");
		definitions.put("__CYGWIN32__", "1");
		definitions.put("__CYGWIN__", "1");
		definitions.put("__DBL_DENORM_MIN__", "4.9406564584124654e-324");
		definitions.put("__DBL_DIG__", "15");
		definitions.put("__DBL_EPSILON__", "2.2204460492503131e-16");
		definitions.put("__DBL_HAS_INFINITY__", "1");
		definitions.put("__DBL_HAS_QUIET_NAN__", "1");
		definitions.put("__DBL_MANT_DIG__", "53");
		definitions.put("__DBL_MAX_10_EXP__", "308");
		definitions.put("__DBL_MAX_EXP__", "1024");
		definitions.put("__DBL_MAX__", "1.7976931348623157e+308");
		definitions.put("__DBL_MIN_10_EXP__", "(-307)");
		definitions.put("__DBL_MIN_EXP__", "(-1021)");
		definitions.put("__DBL_MIN__", "2.2250738585072014e-308");
		definitions.put("__DECIMAL_DIG__", "21");
		definitions.put("__declspec(x)", "__attribute__((x))");
		definitions.put("__DEPRECATED", "1");
		definitions.put("__EXCEPTIONS", "1");
		definitions.put("__fastcall", "__attribute__((__fastcall__))");
		definitions.put("__FINITE_MATH_ONLY__", "0");
		definitions.put("__FLT_DENORM_MIN__", "1.40129846e-45F");
		definitions.put("__FLT_DIG__", "6");
		definitions.put("__FLT_EPSILON__", "1.19209290e-7F");
		definitions.put("__FLT_EVAL_METHOD__", "2");
		definitions.put("__FLT_HAS_INFINITY__", "1");
		definitions.put("__FLT_HAS_QUIET_NAN__", "1");
		definitions.put("__FLT_MANT_DIG__", "24");
		definitions.put("__FLT_MAX_10_EXP__", "38");
		definitions.put("__FLT_MAX_EXP__", "128");
		definitions.put("__FLT_MAX__", "3.40282347e+38F");
		definitions.put("__FLT_MIN_10_EXP__", "(-37)");
		definitions.put("__FLT_MIN_EXP__", "(-125)");
		definitions.put("__FLT_MIN__", "1.17549435e-38F");
		definitions.put("__FLT_RADIX__", "2");
		definitions.put("__GNUC_MINOR__", "4");
		definitions.put("__GNUC_PATCHLEVEL__", "4");
		definitions.put("__GNUC__", "3");
		definitions.put("__GNUG__", "3");
		definitions.put("__GXX_ABI_VERSION", "1002");
		definitions.put("__GXX_WEAK__", "1");
		definitions.put("__i386", "1");
		definitions.put("__i386__", "1");
		definitions.put("__INT_MAX__", "2147483647");
		definitions.put("__LDBL_DENORM_MIN__", "3.64519953188247460253e-4951L");
		definitions.put("__LDBL_DIG__", "18");
		definitions.put("__LDBL_EPSILON__", "1.08420217248550443401e-19L");
		definitions.put("__LDBL_HAS_INFINITY__", "1");
		definitions.put("__LDBL_HAS_QUIET_NAN__", "1");
		definitions.put("__LDBL_MANT_DIG__", "64");
		definitions.put("__LDBL_MAX_10_EXP__", "4932");
		definitions.put("__LDBL_MAX_EXP__", "16384");
		definitions.put("__LDBL_MAX__", "1.18973149535723176502e+4932L");
		definitions.put("__LDBL_MIN_10_EXP__", "(-4931)");
		definitions.put("__LDBL_MIN_EXP__", "(-16381)");
		definitions.put("__LDBL_MIN__", "3.36210314311209350626e-4932L");
		definitions.put("__LONG_LONG_MAX__", "9223372036854775807LL");
		definitions.put("__LONG_MAX__", "2147483647L");
		definitions.put("__NO_INLINE__", "1");
		definitions.put("__PTRDIFF_TYPE__", "int");
		definitions.put("__REGISTER_PREFIX__", "");
		definitions.put("__SCHAR_MAX__", "127");
		definitions.put("__SHRT_MAX__", "32767");
		definitions.put("__SIZE_TYPE__", "unsigned int");
		definitions.put("__stdcall", "__attribute__((__stdcall__))");
		definitions.put("__STDC_HOSTED__", "1");
		definitions.put("__tune_i686__", "1");
		definitions.put("__tune_pentiumpro__", "1");
		definitions.put("__unix", "1");
		definitions.put("__unix__", "1");
		definitions.put("__USER_LABEL_PREFIX__", "_");
		definitions.put("__USING_SJLJ_EXCEPTIONS__", "1");
		definitions.put("__VERSION__", "\"3.4.4 (cygming special, gdc 0.12, using dmd 0.125)\"");
		definitions.put("__WCHAR_MAX__", "65535U");
		definitions.put("__WCHAR_TYPE__", "short unsigned int");
		definitions.put("__WCHAR_UNSIGNED__", "1");
		definitions.put("__WINT_TYPE__", "unsigned int");

		String[] includePaths = new String[] { "C:/programs/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include/c++",
				"C:/programs/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/i686-pc-cygwin",
				"C:/programs/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include/c++/backward",
				"C:/programs/cygwin/lib/gcc/i686-pc-cygwin/3.4.4/include", "C:/programs/cygwin/usr/include",
				"C:/programs/cygwin/usr/include/w32api" };

		return new ScannerInfo(definitions, includePaths);
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
