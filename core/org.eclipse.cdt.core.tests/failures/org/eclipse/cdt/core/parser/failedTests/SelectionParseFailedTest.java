/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */
package org.eclipse.cdt.core.parser.failedTests;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.tests.SelectionParseBaseTest;

/**
 * @author johnc
 *
 */
public class SelectionParseFailedTest extends SelectionParseBaseTest {
	
	public void testBug61800() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class ABCDEF {\n"); //$NON-NLS-1$
		writer.write( " static int stInt; };\n"); //$NON-NLS-1$
		writer.write( "int ABCDEF::stInt = 5;\n"); //$NON-NLS-1$
		String code = writer.toString();
		int startIndex = code.indexOf( "::stInt") + 2; //$NON-NLS-1$
		IASTNode node = parse( code, startIndex, startIndex+ 5, false );
//		IASTNode node = parse( code, startIndex, startIndex+ 5 );
//		assertTrue( node instanceof IASTField );
//		assertEquals( ((IASTField)node).getName(), "stInt" ); //$NON-NLS-1$
	}
	
}
