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

import org.eclipse.cdt.core.parser.tests.BaseDOMTest;

/**
 * @author hamer
 */
public class STLFailedTests extends BaseDOMTest  {

	public STLFailedTests(String name) {
		super(name);
	}

	public void testBug36766A() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class _CharT, class _Alloc>\n");
		code.write("rope<_CharT, _Alloc>::rope(size_t __n, _CharT __c,\n");
		code.write("const allocator_type& __a): _Base(__a)\n");
		code.write("{}\n");
		failTest(code.toString());
	}

	public void testBug36766B() throws Exception {
		Writer code = new StringWriter();
		code.write("template<class _CharT>\n");
		code.write("bool _Rope_insert_char_consumer<_CharT>::operator()\n");
		code.write("(const _CharT* __leaf, size_t __n)\n");
		code.write("{}\n");
		failTest(code.toString());
	}

	public void testBug36766C() throws Exception {
		Writer code = new StringWriter();
		code.write("template <class _CharT, class _Alloc>\n");
		code.write("_Rope_char_ref_proxy<_CharT, _Alloc>&\n");
		code.write("_Rope_char_ref_proxy<_CharT, _Alloc>::operator= (_CharT __c)\n");
		code.write("{}\n");
		failTest(code.toString());
	}
	
	public void testBug36805() throws Exception{
		Writer code = new StringWriter();
		code.write("__STL_BEGIN_NAMESPACE\n");
		code.write("template <class _CharT> class char_traits\n");
		code.write(": public __char_traits_base<_CharT, _CharT>\n");
		code.write("{};\n");
		failTest(code.toString());
	}
	
}
