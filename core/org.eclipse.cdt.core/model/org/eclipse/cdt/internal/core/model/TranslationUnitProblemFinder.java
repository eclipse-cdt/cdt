/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.io.StringReader;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * TranslationUnitProblemFinder
 */
public class TranslationUnitProblemFinder extends NullSourceElementRequestor {

	IProblemRequestor requestor;
	/**
	 * 
	 */
	public TranslationUnitProblemFinder(IProblemRequestor requestor) {
		super();
		this.requestor = requestor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.cdt.core.parser.IProblem)
	 */
	public boolean acceptProblem(IProblem problem) {
		requestor.acceptProblem(problem);
		return true;
	}

	/**
	 * @param copy
	 * @param requestor
	 * @param monitor
	 */
	public static void process(WorkingCopy copy, IProblemRequestor requestor, IProgressMonitor monitor) {
		
		TranslationUnitProblemFinder problemFinder = new TranslationUnitProblemFinder(requestor);
		IProject project = copy.getCProject().getProject();
		String code = new String();
		try{
			code = copy.getBuffer().getContents();
		} catch (CModelException e) {
			//
		}

		// pick the language
		ParserLanguage language = copy.isCXXLanguage()? ParserLanguage.CPP : ParserLanguage.C;

		IParser parser = null;
		try {
			IScannerInfo scanInfo = new ScannerInfo();
			IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
			if (provider != null){
				IScannerInfo buildScanInfo = provider.getScannerInformation(project);
				if (buildScanInfo != null){
					scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
				}
			}
			
			boolean quickParseMode = ! (CCorePlugin.getDefault().useStructuralParseMode());
			ParserMode mode = quickParseMode ? ParserMode.QUICK_PARSE : ParserMode.STRUCTURAL_PARSE;
			IScanner scanner = ParserFactory.createScanner(new StringReader(code), copy.getPath().toOSString(),
					scanInfo, mode, language, problemFinder, null, null);
			parser = ParserFactory.createParser(scanner, problemFinder, mode, language, null);
			parser.parse();
		} catch(ParserFactoryError pfe) {
			//
		}

	}
}
