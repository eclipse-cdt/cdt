/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
package org.eclipse.cdt.core.parser.xlc.tests.base;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.lrparser.tests.LRCPPTests;
import org.eclipse.cdt.core.lrparser.xlc.XlcCLanguage;
import org.eclipse.cdt.core.lrparser.xlc.XlcCPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.ParserLanguage;

public class XlcLRCPPTests extends LRCPPTests {
	public static TestSuite suite() {
		return suite(XlcLRCPPTests.class);
	}

	//CDT_70_FIX_FROM_50-#9
	public void testStaticAssertions_294730() throws Exception {
		String code = getAboveComment();
		code = code.replaceAll("static_assert", "__static_assert");
		parseAndCheckBindings(code, ParserLanguage.CPP);
	}

	protected ILanguage getCLanguage() {
		return XlcCLanguage.getDefault();
	}

	protected ILanguage getCPPLanguage() {
		return XlcCPPLanguage.getDefault();
	}
}
