/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
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

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Doug Schaefer
 *
 */
public class TypesTests extends PDOMTestBase {

	protected PDOM pdom;

	public static Test suite() {
		return suite(TypesTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		if (pdom == null) {
			ICProject project = createProject("types");
			pdom = (PDOM)CCoreInternals.getPDOMManager().getPDOM(project);
		}
		pdom.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}
	
	public void testC() throws Exception {
		// Get the binding for A::f
		IBinding [] CAs = pdom.findBindings(Pattern.compile("CA"), false, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(1, CAs.length);
		ICompositeType CA = (ICompositeType)CAs[0];
		IField [] CAfields = CA.getFields();
		assertEquals(1, CAfields.length);
		IField x = CAfields[0];
		assertEquals("x", x.getName());
		
		// Make sure that there is a reference in g();
		IName[] xRefs = pdom.findNames(x, IIndex.FIND_REFERENCES);
		assertEquals(1, xRefs.length);
		IASTFileLocation loc = xRefs[0].getFileLocation();
		assertEquals(offset("typedef.c", "x->x") + 3, loc.getNodeOffset());
	}

	public void testCPP() throws Exception {
		// Get the binding for A::f
		IBinding [] As = pdom.findBindings(Pattern.compile("A"), true, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(1, As.length);
		ICPPClassType A = (ICPPClassType)As[0];
		ICPPMethod[] Amethods = A.getDeclaredMethods();
		assertEquals(1, Amethods.length);
		ICPPMethod f = Amethods[0];
		assertEquals("f", f.getName());
		
		// Make sure that there is a reference in g();
		IName[] fRefs = pdom.findNames(f, IIndex.FIND_REFERENCES);
		assertEquals(1, fRefs.length);
		IASTFileLocation loc = fRefs[0].getFileLocation();
		assertEquals(offset("typedef.cpp", "x->f") + 3, loc.getNodeOffset());
	}
	
	public void test145351() throws Exception {
		IBinding [] bindings = pdom.findBindings(Pattern.compile("spinlock_t"), false, IndexFilter.ALL, new NullProgressMonitor());
		assertEquals(1, bindings.length);
		ITypedef spinlock_t = (ITypedef)bindings[0];
		IName [] refs = pdom.findNames(spinlock_t, IIndex.FIND_REFERENCES);
		assertEquals(1, refs.length);
		IASTFileLocation loc = refs[0].getFileLocation();
		assertEquals(offset("bug145351.c", "spinlock_t global_bh_lock"), loc.getNodeOffset());
	}
	
}
