/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core.parser.failedTests;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.parser.tests.BaseDOMTest;


/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ACEFailedTest extends BaseDOMTest {

	/**
	 * @param arg
	 */
	public ACEFailedTest(String arg) {
		super(arg);
	}
	
	public void testBug36771() throws Exception {
		Writer code = new StringWriter();
		code.write("#include /**/ \"foo.h\"\n");
		failTest( code.toString());
	}
	
	public void testBug36769() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class A, B> cls<A, C>::operator op &() const {}\n");
		code.write("template <class A, B> cls<A, C>::cls() {}\n");
		code.write("template <class A, B> cls<A, C>::~cls() {}\n");
			
		failTest( code.toString());
	}
}
