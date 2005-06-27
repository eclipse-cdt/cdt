/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.prefix;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CompletionTestSuite extends TestCase {

	public static Test suite() { 
		TestSuite suite= new TestSuite(CompletionTestSuite.class.getName());
		suite.addTestSuite(BasicCompletionTest.class);
		return suite;
	}
}
