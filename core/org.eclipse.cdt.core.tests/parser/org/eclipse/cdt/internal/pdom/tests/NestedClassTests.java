/*******************************************************************************
 * Copyright (c) 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Corbat (IFS) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.regex.Pattern;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;


public class NestedClassTests extends PDOMTestBase {

	protected PDOM pdom;

	public static Test suite() {
		return suite(NestedClassTests.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
		if (pdom == null) {
			ICProject project = createProject("nestedClassTests");
			pdom = (PDOM)CCoreInternals.getPDOMManager().getPDOM(project);
		}
		pdom.acquireReadLock();
	}

	@Override
	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}

	public void testNestedInClassDefaultVisibility() throws Exception {
		String qualifiedClassName = "DefaultInner";
		ICPPMember enclosingClassDefaultInner = assertUniqueNestedClass(qualifiedClassName);
		assertEquals(ICPPMember.v_private, enclosingClassDefaultInner.getVisibility());
	}

	public void testNestedInStructDefaultVisibility() throws Exception {
		String qualifiedClassName = "StructDefaultInner";
		ICPPMember enclosingStructDefaultInner = assertUniqueNestedClass(qualifiedClassName);
		assertEquals(ICPPMember.v_public, enclosingStructDefaultInner.getVisibility());
	}

	public void testNestedInClassPublicVisibility() throws Exception {
		String qualifiedClassName = "PublicInner";
		ICPPMember enclosingClassPublicInner = assertUniqueNestedClass(qualifiedClassName);
		assertEquals(ICPPMember.v_public, enclosingClassPublicInner.getVisibility());
	}

	public void testNestedInClassProtectedVisibility() throws Exception {
		String qualifiedClassName = "ProtectedInner";
		ICPPMember enclosingClassProtectedInner = assertUniqueNestedClass(qualifiedClassName);
		assertEquals(ICPPMember.v_protected, enclosingClassProtectedInner.getVisibility());
	}

	public void testNestedInClassPrivateVisibility() throws Exception {
		String qualifiedClassName = "PrivateInner";
		ICPPMember enclosingClassPrivateInner = assertUniqueNestedClass(qualifiedClassName);
		assertEquals(ICPPMember.v_private, enclosingClassPrivateInner.getVisibility());
	}

	public void testNestedClassOwner() throws Exception {
		String qualifiedClassName = "DefaultInner";
		ICPPMember enclosingClassDefaultInner = assertUniqueNestedClass(qualifiedClassName);
		
		IBinding[] findResult = pdom.findBindings(Pattern.compile("EnclosingClass"), false, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, findResult.length);
		IBinding enclosing = findResult[0];
		assertInstance(enclosing, ICPPClassType.class);
		
		assertEquals(enclosing, enclosingClassDefaultInner.getClassOwner());
	}

	public void testNestedClassNotStatic() throws Exception {
		String qualifiedClassName = "DefaultInner";
		ICPPMember enclosingClassDefaultInner = assertUniqueNestedClass(qualifiedClassName);
		assertFalse(enclosingClassDefaultInner.isStatic());
	}

	public void testNestedClassType() throws Exception {
		String qualifiedClassName = "DefaultInner";
		ICPPMember enclosingClassDefaultInner = assertUniqueNestedClass(qualifiedClassName);
		assertEquals(enclosingClassDefaultInner, enclosingClassDefaultInner.getType());
	}

	private ICPPMember assertUniqueNestedClass(String qualifiedClassName) throws CoreException {
		IBinding[] findResult = pdom.findBindings(Pattern.compile(qualifiedClassName), false, IndexFilter.ALL_DECLARED, npm());
		assertEquals(1, findResult.length);
		IBinding resultBinding = findResult[0];
		assertInstance(resultBinding, ICPPClassType.class);
		assertInstance(resultBinding, ICPPMember.class);
		return (ICPPMember) resultBinding;
	}
}
