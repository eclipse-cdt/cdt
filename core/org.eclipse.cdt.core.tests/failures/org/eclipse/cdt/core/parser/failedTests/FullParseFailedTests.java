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
/*
 * Created on Jul 28, 2003
 */
package org.eclipse.cdt.core.parser.failedTests;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.parser.tests.BaseASTTest;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FullParseFailedTests extends BaseASTTest {

	/**
	 * @param a
	 */
	public FullParseFailedTests(String name) {
		super(name);
	}

	public void testBug40842() throws Exception{
		Writer code = new StringWriter();
		
		//note that if the parse fails at EOF, parse.failParse never sets
		//parsePassed = false because it will throw EOF on LA(1), so get 
		//around this by adding more code after the error.
		code.write("class A {} a;\n int x;");
		assertCodeFailsFullParse(code.toString());
	}
}