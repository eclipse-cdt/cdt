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
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Doug Schaefer
 *
 */
public class TypesTests extends PDOMTestBase {

	protected PDOM pdom;

	protected void setUp() throws Exception {
		if (pdom == null) {
			ICProject project = createProject("types");
			pdom = (PDOM)CCorePlugin.getPDOMManager().getPDOM(project);
		}
		pdom.acquireReadLock();
	}

	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}
	
	public void testC() throws Exception {
		// Get the binding for A::f
		IBinding [] CAs = pdom.findBindings(Pattern.compile("CA"), new NullProgressMonitor());
		assertEquals(1, CAs.length);
		ICompositeType CA = (ICompositeType)CAs[0];
		IField [] CAfields = CA.getFields();
		assertEquals(1, CAfields.length);
		IField x = CAfields[0];
		assertEquals("x", x.getName());
		
		// Make sure that there is a reference in g();
		IName[] xRefs = pdom.getReferences(x);
		assertEquals(1, xRefs.length);
		IASTFileLocation loc = xRefs[0].getFileLocation();
		assertEquals(offset(85, 77), loc.getNodeOffset());
	}

	public void testCPP() throws Exception {
		// Get the binding for A::f
		IBinding [] As = pdom.findBindings(Pattern.compile("A"), new NullProgressMonitor());
		assertEquals(1, As.length);
		ICPPClassType A = (ICPPClassType)As[0];
		ICPPMethod[] Amethods = A.getMethods();
		assertEquals(1, Amethods.length);
		ICPPMethod f = Amethods[0];
		assertEquals("f", f.getName());
		
		// Make sure that there is a reference in g();
		IName[] fRefs = pdom.getReferences(f);
		assertEquals(1, fRefs.length);
		IASTFileLocation loc = fRefs[0].getFileLocation();
		assertEquals(offset(84, 74), loc.getNodeOffset());
	}
	
	public void test145351() throws Exception {
		IBinding [] bindings = pdom.findBindings(Pattern.compile("spinlock_t"), new NullProgressMonitor());
		assertEquals(1, bindings.length);
		ITypedef spinlock_t = (ITypedef)bindings[0];
		IName [] refs = pdom.getReferences(spinlock_t);
		assertEquals(1, refs.length);
		IASTFileLocation loc = refs[0].getFileLocation();
		assertEquals(offset(44, 40), loc.getNodeOffset());
	}
	
}
