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
package org.eclipse.cdt.core.model.failedTests;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.tests.TranslationUnitBaseTest;
import org.eclipse.cdt.testplugin.CProjectHelper;

/**
 * @author jcamelon
 *
 */
public class FailedTranslationUnitTests extends TranslationUnitBaseTest
{
    /**
     * 
     */
    public FailedTranslationUnitTests()
    {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * @param name
     */
    public FailedTranslationUnitTests(String name)
    {
        super(name);
        // TODO Auto-generated constructor stub
    }
    
	/***
		 * Simple sanity test for old K&R-style C function declaration
		 */
	public void testKRFunctionDeclarations() throws CModelException
	{
		ITranslationUnit myTranslationUnit = CProjectHelper.findTranslationUnit(testProject,"exetest.c");
        
		assertTrue(myTranslationUnit.getElement("KRFunction") instanceof IFunction);            
		IFunction myKRFunction = (IFunction)myTranslationUnit.getElement("KRFunction");
		// reverse both these assertions to pass the test
		assertNotSame(myKRFunction.getSignature(), "KRFunction(const char*, int(*)(float), parm3)");
		assertNotSame(myKRFunction.getReturnType(), "bool");
	}

    
}
