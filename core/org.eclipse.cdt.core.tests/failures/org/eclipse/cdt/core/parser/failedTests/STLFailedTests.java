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
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.parser.tests.BaseASTTest;

/**
 * @author hamer
 */
public class STLFailedTests extends BaseASTTest  {

	public STLFailedTests(String name) {
		super(name);
	}
	
	public void testBug36805() throws Exception{
		Writer code = new StringWriter();
		code.write("__STL_BEGIN_NAMESPACE\n");
		code.write("template <class _CharT> class char_traits\n");
		code.write(": public __char_traits_base<_CharT, _CharT>\n");
		code.write("{};\n");
		assertCodeFailsParse(code.toString());
	}
	
}
