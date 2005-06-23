/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Nov 16, 2004
 */
package org.eclipse.cdt.ui.tests.regression;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author aniefer
 */
public class RegressionTestsUISuite extends TestSuite {
    public RegressionTestsUISuite() {
        super();
    }
	
	public RegressionTestsUISuite(Class theClass, String name) {
		super(theClass, name);
	}
	
	public RegressionTestsUISuite(Class theClass) {
		super(theClass);
	}
	
	public RegressionTestsUISuite(String name) {
		super(name);
	}
	
	public static Test suite() {
		final RegressionTestsUISuite suite = new RegressionTestsUISuite();

		suite.addTest( ContentAssistRegressionTests.suite( false ) );
		//suite.addTest( RefactoringRegressionTests.suite( false ) );
		
		//suite.addTest( new RefactoringRegressionTests("cleanupProject") ); //$NON-NLS-1$
		return suite;
	}

}
