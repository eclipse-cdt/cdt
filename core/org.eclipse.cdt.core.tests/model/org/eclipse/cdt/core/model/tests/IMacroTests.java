/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jun 6, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IMacro;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.tests.IntegratedCModelTest;

/**
 * IMacroTest - Class for testing IMacro 
 * 
 * @author bnicolle
 *
 */
public class IMacroTests extends IntegratedCModelTest {
	/**
	 * @returns a test suite named after this class
	 *          containing all its public members named "test*"
	 */
	public static Test suite() {
		TestSuite suite= new TestSuite( IMacroTests.class.getName() );
		suite.addTest( new IMacroTests("testGetElementName"));
		// TODO Bug# 38740: suite.addTest( new IMacroTest("testGetIdentifierList"));
		// TODO Bug# 38740: suite.addTest( new IMacroTest("testGetTokenSequence"));
		return suite;
	}		

	/**
	 * @param name
	 */
	public IMacroTests(String name) {
		super(name);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	public String getSourcefileSubdir() {
		return "resources/cmodel/";
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	public String getSourcefileResource() {
		return "IMacroTest.h";
	}

	public void testGetElementName() throws CModelException {
		ITranslationUnit tu = getTU();
		List arrayElements = tu.getChildrenOfType( ITranslationUnit.C_MACRO );

		String expectedList[] =  new String[] {
			"SINGLETON",
			"NUMBER",
			"PRINT"
		};
		assertEquals( expectedList.length, arrayElements.size() );
		for( int i=0; i<expectedList.length; i++ )
		{
			IMacro iMacro = (IMacro) arrayElements.get(i);
			assertEquals( expectedList[i], iMacro.getElementName() );
		}
	}
		
	public void testGetIdentifierList() throws CModelException {
		ITranslationUnit tu = getTU();
		List arrayElements = tu.getChildrenOfType( ITranslationUnit.C_MACRO );

		String expectedList[] =  new String[] {
			"",
			"",
			"string,msg"
		};
		assertEquals( expectedList.length, arrayElements.size() );
		for( int i=0; i<expectedList.length; i++ )
		{
			IMacro iMacro = (IMacro) arrayElements.get(i);
			assertEquals( expectedList[i], iMacro.getIdentifierList() );
		}
	}

	public void testGetTokenSequence() throws CModelException {
		ITranslationUnit tu = getTU();
		List arrayElements = tu.getChildrenOfType( ITranslationUnit.C_MACRO );

		String expectedList[] =  new String[] {
			"",
			"1",
			"printf(string, msg)"
		};
		assertEquals( expectedList.length, arrayElements.size() );
		for( int i=0; i<expectedList.length; i++ )
		{
			IMacro iMacro = (IMacro) arrayElements.get(i);
			assertEquals( expectedList[i], iMacro.getTokenSequence() );
		}
	}		
}
