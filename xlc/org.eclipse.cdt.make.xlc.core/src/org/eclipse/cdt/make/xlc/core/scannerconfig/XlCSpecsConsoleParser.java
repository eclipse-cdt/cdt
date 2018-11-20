/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.xlc.core.scannerconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.cdt.make.xlc.core.activator.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Parses output of ppuxlc -E -v specs.c or ppuxlc -E -v specs.cpp command
 *
 * @author laggarcia
 * @since 1.0.0
 */
public class XlCSpecsConsoleParser implements IScannerInfoConsoleParser {

	// pattern for the output line of interest
	final Pattern linePattern = Pattern.compile("exec:\\s(?!export)(?:.*)\\((.*)\\)"); //$NON-NLS-1$

	// pattern for the symbols arguments
	final Pattern symbolPattern = Pattern.compile("-D(.*)"); //$NON-NLS-1$

	// pattern for the includes arguments
	final Pattern includePattern = Pattern.compile("-(?:qgcc_c_stdinc|qc_stdinc|qgcc_cpp_stdinc|qcpp_stdinc)=(.*)"); //$NON-NLS-1$

	final Pattern C_includePattern = Pattern.compile("-(?:qgcc_c_stdinc|qc_stdinc)=(.*)"); //$NON-NLS-1$
	final Pattern CXX_includePattern = Pattern.compile("-(?:qgcc_cpp_stdinc|qcpp_stdinc)=(.*)"); //$NON-NLS-1$

	// xlC compiler constants
	protected final static String[] compilerConstants = { "_Builtin", //$NON-NLS-1$
			"__IBMCPP__", //$NON-NLS-1$
			"__xlC__", //$NON-NLS-1$
			"__IBMC__", //$NON-NLS-1$
			"__xlc__" //$NON-NLS-1$
	};

	private IProject fProject = null;

	protected IScannerInfoCollector fCollector = null;

	protected List<String> symbols = new ArrayList<>();

	protected List<String> includes = new ArrayList<>();
	protected List<String> c_includes = new ArrayList<>();
	protected List<String> cpp_includes = new ArrayList<>();

	boolean c_lang; // if language is C only search for the C include paths from the XL Compiler, otherwise get the C++ ones.

	public boolean isC_lang() {
		return c_lang;
	}

	public void setC_lang(boolean c_lang) {
		this.c_lang = c_lang;
	}

	@Override
	public void startup(IProject project, IPath workingDirectory, IScannerInfoCollector collector,
			IMarkerGenerator markerGenerator) {
		this.fProject = project;
		this.fCollector = collector;

		try {
			if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
				// use C++ pattern
				c_lang = false;
			} else {
				// use C pattern
				c_lang = true;
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Process an output line from the compiler command line used to retrieve
	 * standard information about the compiler being used. <p> During the
	 * processing, builds two List objects, one with the standard symbols
	 * defined in the compiler and other with the standard include directories.
	 *
	 * @param line the output line from the compiler command line used @return
	 * boolean
	 *
	 * @see org.eclipse.cdt.make.intrenal.core.scannerconfig.gnu.GCCSpecsConsoleParser#processLine(java.lang.String)
	 * @since 1.0
	 */
	@Override
	public boolean processLine(String line) {
		boolean rc = false;
		TraceUtil.outputTrace("XLCSpecsConsoleParser parsing line: [", line, "]"); //$NON-NLS-1$ //$NON-NLS-2$

		line = line.trim();
		if (line.length() == 0) {
			return false;
		}

		// testing the output line against the pattern of interest
		Matcher lineMatcher = linePattern.matcher(line);
		if (lineMatcher.matches()) {
			// getting the arguments from the line of interest from the
			// output
			// generated in command line
			String[] args = lineMatcher.group(1).split(","); //$NON-NLS-1$
			for (int i = 0; i < args.length; i++) {
				// getting the arguments of interest
				Matcher symbolMatcher = symbolPattern.matcher(args[i]);
				if (symbolMatcher.matches() && !symbols.contains(symbolMatcher.group(1))) {
					// if it is a symbol and it was not yet added
					symbols.add(symbolMatcher.group(1));
				} else {
					// if it is not a symbol, check to see if it is an
					// include
					Matcher includeMatcher = c_lang ? C_includePattern.matcher(args[i])
							: CXX_includePattern.matcher(args[i]);
					if (includeMatcher.matches()) {
						// if it is a set of include paths, split it
						String[] includePaths = includeMatcher.group(1).split(":"); //$NON-NLS-1$
						for (int j = 0; j < includePaths.length; j++) {
							if (!includes.contains(includePaths[j])) {
								// if the include path was not yet added
								includes.add(includePaths[j]);
							}
						}
					}
				}
			}
		}
		return rc;
	}

	@Override
	public void shutdown() {
		Map<ScannerInfoTypes, List<String>> scannerInfo = new HashMap<>();

		// insert compiler constants, work around buggy xlC option for dumping symbols (it misses a few)
		for (String constant : compilerConstants) {
			if (!symbols.contains(constant))
				symbols.add(constant);
		}

		// add the scanner info
		scannerInfo.put(ScannerInfoTypes.INCLUDE_PATHS, includes);
		scannerInfo.put(ScannerInfoTypes.SYMBOL_DEFINITIONS, symbols);

		fCollector.contributeToScannerConfig(fProject, scannerInfo);
		if (fCollector != null && fCollector instanceof IScannerInfoCollector2) {
			IScannerInfoCollector2 collector = (IScannerInfoCollector2) fCollector;
			try {
				collector.updateScannerConfiguration(null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				Activator.log(e);
			}
		}
		TraceUtil.outputTrace("Scanner info from \'specs\' file", //$NON-NLS-1$
				"Include paths", includes, new ArrayList<String>(), "Defined symbols", symbols); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
