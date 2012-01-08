/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.tests.CModelElementsTests;
import org.eclipse.cdt.core.model.tests.StructuralCModelElementsTests;
import org.eclipse.cdt.core.parser.tests.ast2.DOMGCCParserExtensionTestSuite;
import org.eclipse.cdt.core.parser.tests.ast2.DOMParserTestSuite;
import org.eclipse.cdt.core.parser.tests.ast2.SemanticsTests;
import org.eclipse.cdt.core.parser.tests.scanner.ScannerTestSuite;

/**
 * Combines all tests for the parsers.
 */
public class ParserTestSuite extends TestCase {
	public static Test suite() {
		TestSuite suite= new TestSuite(ParserTestSuite.class.getName());
		suite.addTestSuite(ArrayUtilTest.class);
		suite.addTestSuite(CharArrayUtilsTest.class);
		suite.addTestSuite(SegmentMatcherTest.class);
		suite.addTestSuite(ContentAssistMatcherFactoryTest.class);
		suite.addTestSuite(CModelElementsTests.class);
		suite.addTestSuite(StructuralCModelElementsTests.class);
		suite.addTestSuite(ObjectMapTest.class);
		suite.addTestSuite(SemanticsTests.class);
		suite.addTest(ScannerTestSuite.suite());
		suite.addTest(DOMParserTestSuite.suite());
		suite.addTest(DOMGCCParserExtensionTestSuite.suite());
		return suite;
	}
}
