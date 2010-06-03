/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C++ variable declarations.
 */
public class CPPVariableTests extends PDOMTestBase {

	protected ICProject project;
	protected PDOM pdom;

	public static Test suite() {
		return suite(CPPVariableTests.class);
	}

	protected void setUp() throws Exception {
		project = createProject("variableTests");
		pdom = (PDOM) CCoreInternals.getPDOMManager().getPDOM(project);
		pdom.acquireReadLock();
	}

	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
		if (project != null) {
			project.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
		}
	}
	
	public void testCPPAutoVariable() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "autoCPPVariable");
		assertEquals(1, bindings.length);
		ICPPVariable variable = (ICPPVariable) bindings[0];
		assertFalse(variable.isExtern());
		assertFalse(variable.isStatic());
	}

	public void testCPPExternVariable() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "externCPPVariable");
		assertEquals(1, bindings.length);
		ICPPVariable variable = (ICPPVariable) bindings[0];
		assertTrue(variable.isExtern());
	}

	public void testCPPRegisterVariable() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "registerCPPVariable");
		assertEquals(1, bindings.length);
		ICPPVariable variable = (ICPPVariable) bindings[0];
		assertTrue(variable.isRegister());
	}

	public void testCPPStaticVariable() throws Exception {
		// static elements cannot be found on global scope, see bug 161216
		IBinding[] bindings = findUnqualifiedName(pdom, "staticCPPVariable");
		assertEquals(1, bindings.length);
		ICPPVariable variable = (ICPPVariable) bindings[0];
		assertTrue(variable.isStatic());
	}
}
