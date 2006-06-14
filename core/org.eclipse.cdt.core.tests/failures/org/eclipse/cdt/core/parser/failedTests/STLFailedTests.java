/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.failedTests;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
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

	public void testBug40714() throws Exception{
		// templates of variables
		Writer code = new StringWriter();
		code.write("template <bool __threads, int __inst>\n");
		code.write("char* default_alloc_template<__threads, __inst>::_S_start_free = 0;\n");
		IASTCompilationUnit cu = parse(code.toString());
		IASTTemplateDeclaration templateDecl = (IASTTemplateDeclaration) cu.getDeclarations().next(); 
		// should not get this exception 
		IASTVariable v = (IASTVariable) templateDecl.getOwnedDeclaration();
		assertEquals( v, null );
	}
	
}
