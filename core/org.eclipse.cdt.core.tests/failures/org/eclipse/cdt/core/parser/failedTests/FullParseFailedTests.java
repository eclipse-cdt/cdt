/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jul 28, 2003
 */
package org.eclipse.cdt.core.parser.failedTests;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.tests.CompleteParseASTTest;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FullParseFailedTests extends CompleteParseASTTest {

	/**
	 * @param a
	 */
	public FullParseFailedTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(FullParseFailedTests.class.getName());
		return suite;
	}
	
	public void testBug41520() throws Exception 
	{
		Iterator i = parse( "int x = 666; int y ( x );").getDeclarations();
		IASTVariable variableX = (IASTVariable)i.next();
		try
		{ 
			IASTVariable variableY = (IASTVariable)i.next();
			failedAsExpected();
		}catch( ClassCastException cce )
		{
			//this is bad
		}
	}

    /**
     * 
     */
    private void failedAsExpected()
    {   
    }


}