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
		suite.addTest(StructuralIncludeTests.suite());
		suite.addTest(IMacroTests.suite());
		suite.addTest(StructuralMacroTests.suite());
		suite.addTest(IStructureTests.suite());
		suite.addTest(StructuralStructureTests.suite());		
		suite.addTest(ITemplateTests.suite());
		suite.addTest(StructuralTemplateTests.suite());		
		return suite;
        
	}

}
