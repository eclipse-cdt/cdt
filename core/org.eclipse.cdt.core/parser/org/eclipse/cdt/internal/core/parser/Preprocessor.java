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
import java.util.Map;

import org.eclipse.cdt.core.parser.EndOfFile;
import org.eclipse.cdt.core.parser.IPreprocessor;
import org.eclipse.cdt.core.parser.IProblemReporter;
import org.eclipse.cdt.core.parser.ITranslationResult;
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
	public Preprocessor(Reader reader, String filename, Map defns, IProblemReporter problemReporter, ITranslationResult unitResult) {
        super(reader, filename, defns, problemReporter, unitResult);
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
		}
		catch( EndOfFile eof )
		{
			// expected 
		}
	}
}
