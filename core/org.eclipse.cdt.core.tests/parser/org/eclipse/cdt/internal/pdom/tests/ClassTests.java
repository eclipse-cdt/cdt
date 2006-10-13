/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.regex.Pattern;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Doug Schaefer
 *
 */
public class ClassTests extends PDOMTestBase {

	protected PDOM pdom;

	public static Test suite() {
		return suite(ClassTests.class);
	}

	protected void setUp() throws Exception {
		if (pdom == null) {
			ICProject project = createProject("classTests");
			pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(project);
		}
		pdom.acquireReadLock();
	}

	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}
	
	public void test1() throws Exception {
		IBinding[] Bs = pdom.findBindings(Pattern.compile("B"), false, new IndexFilter(), new NullProgressMonitor());
		assertEquals(1, Bs.length);
		ICPPClassType B = (ICPPClassType)Bs[0];
		ICPPMethod[] Bmethods = B.getAllDeclaredMethods();
		assertEquals(1, Bmethods.length);
		ICPPMethod Bf = Bmethods[0];
		assertEquals("f", Bf.getName());
		IName [] Bf_refs = pdom.findNames(Bf, IIndex.FIND_REFERENCES);
		assertEquals(1, Bf_refs.length);
		IASTFileLocation loc = Bf_refs[0].getFileLocation();
		assertEquals(offset("class.cpp", "b.f()") + 2, loc.getNodeOffset());
	}
	
	public void testNested() throws Exception {
		IBinding[] bindings = pdom.findBindings(Pattern.compile("NestedA"), false, new IndexFilter(), new NullProgressMonitor());
		assertEquals(1, bindings.length);
		ICPPClassType NestedA = (ICPPClassType)bindings[0];
		ICPPClassType[] nested = NestedA.getNestedClasses();
		assertEquals(1, nested.length);
		ICPPClassType NestedB = nested[0];
		assertEquals("NestedB", NestedB.getName());
		IField[] fields = NestedB.getFields();
		assertEquals(1, fields.length);
		IField NestedB_x = fields[0];
		
		IName[] refs = pdom.findNames(NestedB, IIndex.FIND_REFERENCES);
		assertEquals(1, refs.length);
		IASTFileLocation loc = refs[0].getFileLocation();
		assertEquals(offset("nested.cpp", "::NestedB") + 2, loc.getNodeOffset());
		
		refs = pdom.findNames(NestedB_x, IIndex.FIND_REFERENCES);
		assertEquals(1, refs.length);
		loc = refs[0].getFileLocation();
		assertEquals(offset("nested.cpp", "x.x") + 2, loc.getNodeOffset());
	}
	
	public void failedTest147903() throws Exception {
		IBinding[] bindings = pdom.findBindings(Pattern.compile("pr147903"), false, new IndexFilter(), new NullProgressMonitor());
		assertEquals(1, bindings.length);
		ICPPNamespaceScope ns = ((ICPPNamespace)bindings[0]).getNamespaceScope();
		bindings = ns.find("testRef");
		assertEquals(1, bindings.length);
		IName[] refs = pdom.findNames(bindings[0], IIndex.FIND_REFERENCES);
		for (int i = 0; i < refs.length; ++i)
			System.out.println(refs[i].getFileLocation().getNodeOffset());
		assertEquals(5, refs.length);
	}
}
