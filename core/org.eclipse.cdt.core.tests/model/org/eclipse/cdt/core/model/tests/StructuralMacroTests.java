package org.eclipse.cdt.core.model.tests;

/**
 * @author hamer
 *
 */

import org.eclipse.cdt.core.model.CModelException;
import junit.framework.Test;
import junit.framework.TestSuite;

public class StructuralMacroTests extends IMacroTests {

	/**
	 * @returns a test suite named after this class
	 *          containing all its public members named "test*"
	 */
	public static Test suite() {
		TestSuite suite= new TestSuite( StructuralMacroTests.class.getName() );
		suite.addTest( new StructuralMacroTests("testGetElementName"));
		return suite;
	}		

	/**
	 * @param name
	 */
	public StructuralMacroTests(String name) {
		super(name);
	}
	
	
	public void testGetElementName() throws CModelException {
		setStructuralParse(true);
		super.testGetElementName();
	}	
}
