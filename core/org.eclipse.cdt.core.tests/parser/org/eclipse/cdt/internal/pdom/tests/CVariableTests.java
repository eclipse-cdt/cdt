/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.pdom.tests;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.PDOM;

/**
 * Tests for verifying whether the PDOM correctly stores information about
 * C variable declarations.
 */
public class CVariableTests extends PDOMTestBase {

	protected ICProject project;
	protected PDOM pdom;

	public static Test suite() {
		return suite(CVariableTests.class);
	}

	protected void setUp() throws Exception {
		project = createProject("variableTests");
		pdom = (PDOM) CCorePlugin.getPDOMManager().getPDOM(project);
		pdom.acquireReadLock();
	}

	protected void tearDown() throws Exception {
		pdom.releaseReadLock();
	}
	
	public void testCAutoVariable() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "autoCVariable");
		assertEquals(1, bindings.length);
		IVariable variable = (IVariable) bindings[0];
		assertTrue(variable.isAuto());
	}

	public void testCExternVariable() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "externCVariable");
		assertEquals(1, bindings.length);
		IVariable variable = (IVariable) bindings[0];
		assertTrue(variable.isExtern());
	}

	public void testCRegisterVariable() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "registerCVariable");
		assertEquals(1, bindings.length);
		IVariable variable = (IVariable) bindings[0];
		assertTrue(variable.isRegister());
	}

	public void testCStaticVariable() throws Exception {
		IBinding[] bindings = findQualifiedName(pdom, "staticCVariable");
		assertEquals(1, bindings.length);
		IVariable variable = (IVariable) bindings[0];
		assertTrue(variable.isStatic());
	}

}
