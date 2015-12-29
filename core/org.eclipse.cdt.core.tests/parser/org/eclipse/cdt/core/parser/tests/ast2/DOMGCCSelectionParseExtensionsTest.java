/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * @author jcamelon
 *
 */
public class DOMGCCSelectionParseExtensionsTest extends DOMSelectionParseTestBase {

	public DOMGCCSelectionParseExtensionsTest() {
	}

	public DOMGCCSelectionParseExtensionsTest(String name, Class className) {
		super(name, className);
	}

	public DOMGCCSelectionParseExtensionsTest(String name) {
		super(name, DOMGCCSelectionParseExtensionsTest.class);
	}

	public void testBug43021() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "extern int johnc(__const char *__restrict __format, ...);\n" ); //$NON-NLS-1$
		writer.write( "void m() {johnc(\"HI\");}" ); //$NON-NLS-1$
		String code = writer.toString();
		int startIndex = code.indexOf( "{johnc") + 1; //$NON-NLS-1$
		IASTNode node = parse( code, startIndex, startIndex + 5 );
		assertNotNull( node );
	}
}
