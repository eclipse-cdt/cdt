/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.parser.tests.BaseASTTest;

/**
 * @author vmozgin
 *
 */
public class PerformanceTests extends BaseASTTest
{
	public PerformanceTests(String name)
	{
		super(name);
	}

	public void testBug39523() throws Exception
	{
		Writer code = new StringWriter();
		try	{ 
			code.write("#define e0	\"a\"\n"); //$NON-NLS-1$
			code.write("#define e1	e0 e0 e0 e0 e0 e0 e0 e0 e0 e0\n"); //$NON-NLS-1$
			code.write("#define e2	e1 e1 e1 e1 e1 e1 e1 e1 e1 e1\n"); //$NON-NLS-1$
			code.write("#define e3	e2 e2 e2 e2 e2 e2 e2 e2 e2 e2\n"); //$NON-NLS-1$
			code.write("#define e4	e3 e3 e3 e3 e3 e3 e3 e3 e3 e3\n"); //$NON-NLS-1$
			code.write("#define e5	e4 e4 e4 e4 e4 e4 e4 e4 e4 e4\n"); //$NON-NLS-1$
			code.write("void foo() { (void)(e5); }\n"); //$NON-NLS-1$
		} catch( IOException ioe ){}
			
		parse(code.toString());
	}    
}
