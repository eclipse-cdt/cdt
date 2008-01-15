/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.model.failedTests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.tests.IntegratedCModelTest;

/**
 * @author jcamelon
 *
 */
public class FailedMacroTests extends IntegratedCModelTest
{
	public static Test suite() {
		TestSuite suite= new TestSuite("FailedMacroTests");
		return suite;
	}

    /**
     * 
     */
    public FailedMacroTests()
    {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * @param name
     */
    public FailedMacroTests(String name)
    {
        super(name);
        // TODO Auto-generated constructor stub
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
		return "MacroTests.cpp";
	}
	
}
