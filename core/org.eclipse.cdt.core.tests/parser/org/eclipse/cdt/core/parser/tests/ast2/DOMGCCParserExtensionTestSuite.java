/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author jcamelon
 *
 */
public class DOMGCCParserExtensionTestSuite extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(DOMGCCParserExtensionTestSuite.class.getName());
		//		suite.addTestSuite( GCCScannerExtensionsTest.class );
		//		suite.addTestSuite( GCCQuickParseExtensionsTest.class );
		//		suite.addTestSuite( GCCCompleteParseExtensionsTest.class );
		suite.addTestSuite(DOMGCCSelectionParseExtensionsTest.class);
		return suite;
	}

}
