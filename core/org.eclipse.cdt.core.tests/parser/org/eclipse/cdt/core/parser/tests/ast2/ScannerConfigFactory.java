/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ScannerInfo;

/**
 * @author Doug Schaefer
 */
public class ScannerConfigFactory {
	
	public static IScannerInfo getScannerInfo() {
		String config = System.getProperty("speedTest.config"); 

		if (config == null)
			return mingwScannerInfo();

		if (config.equals("msvc"))
			return msvcScannerInfo();
		else if (config.equals("ydl"))
			return ydlScannerInfo();
		else
			return mingwScannerInfo();
	}
	
	private static IScannerInfo msvcScannerInfo() {
		Map definitions = new Hashtable();
		//definitions.put( "__GNUC__", "3" );  //$NON-NLS-1$ //$NON-NLS-2$

		String [] includePaths = new String[] {
			"C:\\Program Files\\Microsoft SDK\\Include",
			"C:\\Program Files\\Microsoft Visual C++ Toolkit 2003\\include"
		};
		return new ScannerInfo( definitions, includePaths );
	}

	private static IScannerInfo mingwScannerInfo() {
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

	private static IScannerInfo ydlScannerInfo() {
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
