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
package org.eclipse.cdt.core.parser.tests;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.IPreprocessor;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.internal.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.internal.core.parser.ScannerInfo;

/**
 * @author jcamelon
 *
 */
public class PreprocessorTest extends TestCase {

	public static class Callback extends NullSourceElementRequestor implements ISourceElementRequestor 
	{
		private List enteredInc = new ArrayList(), exitedInc = new ArrayList(); 
		
		public boolean asExpected( int balance )
		{
			return( ( enteredInc.size() - exitedInc.size() ) == balance ); 
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
		 */
		public void enterInclusion(IASTInclusion inclusion) {
			enteredInc.add( inclusion );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.cdt.core.parser.ast.IASTInclusion)
		 */
		public void exitInclusion(IASTInclusion inclusion) {
			exitedInc.add( inclusion );
		}
	}

	public PreprocessorTest( String name )
	{
		super( name );
	}
	
	public void testSimpleExample()
	{
		Callback c = new Callback(); 
		IPreprocessor p = setupPreprocessor( "#include <stdio.h>", 
			null, 	// NOTE -- to demonstrate simple example, this should be set up with the info from the 
					// build properties
			null, c );
		p.process(); 
		c.asExpected(0);
	}
	
	public IPreprocessor setupPreprocessor( String text, List includePaths, Map defns, ISourceElementRequestor rq )
	{
		IPreprocessor p = ParserFactory.createPreprocessor( new StringReader( text ), "test", new ScannerInfo(), ParserMode.COMPLETE_PARSE );
		p.setRequestor( rq );
		return p; 
	}
}
