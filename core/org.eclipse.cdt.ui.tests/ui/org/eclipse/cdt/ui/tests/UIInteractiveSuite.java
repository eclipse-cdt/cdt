/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.cdt.ui.tests;

import org.eclipse.cdt.ui.tests.dialogs.PreferencesTest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test all areas of the UI.
 */
public class UIInteractiveSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new UIInteractiveSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public UIInteractiveSuite() {
		addTest(PreferencesTest.suite());
		//addTest(WizardsTest.suite());
		//addTest(DialogsTest.suite());
	}
	
}