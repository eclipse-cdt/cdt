/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.failedTests;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.parser.tests.BaseASTTest;

/**
 * @author jcamelon
 */
public class LokiFailures extends BaseASTTest  {

	public LokiFailures(String name) {
		super(name);
	}

    public void testBug40419()
    {
		Writer code = new StringWriter();
		try
		{ 
			code.write( "template <class T, class U>	struct SuperSubclass {\n"  );
			code.write( "enum { value = (::Loki::Conversion<const volatile U*, const volatile T*>::exists && \n" );
			code.write( "!::Loki::Conversion<const volatile T*, const volatile void*>::sameType) };	};" );
		} catch( IOException ioe ){}
		assertCodeFailsParse( code.toString() );
	
    }	
}
