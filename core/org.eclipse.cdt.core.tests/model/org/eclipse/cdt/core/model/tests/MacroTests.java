/*
 * Created on Jun 9, 2003
 * by bnicolle
 */
package org.eclipse.cdt.core.model.tests;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author bnicolle
 *
 */
public class MacroTests extends IntegratedCModelTest {
	/**
	 * @param name
	 */
	public MacroTests(String name) {
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
		return "MacroTests.c";
	}
	
	/**
	 * @returns a test suite named after this class
	 *          containing all its public members named "test*"
	 */
	public static Test suite() {
		TestSuite suite= new TestSuite(MacroTests.class);
		return suite;
	}


 
}
