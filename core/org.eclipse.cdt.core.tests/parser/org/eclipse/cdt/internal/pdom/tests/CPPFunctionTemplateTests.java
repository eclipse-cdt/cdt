/*******************************************************************************
 * Copyright (c) 2007, 2013 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.index.IIndexFragment;

public class CPPFunctionTemplateTests extends PDOMInlineCodeTestBase {
	
	public static Test suite() {
		return suite(CPPFunctionTemplateTests.class);
	}
	
	/*************************************************************************/

	//	template<typename X>
	//	void foo(X x) {}
	//
	//	template<typename A, typename B>
	//	void foo(A a, B b) {}
	//
	//	class C1 {}; class C2 {}; class C3 {};
	//
	//	void bar() {
	//		foo<C1>(*new C1());
	//		foo<C2>(*new C2());
	//		foo<C3>(*new C3());
	//      foo<C1,C2>(*new C1(), *new C2());
	//      foo<C2,C3>(*new C2(), *new C3());
	//      foo<C3,C1>(*new C3(), *new C1());
	//      foo<C2,C1>(*new C2(), *new C1());
	//      foo<C3,C2>(*new C3(), *new C2());
	//      foo<C1,C3>(*new C1(), *new C3());
	//	}
	public void testSimpleInstantiation() throws Exception {
		setUpSections(1);
		IBinding[] bs= pdom.findBindings(new char[][]{"foo".toCharArray()}, IndexFilter.ALL_DECLARED, npm());
		assertEquals(2, bs.length);
		assertInstance(bs[0], ICPPFunctionTemplate.class);
		assertInstance(bs[1], ICPPFunctionTemplate.class);
		
		boolean b= ((ICPPFunctionTemplate)bs[0]).getTemplateParameters().length==1;
		ICPPFunctionTemplate fooX= (ICPPFunctionTemplate) bs[b ? 0 : 1];
		ICPPFunctionTemplate fooAB= (ICPPFunctionTemplate) bs[b ? 1 : 0];
		
		List<ICPPTemplateInstance> instances= Arrays.asList(((ICPPInstanceCache) fooX).getAllInstances());
		assertEquals(3, instances.size());
		for (ICPPTemplateInstance inst : instances) {
			assertEquals(1, pdom.findNames(inst, IIndexFragment.FIND_REFERENCES).length);
		}
		instances= Arrays.asList(((ICPPInstanceCache) fooAB).getAllInstances());
		assertEquals(6, instances.size());
		for (ICPPTemplateInstance inst : instances) {
			assertEquals(1, pdom.findNames(inst, IIndexFragment.FIND_REFERENCES).length);
		}
	}
}
