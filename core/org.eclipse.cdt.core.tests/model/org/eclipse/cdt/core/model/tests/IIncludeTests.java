/*
 * Created on Jun 4, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CElement;

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
	public String getSourcefileSubdir() {
		return "resources/cmodel/";
	}

	/**
	 * @see org.eclipse.cdt.internal.core.model.IntegratedCModelTest
	 */
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

	public void testGetIncludeName()
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

		String getIncludeNameList[] =  new String[] {
			new String("stdio.h"),
			new String("whatever.h"),
			new String("src/slash.h"),
			new String("src\\backslash.h"), // that's a single backslash, escaped
			new String("Program Files/space.h"),
			new String("../up1dir.h"),
			new String("./samedir.h"),
			new String("different_extension1.hpp"),
			new String("different_extension2.hh"),
			new String("different_extension3.x"),
			new String("no_extension"),
			new String("whitespace_after_hash"),
			new String("whitespace_before_hash"),
			new String("resync_after_bad_parse_1"),			
			new String("resync_after_bad_parse_2"),
			new String("one"),  // C-spec does not allow this, but that's OK for our present purposes
			new String("resync_after_bad_parse_3"),
			new String("invalid.h"),  // C-spec does not allow this, but that's OK for our present purposes
			new String("myInclude1.h"),
			new String("vers2.h")						
		};
		assertEquals( getIncludeNameList.length, theIncludes.length );
		for( int i=0; i<getIncludeNameList.length; i++ )
		{
			IInclude inc1 = theIncludes[i];
			assertEquals( getIncludeNameList[i], inc1.getIncludeName() );
		}
		
	}
	
	public void testIsStandard()
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
		boolean isStandardList[] =  new boolean[] {
			true, false
		};
		for( int i=0; i<isStandardList.length; i++ )
		{
			IInclude inc1 = theIncludes[i];
			assertEquals( isStandardList[i], inc1.isStandard() );
		}
	}
	
}

