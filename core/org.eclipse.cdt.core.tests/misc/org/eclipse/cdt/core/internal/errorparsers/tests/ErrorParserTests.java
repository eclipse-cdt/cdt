/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.internal.errorparsers.tests;

import junit.framework.Test;
import junit.framework.TestSuite;


public class ErrorParserTests {

	public static Test suite() {
        TestSuite suite = new TestSuite(ErrorParserTests.class.getName());

        // Just add more test cases here as you create them for
        // each class being tested
		suite.addTest(GCCErrorParserTests.suite());
        suite.addTest(FileBasedErrorParserTests.suite());
        return suite;
	}

}
