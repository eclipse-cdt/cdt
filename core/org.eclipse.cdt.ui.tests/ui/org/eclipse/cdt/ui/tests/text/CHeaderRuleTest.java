/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
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
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text;

import org.eclipse.cdt.internal.ui.text.CHeaderRule;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests to verify recognition of #include headers.
 */
public class CHeaderRuleTest extends TestCase {
	private static final String HEADER = "header";
	private IToken fToken;
	private RuleBasedScanner fScanner;
	private Document fDocument;

	public CHeaderRuleTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		fToken = new Token(HEADER);
		fScanner = new RuleBasedScanner();
		fScanner.setRules(new IRule[] { new CHeaderRule(fToken) });
		fDocument = new Document();
	}

	public static Test suite() {
		return new TestSuite(CHeaderRuleTest.class);
	}

	public void testHeader() {
		assertHeader("#include <foo.h>", "<foo.h>", 9);
	}

	public void testHeader2() {
		assertHeader("#include <vector>", "<vector>", 9);
	}

	public void testHeaderNoSpaceBetween() {
		assertHeader("#include<vector>", "<vector>", 8);
	}

	public void testHeaderExtraSpacesBetween() {
		assertHeader("#include    <foo.h>", "<foo.h>", 12);
	}

	public void testHeaderExtraSpacesBefore() {
		assertHeader("  #include <foo.h>", "<foo.h>", 11);
	}

	public void testHeaderIncludeNext() {
		assertHeader("#include_next<vector>", "<vector>", 13);
	}

	public void testBooleanLogic() {
		assertNotHeader("if (x < 10 && x > 20) return false;", 6);
	}

	public void testVariableDeclaration() {
		assertNotHeader("vector<int> foo;", 6);
	}

	/**
	 * Verifies that there is a header at the given character position.
	 * @param string String to check.
	 * @param header Expected header.
	 * @param position The location of the token which is expected to be a header.
	 */
	private void assertHeader(String string, String header, int position) {
		fDocument.set(string);
		fScanner.setRange(fDocument, 0, fDocument.getLength());
		while (position > 0) {
			fScanner.read();
			position--;
		}
		IToken token = fScanner.nextToken();
		assertSame(HEADER, token.getData());
		assertEquals(header.length(), fScanner.getTokenLength());
		assertEquals(string.indexOf(header), fScanner.getTokenOffset());
	}

	/**
	 * Verifies that string does not contain a header at the given position.
	 * @param string The String to check.
	 * @param position Offset where scanning should begin.
	 */
	private void assertNotHeader(String string, int position) {
		fDocument.set(string);
		fScanner.setRange(fDocument, 0, fDocument.getLength());
		while (position > 0) {
			fScanner.read();
			position--;
		}
		IToken token = fScanner.nextToken();
		assertNotSame(HEADER, token.getData());
	}
}
