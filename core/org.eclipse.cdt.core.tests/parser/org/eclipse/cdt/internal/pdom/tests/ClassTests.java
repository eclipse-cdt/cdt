/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM;

/**
 * @author Doug Schaefer
 *
 */
public class ClassTests extends PDOMTestBase {

	protected ICProject project;
	
	protected void setUp() throws Exception {
		project = createProject("classTests");
	}

	public void test1() throws Exception {
		PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(project);
		
		IBinding[] Bs = pdom.findBindings(Pattern.compile("B"));
		assertEquals(1, Bs.length);
		ICPPClassType B = (ICPPClassType)Bs[0];
		ICPPMethod[] Bmethods = B.getAllDeclaredMethods();
		assertEquals(1, Bmethods.length);
		ICPPMethod Bf = Bmethods[0];
		assertEquals("f", Bf.getName());
		IASTName [] Bf_refs = pdom.getReferences(Bf);
		assertEquals(1, Bf_refs.length);
		IASTFileLocation loc = Bf_refs[0].getFileLocation();
		assertEquals(offset(95, 84), loc.getNodeOffset());
	}
	
	public void testNested() throws Exception {
		PDOM pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(project);

		IBinding[] bindings = pdom.findBindings(Pattern.compile("NestedA"));
		assertEquals(1, bindings.length);
		ICPPClassType NestedA = (ICPPClassType)bindings[0];
		ICPPClassType[] nested = NestedA.getNestedClasses();
		assertEquals(1, nested.length);
		ICPPClassType NestedB = nested[0];
		assertEquals("NestedB", NestedB.getName());
		IField[] fields = NestedB.getFields();
		assertEquals(1, fields.length);
		IField NestedB_x = fields[0];
		
		IASTName[] refs = pdom.getReferences(NestedB);
		assertEquals(1, refs.length);
		IASTFileLocation loc = refs[0].getFileLocation();
		assertEquals(offset(96, 86), loc.getNodeOffset());
		
		refs = pdom.getReferences(NestedB_x);
		assertEquals(1, refs.length);
		loc = refs[0].getFileLocation();
		assertEquals(offset(118, 108), loc.getNodeOffset());
	}
}
