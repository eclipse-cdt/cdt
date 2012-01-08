/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
/*
 * Created on Jun 4, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import java.util.LinkedHashMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ITranslationUnit;

/**
 * @author bnicolle
 *
 */
public class IIncludeTests extends IntegratedCModelTest {

	/**
	 * @param string
	 */
	public IIncludeTests(String string) {
		super( string );
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	@Override
	public String getSourcefileSubdir() {
		return "resources/cmodel/";
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
	@Override
	public String getSourcefileResource() {
		return "IIncludeTest.h";
	}

	/**
	 * @returns a test suite named after this class
	 *          containing all its public members named "test*"
	 */
	public static Test suite() {
		TestSuite suite= new TestSuite(IIncludeTests.class);
		return suite;
	}		

	public void testGetIncludeName() throws CModelException
	{
		ITranslationUnit tu = getTU();
		IInclude[] theIncludes = null;
		try {
			theIncludes = tu.getIncludes();
		}
		catch( CModelException c )
		{
			assertNotNull("CModelException thrown",c);
		}

		LinkedHashMap expectIncludes= new LinkedHashMap();
		expectIncludes.put("stdio.h", Boolean.TRUE);
		expectIncludes.put("whatever.h", Boolean.FALSE);
		expectIncludes.put("src/slash.h", Boolean.TRUE);
		expectIncludes.put("src\\backslash.h", Boolean.TRUE); // that's a single backslash, escaped
		expectIncludes.put("Program Files/space.h", Boolean.FALSE);
		expectIncludes.put("../up1dir.h", Boolean.FALSE);
		expectIncludes.put("./samedir.h", Boolean.FALSE);
		expectIncludes.put("different_extension1.hpp", Boolean.FALSE);
		expectIncludes.put("different_extension2.hh", Boolean.FALSE);
		expectIncludes.put("different_extension3.x", Boolean.FALSE);
		expectIncludes.put("no_extension", Boolean.TRUE);
		expectIncludes.put("whitespace_after_hash", Boolean.FALSE);
		expectIncludes.put("whitespace_before_hash", Boolean.FALSE);
		expectIncludes.put("resync_after_bad_parse_1", Boolean.FALSE);			
		expectIncludes.put("resync_after_bad_parse_2", Boolean.FALSE);
		expectIncludes.put("one", Boolean.FALSE);  // C-spec does not allow this, gcc warns and includes, so we should include it, also.
		expectIncludes.put("resync_after_bad_parse_3", Boolean.FALSE);
		expectIncludes.put("myInclude1.h", Boolean.FALSE);
		expectIncludes.put("vers2.h", Boolean.FALSE);					

		String[] getIncludeNameList= (String[]) expectIncludes.keySet().toArray(new String[expectIncludes.size()]);
		assertEquals( getIncludeNameList.length, theIncludes.length );
		for( int i=0; i<getIncludeNameList.length; i++ )
		{
			IInclude inc1 = theIncludes[i];
			String expectName= getIncludeNameList[i];
			assertEquals( expectName, inc1.getIncludeName() );
			assertEquals( ((Boolean) expectIncludes.get(expectName)).booleanValue(), inc1.isStandard());
		}
	}
}

