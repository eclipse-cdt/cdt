/*
 * Created on Jun 9, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * LanguageInterfaceTests
 * lists all parts of the C/C++ language interface objects
 * to be tested.
 * @author bnicolle
 *
 */
public class AllLanguageInterfaceTests {

	/**
	 * 
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(AllLanguageInterfaceTests.class.getName());

		// Just add more test cases here as you create them for 
		// each class being tested
        
		suite.addTest(IIncludeTests.suite());
		suite.addTest(IMacroTests.suite());
		suite.addTest(IStructureTests.suite());
		return suite;
        
	}

}
