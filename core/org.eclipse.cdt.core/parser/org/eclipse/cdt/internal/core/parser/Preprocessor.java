/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.io.Reader;

import org.eclipse.cdt.core.parser.EndOfFile;
import org.eclipse.cdt.core.parser.IPreprocessor;
import org.eclipse.cdt.core.parser.IProblemReporter;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ITranslationResult;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerException;


/**
 * @author jcamelon
 *
 */
public class Preprocessor extends Scanner implements IPreprocessor {

	/**
	 * @param reader
	 * @param filename
	 * @param defns
	 */
	public Preprocessor(Reader reader, String filename, IScannerInfo info, ISourceElementRequestor requestor, IProblemReporter problemReporter, ITranslationResult unitResult, ParserMode mode, ParserLanguage language ) {
        super(reader, filename, info, problemReporter, unitResult, requestor, mode, language );
    }

	public void process()
	{
		try
		{
			while( true )
				nextToken();
		}
		catch( ScannerException se )
		{
			// callback IProblem here
			org.eclipse.cdt.internal.core.model.Util.log(se, "Preprocessor Exception"); //$NON-NLS-1$h
			
		}
		catch( EndOfFile eof )
		{
			// expected 
		}
	}
}
