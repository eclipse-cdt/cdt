/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
		TestSuite suite = new TestSuite(StructuralMacroTests.class.getName());
		suite.addTest(new StructuralMacroTests("testGetElementName"));
		return suite;
	}

	/**
	 * @param name
	 */
	public StructuralMacroTests(String name) {
		super(name);
	}

	@Override
	public void testGetElementName() throws CModelException {
		setStructuralParse(true);
		super.testGetElementName();
	}
}
