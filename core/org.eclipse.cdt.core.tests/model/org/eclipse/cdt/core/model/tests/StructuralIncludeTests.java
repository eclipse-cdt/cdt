package org.eclipse.cdt.core.model.tests;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author hamer
 *
 */
public class StructuralIncludeTests extends IIncludeTests {
	/**
	 * @param string
	 */
	public StructuralIncludeTests(String string) {
		super( string );
	}

	
	/**
	 * @returns a test suite named after this class
	 *          containing all its public members named "test*"
	 */
	public static Test suite() {
		TestSuite suite= new TestSuite(StructuralIncludeTests.class);
		return suite;
	}		

	public void testGetIncludeName()
	{
		setStructuralParse(true);
//		super.testGetIncludeName();
	}

	public void testIsStandard()
	{
		setStructuralParse(true);
//		super.testIsStandard();
	}
}
