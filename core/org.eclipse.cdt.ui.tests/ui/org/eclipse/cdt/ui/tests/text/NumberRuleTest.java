/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.internal.ui.text.NumberRule;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * Testing the NumberRule matching integers and floats.
 */
public class NumberRuleTest extends TestCase {

	private static final Object NUMBER = "number";
	private RuleBasedScanner fScanner;
	private Document fDocument;

	/**
	 * @param name
	 */
	public NumberRuleTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(NumberRuleTest.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		fScanner = new RuleBasedScanner() {};
		fScanner.setRules(new IRule[] {
				new NumberRule(new Token(NUMBER))
		});
		fDocument = new Document();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testIntegers() {
		// decimal numbers
		assertNumber("0");
		assertNumber("-1");
		assertNumber("+1");
		assertNumber("123456789");
		assertNumber("-123456789");
		assertNumber("+123456789");

		// hex numbers
		assertNumber("0xaffe");
		assertNumber("-0xaffe");
		assertNumber("+0xaffe");
		assertNumber("0Xaffe");
		assertNumber("+0XaFFe");
		assertNumber("0xabcdefABCDEF");
		assertNumber("0x0123456789");
	}
	
	public void testFloats() {
		assertNumber("0.");
		assertNumber(".0");
		assertNumber("-.0");
		assertNumber("+.0");
		assertNumber("-0.");
		assertNumber("+0.");
		assertNumber("0.123456789");
		assertNumber("-0.123456789");
		assertNumber("+12345.6789");
		assertNumber("1e5");
		assertNumber("1E5");
		assertNumber("1.e5");
		assertNumber("-1e5");
		assertNumber("-.1e5");
		assertNumber("1e-5");
		assertNumber("1e+55");
	}

	public void testNonNumbers() {
		// test pathological cases
		assertNoNumber("-");
		assertNoNumber("+");
		assertNoNumber(".");
		assertNoNumber("-.");
		assertNoNumber("+.");
		assertNoNumber("x");
		assertNoNumber(".x");
		assertNoNumber("-x");
		assertNoNumber("e");
		assertNoNumber(".e");
		assertNoNumber("-e");
		assertNoNumber("+e");
		
		// false positives:
//		assertNoNumber("0x");
//		assertNoNumber("1e");
//		assertNoNumber("1e+");
	}

	/**
	 * Validate that given string is recognized as a number.
	 * @param string
	 */
	private void assertNumber(String string) {
		fDocument.set(string);
		fScanner.setRange(fDocument, 0, fDocument.getLength());
		IToken token = fScanner.nextToken();
		assertSame(NUMBER, token.getData());
		assertEquals(string.length(), fScanner.getTokenLength());
	}
	
	/**
	 * Validate that given string is not recognized as a number.
	 * @param string
	 */
	private void assertNoNumber(String string) {
		fDocument.set(string);
		fScanner.setRange(fDocument, 0, fDocument.getLength());
		IToken token = fScanner.nextToken();
		assertNotSame(NUMBER, token.getData());
	}
	
}
