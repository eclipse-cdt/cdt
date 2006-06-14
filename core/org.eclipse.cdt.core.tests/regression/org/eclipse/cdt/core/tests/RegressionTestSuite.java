/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Oct 4, 2004
 */
package org.eclipse.cdt.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author aniefer
 */
public class RegressionTestSuite extends TestSuite {
    public RegressionTestSuite() {
        super();
    }
	
	public RegressionTestSuite(Class theClass, String name) {
		super(theClass, name);
	}
	
	public RegressionTestSuite(Class theClass) {
		super(theClass);
	}
	
	public RegressionTestSuite(String name) {
		super(name);
	}
	
	public static Test suite() {
		final RegressionTestSuite suite = new RegressionTestSuite();

		suite.addTest( SelectionRegressionTest.suite( false ) );
		
		return suite;
	}
}
