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
package org.eclipse.cdt.internal.core.parser.scanner;

import java.io.Reader;

import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IPreprocessor;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
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
	public Preprocessor(Reader reader, String filename, IScannerInfo info, ISourceElementRequestor requestor, ParserMode mode, ParserLanguage language, IParserLogService logService ) {
        super(reader, filename, info, requestor, mode, language, logService );
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
			log.errorLog("Preprocessor Exception "+ se.getProblem().getMessage()); //$NON-NLS-1$h
		}
		catch( EndOfFileException eof )
		{
			// expected 
		}
	}
}
