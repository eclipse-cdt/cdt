/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;



/**
 * @author dsteffle
 * mstodo- the class should be removed
 */
public class AST2CSpecFailingTest extends AST2SpecBaseTest {

	public AST2CSpecFailingTest() {
	}

	public AST2CSpecFailingTest(String name) {
		super(name);
	}

	/**
	 [--Start Example(C 6.7.7-6):
	typedef signed int t;
	typedef int plain;
	struct tag {
	unsigned t:4;
	const t:5;
	plain r:5;
	};
	t f(t (t));
	long t;
	 --End Example]
	 */
	public void test6_7_7s6() throws Exception {
		// test is no longer failing, was moved to AST2CSpecTest
//		StringBuffer buffer = new StringBuffer();
//		buffer.append("typedef signed int t;\n"); //$NON-NLS-1$
//		buffer.append("typedef int plain;\n"); //$NON-NLS-1$
//		buffer.append("struct tag {\n"); //$NON-NLS-1$
//		buffer.append("unsigned t:4;\n"); //$NON-NLS-1$
//		buffer.append("const t:5;\n"); //$NON-NLS-1$
//		buffer.append("plain r:5;\n"); //$NON-NLS-1$
//		buffer.append("};\n"); //$NON-NLS-1$
//		buffer.append("t f(t (t));\n"); //$NON-NLS-1$
//		buffer.append("long t;\n"); //$NON-NLS-1$
//		try {
//			parse(buffer.toString(), ParserLanguage.C, true, 0);
//			assertTrue(false);
//		} catch (Exception e) {}
	}
}
